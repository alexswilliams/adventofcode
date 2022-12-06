package day6

import common.*
import kotlin.test.*

private val exampleInputs = mapOf(
    "mjqjpqmgbljsphdztnvjfqwrcgsmlb" to (7 to 19),
    "bvwbjplbgvbhsrlpgdmjqwftvncz" to (5 to 23),
    "nppdvjthqldpwncqszvftbrmjlhg" to (6 to 23),
    "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg" to (10 to 29),
    "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw" to (11 to 26),
)
private val puzzleInput = "day6/input.txt".fromClasspathFileToLines().single()

fun main() {
    exampleInputs.forEach { (input, expectedOutput) -> assertEquals(expectedOutput.first, part1(input)) }
    println("Part 1: " + part1(puzzleInput)) // 1142

    exampleInputs.forEach { (input, expectedOutput) -> assertEquals(expectedOutput.second, part2(input)) }
    println("Part 2: " + part2(puzzleInput)) // 2803
}

private fun part1(input: String) = solveForRunLength(input, 4)
private fun part2(input: String) = solveForRunLength(input, 14)

private fun solveForRunLength(input: String, runLength: Int) = input
    .windowedSequence(runLength, 1).withIndex()
    .first { it.value.toSet().size == it.value.length }
    .index + runLength
