package aoc2023.day1

import common.fromClasspathFileToLines
import kotlin.test.assertEquals

private val exampleInput1 = "aoc2023/day1/example1.txt".fromClasspathFileToLines()
private val exampleInput2 = "aoc2023/day1/example2.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day1/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput1).also { println("[Example] Part 1: $it") }.also { assertEquals(142, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(55477, it) }

    part2(exampleInput2).also { println("[Example] Part 2: $it") }.also { assertEquals(281, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(54431, it) }
}

private fun part1(input: List<String>) = input.sumOf { line -> line.firstAndLastDigitAsInt() }
private fun part2(input: List<String>) = input.sumOf { line -> line.mapOverlappingWordsToDigits().firstAndLastDigitAsInt() }

private fun String.firstAndLastDigitAsInt() = filter { it.isDigit() }
        .let { it.first().digitToInt() * 10 + it.last().digitToInt() }

private fun String.mapOverlappingWordsToDigits(): String {
    if (isEmpty()) return ""
    val digit = when {
        startsWith("one") -> '1'
        startsWith("two") -> '2'
        startsWith("three") -> '3'
        startsWith("four") -> '4'
        startsWith("five") -> '5'
        startsWith("six") -> '6'
        startsWith("seven") -> '7'
        startsWith("eight") -> '8'
        startsWith("nine") -> '9'
        else -> first()
    }
    // For overlapping (e.g. eightwo = 82) use `drop(1)`; for non-overlapping (e.g. eightwo = 8wo) use `drop(name.length)` to advance over the word.
    // Puzzle instructions don't indicate which version is correct - trial and error says they can overlap
    return digit + drop(1).mapOverlappingWordsToDigits()
}
