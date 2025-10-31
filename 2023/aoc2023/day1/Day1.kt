package aoc2023.day1

import common.*

private val examples = loadFilesToLines("aoc2023/day1", "example1.txt", "example2.txt")
private val puzzles = loadFilesToLines("aoc2023/day1", "input.txt")

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzles[0]) } // ~23µs
    benchmark { part2(puzzles[0]) } // ~82µs
}

internal object Day1 : Challenge {
    override fun assertCorrect() {
        check(142, "P1 Example") { part1(examples[0]) }
        check(55477, "P1 Puzzle") { part1(puzzles[0]) }

        check(281, "P2 Example") { part2(examples[1]) }
        check(54431, "P2 Puzzle") { part2(puzzles[0]) }
    }
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
    when (c) {
        in digits -> c.digitToInt()
        'o' if s.regionMatches(index, "one", 0, 3) -> 1
        't' if s.regionMatches(index, "two", 0, 3) -> 2
        't' if s.regionMatches(index, "three", 0, 5) -> 3
        'f' if s.regionMatches(index, "four", 0, 4) -> 4
        'f' if s.regionMatches(index, "five", 0, 4) -> 5
        's' if s.regionMatches(index, "six", 0, 3) -> 6
        's' if s.regionMatches(index, "seven", 0, 5) -> 7
        'e' if s.regionMatches(index, "eight", 0, 5) -> 8
        'n' if s.regionMatches(index, "nine", 0, 4) -> 9
        else -> null
    }
