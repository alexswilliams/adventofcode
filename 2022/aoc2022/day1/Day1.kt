package aoc2022.day1

import common.*

private val example = loadFilesToLines("aoc2022/day1", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day1", "input.txt").single()

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzle) } // 109.7µs
    benchmark { part2(puzzle) } // 75.9µs
}

internal object Day1 : Challenge {
    override fun assertCorrect() {
        check(24000, "P1 Example") { part1(example) }
        check(69289, "P1 Puzzle") { part1(puzzle) }

        check(45000, "P2 Example") { part2(example) }
        check(205615, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: List<String>) = snacksPerElf(input).max()
private fun part2(input: List<String>) = snacksPerElf(input).sortedDescending().take(3).sum()

private fun snacksPerElf(input: List<String>) =
    input.map { it.toIntOrNull() }
        .fold(emptyList<Int>() to 0) { (acc, sum), i -> if (i == null) acc.plus(sum) to 0 else acc to sum.plus(i) }
        .let { (acc, sum) -> acc.plus(sum) }
