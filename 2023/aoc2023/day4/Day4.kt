package aoc2023.day4

import common.TwoPartChallenge
import common.benchmark
import common.filterNotBlank
import common.fromClasspathFileToLines
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day4/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day4/input.txt".fromClasspathFileToLines()

fun main() {
    Day4.assertPart1Correct()
    Day4.assertPart2Correct()
    benchmark { part1(puzzleInput) } // 528µs
    benchmark { part2(puzzleInput) } // 476µs
}

object Day4 : TwoPartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(13, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(23678, it) }
    }
    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(30, it) }
        part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(15455663, it) }
    }
}


private fun part1(input: List<String>) = winsPerCard(input).sumOf { if (it == 0) 0 else 1 shl (it - 1) }

private fun part2(input: List<String>): Int {
    val cardMultiplier = IntArray(input.size) { 1 }
    winsPerCard(input).forEachIndexed { index, winsForCard ->
        (index + 1..index + winsForCard).forEach {
            cardMultiplier[it] += cardMultiplier[index]
        }
    }
    return cardMultiplier.sum()
}

private fun winsPerCard(input: List<String>) = input.map { line ->
    val parts = line.split(": ", " | ")
    val winningNumbers = parts[1].split(' ').filterNotBlank()
    parts[2].split(' ').filterNotBlank()
        .count { it in winningNumbers }
}
