package aoc2023.day6

import common.benchmark
import common.fromClasspathFileToLines
import common.product
import common.tail
import common.transpose
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day6/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day6/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(288, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(800280, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(71503, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(45128024, it) }
    benchmark { part1(puzzleInput) } // 16µs
    benchmark { part2(puzzleInput) } // 4µs
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
    return floor(longestHoldTime - 0.001).toInt() - ceil(shortestHoldTime + 0.001).toInt() + 1
}
