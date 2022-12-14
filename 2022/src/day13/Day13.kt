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


private fun packetComparator(sL: CharArray, sR: CharArray): Int {
    var leftIndex = 0
    var rightIndex = 0
    var openL = 0 // extra bracket pairs only wrap a single token, so they can be tracked with just a count
    var closeL = 0
    var openR = 0
    var closeR = 0
    while (true) {
        val left = if (openL > 0) '[' else if (sL[leftIndex] !in '0'..'9' && closeL > 0) ']' else sL[leftIndex]
        val right = if (openR > 0) '[' else if (sR[rightIndex] !in '0'..'9' && closeR > 0) ']' else sR[rightIndex]
        when {
            left in '0'..'9' && right in '0'..'9' -> {
                val lInt = consumeNumber(sL, leftIndex).also { leftIndex = it and 0xffff }
                val rInt = consumeNumber(sR, rightIndex).also { rightIndex = it and 0xffff }
                if ((lInt shr 16) != (rInt shr 16)) return (if (lInt < rInt) -1 else 1)
            }

            left == right -> {
                if (openL > 0) openL-- else if (closeL > 0) closeL-- else leftIndex++
                if (openR > 0) openR-- else if (closeR > 0) closeR-- else rightIndex++
            }

            left == '[' && right in '0'..'9' -> openR++.also { closeR++ }
            right == '[' && left in '0'..'9' -> openL++.also { closeL++ }

            left == ']' || right == ']' -> return (if (left == ']') -1 else 1)
        }
    }
}

// Evaluate the currently referenced number, and return it in the top half of the return value; the next index to continue from is stored in the lsw
// Assumes that none of the lines are > 64k characters long, and none of the values are > 64k
private fun consumeNumber(s: CharArray, startAt: Int): Int {
    var value = 0
    var i = startAt
    while (s[i] in '0'..'9') value = value * 10 + (s[i++] - '0')
    return (value shl 16) + i
}

