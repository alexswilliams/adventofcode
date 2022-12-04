package day4

import common.*
import kotlin.test.*

private val exampleInput = "day4/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day4/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_ANSWER = 2
private const val PART_2_EXPECTED_ANSWER = 4

fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1: " + part1(puzzleInput)) // 569

    assertEquals(PART_2_EXPECTED_ANSWER, part2(exampleInput))
    println("Part 2: " + part2(puzzleInput)) // 936
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
