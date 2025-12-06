package aoc2022.day4

import common.*

private val example = loadFilesToLines("aoc2022/day4", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day4", "input.txt").single()

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzle) } // 227.6µs
    benchmark { part2(puzzle) } // 151.9µs
}

internal object Day4 : Challenge {
    override fun assertCorrect() {
        check(2, "P1 Example") { part1(example) }
        check(569, "P1 Puzzle") { part1(puzzle) }

        check(4, "P2 Example") { part2(example) }
        check(936, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: List<String>): Int = input
    .toIntRanges()
    .count { (area1, area2) -> area1 fullyContains area2 || area2 fullyContains area1 }

private fun part2(input: List<String>): Int = input
    .toIntRanges()
    .count { (area1, area2) -> area1 overlaps area2 }


private fun List<String>.toIntRanges(): List<List<IntRange>> = this.map { line ->
    line.split(',').map { range ->
        range.split('-').map(String::toInt).let { (lower, upper) -> IntRange(lower, upper) }
    }
}
