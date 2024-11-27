package aoc2023.day4

import common.*

private val examples = loadFilesToLines("aoc2023/day4", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day4", "input.txt")

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzles[0]) } // 528µs
    benchmark { part2(puzzles[0]) } // 476µs
}

internal object Day4 : Challenge {
    override fun assertCorrect() {
        check(13, "P1 Example") { part1(examples[0]) }
        check(23678, "P1 Puzzle") { part1(puzzles[0]) }

        check(30, "P2 Example") { part2(examples[0]) }
        check(15455663, "P2 Puzzle") { part2(puzzles[0]) }
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
