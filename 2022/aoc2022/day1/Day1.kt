package aoc2022.day1

import common.*
import kotlin.test.*

private val exampleInput = "aoc2022/day1/example.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_ANSWER = 24000;
private const val PART_2_EXPECTED_ANSWER = 45000

private val puzzleInput = "aoc2022/day1/input.txt".fromClasspathFileToLines()

fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1: " + part1(puzzleInput)) // 69289

    assertEquals(PART_2_EXPECTED_ANSWER, part2(exampleInput))
    println("Part 2: " + part2(puzzleInput)) // 205615
}

private fun part1(input: List<String>) = snacksPerElf(input).max()
private fun part2(input: List<String>) = snacksPerElf(input).sortedDescending().take(3).sum()

private fun snacksPerElf(input: List<String>) =
    input.map { it.toIntOrNull() }
        .fold(emptyList<Int>() to 0) { (acc, sum), i -> if (i == null) acc.plus(sum) to 0 else acc to sum.plus(i) }
        .let { (acc, sum) -> acc.plus(sum) }
