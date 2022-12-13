package day13

import common.*
import kotlin.test.*

private val exampleInput = "day13/example.txt".fromClasspathFile()
private val puzzleInput = "day13/input.txt".fromClasspathFile()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 13
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 140

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 5503

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 20952
}

private fun part1(input: String) = input
    .linesAsCharArrays().chunked(3)
    .map { packetComparator(it[0], it[1]) }.withIndex()
    .sumOf { if (it.value < 0) it.index + 1 else 0 }

private val beacon2 = "[[2]]".toCharArray()
private val beacon6 = "[[6]]".toCharArray()
private fun part2(input: String) = input
    .linesAsCharArrays().filter { it.isNotEmpty() }
    .plus(listOf(beacon2, beacon6))
    .sortedWith(::packetComparator)
    .let { (it.indexOf(beacon2) + 1) * (it.indexOf(beacon6) + 1) }


private fun packetComparator(s0: CharArray, s1: CharArray): Int {
    var sL = s0
    var sR = s1
    var l = 0
    var r = 0
    var xR = false;
    var xL = false
    while (true) {
        val left = sL[l]
        val right = sR[r]
        when {
            left in '0'..'9' && right in '0'..'9' -> {
                val lInt = consumeNumber(sL, l).also { l = it and 0xffff }
                val rInt = consumeNumber(sR, r).also { r = it and 0xffff }
                if ((lInt shr 16) != (rInt shr 16)) return if (lInt < rInt) -1 else 1
            }

            left == right -> {
                l++;r++
            }

            left == '[' && right in '0'..'9' -> sR = surroundNumberAt(sR, r, xR, bufferR).also { r = idxAfterExtend(xR, r, it, sR); xR = true }
            right == '[' && left in '0'..'9' -> sL = surroundNumberAt(sL, l, xL, bufferL).also { l = idxAfterExtend(xL, l, it, sL); xL = true }

            left == ']' || right == ']' -> return if (left == ']') -1 else 1
        }
    }
}

private fun idxAfterExtend(expanded: Boolean, oldIdx: Int, newString: CharArray, oldString: CharArray) =
    if (expanded) oldIdx - 2
    else newString.size - (oldString.size - oldIdx) - 2

// Shunts the currently referenced number earlier by 1 character, and adds [ and ] around it.
// If the array is too small to accommodate this, explode it into a static buffer (to avoid runtime allocation)
//
// i = 3, growBy = 4
//idx  0 1 2 3 4 5 6 7 8 9 a b
//  s: [ 1 , 2 3 , 4 ]
//  A: _ _ _ _ _ _ _ _ _ _ _ _
//  B: _ _ _ _ _ _ _ 2 3 , 4 ]
//  C: _ _ _ _ _ [ 2 3 ] , 4 ]
// newI = 5
//
// i = 7, prevXp = true
//idx  0 1 2 3 4 5 6 7 8 9 a b
//  s: _ _ _ [ 1 ] , 2 3 , 4 ]
//  D: _ _ _ _ _ [ 2 3 ] , 4 ]
// newI = 5
private val bufferL = CharArray(512)
private val bufferR = CharArray(512)
private fun surroundNumberAt(s: CharArray, i: Int, previouslyExpanded: Boolean, newBuffer: CharArray): CharArray {
    if (previouslyExpanded) {
        var idx = i
        s[idx - 2] = '['
        while (s[idx] in '0'..'9') s[idx - 1] = s[idx++]
        s[idx - 1] = ']' // D
        return s
    }
    val lengthToEnd = s.size - i
    System.arraycopy(s, i, newBuffer, newBuffer.size - lengthToEnd, lengthToEnd) // B
    var toA = newBuffer.size - lengthToEnd
    newBuffer[toA - 2] = '['
    while (newBuffer[toA] in '0'..'9') newBuffer[toA - 1] = newBuffer[toA++]
    newBuffer[toA - 1] = ']' // C
    return newBuffer
}

// Evaluate the currently referenced number, and return it in the top half of the return value; the next index to continue from is stored in the lsw
// Assumes that none of the lines are > 64k characters long, and none of the values are > 64k
private fun consumeNumber(s: CharArray, startAt: Int): Int {
    var value = 0
    var i = startAt
    while (s[i] in '0'..'9') value = value * 10 + (s[i++] - '0')
    return (value shl 16) + i
}

