package aoc2023.day1

import common.benchmark
import common.firstNotNullOfIndexed
import common.fromClasspathFileToLines
import common.lastNotNullOfIndexed
import kotlin.test.assertEquals

private val exampleInput1 = "aoc2023/day1/example1.txt".fromClasspathFileToLines()
private val exampleInput2 = "aoc2023/day1/example2.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day1/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput1).also { println("[Example] Part 1: $it") }.also { assertEquals(142, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(55477, it) }

    part2(exampleInput2).also { println("[Example] Part 2: $it") }.also { assertEquals(281, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(54431, it) }

    benchmark { part2(puzzleInput) } // ~85µs
}

private fun part1(input: List<String>) = input.sumOf { line ->
    line.first { it in digits }.digitToInt() * 10 +
            line.last { it in digits }.digitToInt()
}

private fun part2(input: List<String>) = input.sumOf { line ->
    line.firstNotNullOfIndexed { index, c -> getDigitFromWordOrNull(line, c, index) } * 10 +
            line.lastNotNullOfIndexed { index, c -> getDigitFromWordOrNull(line, c, index) }
}

private val digits = '1'..'9' // note 0 is missing, so .isDigit() may give wrong answer
private fun getDigitFromWordOrNull(s: String, c: Char, index: Int): Int? =
    when {
        c in digits -> c.digitToInt()
        c == 'o' && s.regionMatches(index, "one", 0, 3) -> 1
        c == 't' && s.regionMatches(index, "two", 0, 3) -> 2
        c == 't' && s.regionMatches(index, "three", 0, 5) -> 3
        c == 'f' && s.regionMatches(index, "four", 0, 4) -> 4
        c == 'f' && s.regionMatches(index, "five", 0, 4) -> 5
        c == 's' && s.regionMatches(index, "six", 0, 3) -> 6
        c == 's' && s.regionMatches(index, "seven", 0, 5) -> 7
        c == 'e' && s.regionMatches(index, "eight", 0, 5) -> 8
        c == 'n' && s.regionMatches(index, "nine", 0, 4) -> 9
        else -> null
    }
