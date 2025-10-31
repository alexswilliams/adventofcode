package aoc2024.day13

import common.*

private val example = loadFilesToLines("aoc2024/day13", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day13", "input.txt").single()

internal fun main() {
    Day13.assertCorrect()
    benchmark { part1(puzzle) } // 274µs
    benchmark { part2(puzzle) } // 156µs
}

internal object Day13 : Challenge {
    override fun assertCorrect() {
        check(480, "P1 Example") { part1(example) }
        check(35082, "P1 Puzzle") { part1(puzzle) }

        check(875318608908L, "P2 Example") { part2(example) }
        check(82570698600470L, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: List<String>): Long = payToWin(input, 0, false)
private fun part2(input: List<String>): Long = payToWin(input, TEN_BAJILLION, true)

private val INPUT_MATCHER = Regex(".*X.(\\d+), Y.(\\d+)")
private const val TEN_BAJILLION = 10_000_000_000_000

private fun payToWin(input: List<String>, movePrizeAwayBy: Long, armsNeverGetTired: Boolean) = input.windowed(3, 4).sumOf { claw ->
    val (ax, ay) = claw[0].matchingAsLongList(INPUT_MATCHER) ?: error("invalid input")
    val (bx, by) = claw[1].matchingAsLongList(INPUT_MATCHER) ?: error("invalid input")
    val (px, py) = claw[2].matchingAsLongList(INPUT_MATCHER)?.map { it + movePrizeAwayBy } ?: error("invalid input")
    val aCount = (by * px - bx * py) / (ax * by - bx * ay)
    val bCount = (ax * py - ay * px) / (ax * by - bx * ay)
    val hasIntegerSolution = aCount * ax + bCount * bx == px && aCount * ay + bCount * by == py
    val hasHumaneNumberOfButtonPushes = armsNeverGetTired || aCount <= 100 && bCount <= 100
    if (hasIntegerSolution && hasHumaneNumberOfButtonPushes)
        aCount * 3 + bCount
    else 0
}
