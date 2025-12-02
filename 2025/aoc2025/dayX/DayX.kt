package aoc2025.dayX

import common.*

private val example = loadFilesToLines("aoc2025/dayX", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/dayX", "input.txt").single()

internal fun main() {
    DayX.assertCorrect()
    benchmark { part1(puzzle) }
    benchmark { part2(puzzle) }
}

internal object DayX : Challenge {
    override fun assertCorrect() {
        check(0, "P1 Example") { part1(example) }
        check(0, "P1 Puzzle") { part1(puzzle) }

        check(0, "P2 Example") { part2(example) }
        check(0, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int = 0
private fun part2(input: List<String>): Int = 0
