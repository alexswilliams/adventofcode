package day13

import common.*
import kotlin.test.*

//import kotlin.time.*

private val exampleInput = "day13/example.txt".fromClasspathFile()
private val puzzleInput = "day13/input.txt".fromClasspathFile()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 13
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 140

//@OptIn(ExperimentalTime::class)
fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 5503

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 20952
//
//    repeat(20) { part2(puzzleInput) }
//    println(measureTime { repeat(10000) { part2(puzzleInput) } }.div(10000))
}

private fun part1(input: String) = input
    .split("\n\n")
    .map { it.lines().map { line -> line.toCharArray() } }
    .map { packetComparator(it[0], it[1]) }.withIndex()
    .sumOf { if (it.value < 0) it.index + 1 else 0 }

private val beacon2 = "[[2]]".toCharArray()
private val beacon6 = "[[6]]".toCharArray()
private fun part2(input: String) = input
    .lines().filterNotBlank()
    .map { it.toCharArray() }
    .plus(listOf(beacon2, beacon6))
    .sortedWith(::packetComparator)
    .let { (it.indexOf(beacon2) + 1) * (it.indexOf(beacon6) + 1) }


private fun packetComparator(s0: CharArray, s1: CharArray): Int {
    var sL = s0
    var sR = s1
    var lIdx = 0
    var rIdx = 0
    while (true) {
        val left = sL[lIdx]
        val right = sR[rIdx]
        when {
            left in '0'..'9' && right in '0'..'9' -> {
                val lInt = consumeNumber(sL, lIdx).also { lIdx = it and 0xffff }
                val rInt = consumeNumber(sR, rIdx).also { rIdx = it and 0xffff }
                if ((lInt shr 16) != (rInt shr 16)) return if (lInt < rInt) -1 else 1
            }

            left == right -> {
                lIdx++;rIdx++
            }

            left == '[' && right in '0'..'9' -> sR = surroundNumberAt(sR, rIdx).also { rIdx = 0 }
            right == '[' && left in '0'..'9' -> sL = surroundNumberAt(sL, lIdx).also { lIdx = 0 }
            left == ']' || right == ']' -> return if (left == ']') -1 else 1
        }
    }
}

private fun consumeNumber(s: CharArray, startAt: Int): Int {
    var value = 0
    var i = startAt
    while (s[i] in '0'..'9')
        value = value * 10 + (s[i++] - '0')
    return (value shl 16) + i
}

private fun surroundNumberAt(s: CharArray, i: Int): CharArray {
    val array = CharArray(s.size - i + 2)
    var p = i
    array[0] = '['
    while (s[p] in '0'..'9') array[p - i + 1] = s[p++]
    array[p - i + 1] = ']'
    System.arraycopy(s, p, array, p - i + 2, s.size - p)
    return array
}
