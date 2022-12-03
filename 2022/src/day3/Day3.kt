package day3

import common.*
import kotlin.test.*

private val exampleInput = "day3/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day3/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_ANSWER = 157
private const val PART_2_EXPECTED_ANSWER = 70

fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1: " + part1(puzzleInput)) // 7716

    assertEquals(PART_2_EXPECTED_ANSWER, part2(exampleInput))
    println("Part 2: " + part2(puzzleInput)) // 2973
}

private fun part1(input: List<String>) = input
    .map { it.chunked(it.length / 2) }
    .map { (left, right) -> left.first { it in right } }
    .sumOf { it.priority }

private fun part2(input: List<String>) = input
    .chunked(3)
    .map { (elf1, elf2, elf3) -> elf1.first { it in elf2 && it in elf3 } }
    .sumOf { it.priority }

private val Char.priority: Int
    get() = when (this) {
        in 'a'..'z' -> (this - 'a') + 1
        in 'A'..'Z' -> (this - 'A') + 27
        else -> 0
    }
