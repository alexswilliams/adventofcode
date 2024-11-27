package aoc2023.day6

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("aoc2023/day6", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day6", "input.txt")

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzles[0]) } // 16µs
    benchmark { part2(puzzles[0]) } // 4µs
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(288, "P1 Example") { part1(examples[0]) }
        check(800280, "P1 Puzzle") { part1(puzzles[0]) }

        check(71503, "P2 Example") { part2(examples[0]) }
        check(45128024, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private fun part1(input: List<String>) =
    input.map { line -> line.split(Regex("\\s+")).tail().map { it.toDouble() } }
        .transpose()
        .map { (time, distance) -> countWinningTimes(time, distance) }
        .product()

private fun part2(input: List<String>) =
    input.map { it.substringAfter(':').replace(" ", "").toDouble() }
        .let { (time, distance) -> countWinningTimes(time, distance) }

private fun countWinningTimes(time: Double, distanceToBeat: Double): Int {
    val offsetFromBestHoldTime = sqrt(time * time - 4 * distanceToBeat)
    val longestHoldTime = (time + offsetFromBestHoldTime) / 2
    val shortestHoldTime = (time - offsetFromBestHoldTime) / 2
    return ceil(longestHoldTime - 1).toInt() - floor(shortestHoldTime + 1).toInt() + 1
}
