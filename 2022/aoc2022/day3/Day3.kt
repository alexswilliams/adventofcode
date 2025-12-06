package aoc2022.day3

import common.*

private val example = loadFilesToLines("aoc2022/day3", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day3", "input.txt").single()

internal fun main() {
    Day3.assertCorrect()
    benchmark { part1(puzzle) } // 52.2µs
    benchmark { part2(puzzle) } // 37.9µs
}

internal object Day3 : Challenge {
    override fun assertCorrect() {
        check(157, "P1 Example") { part1(example) }
        check(7716, "P1 Puzzle") { part1(puzzle) }

        check(70, "P2 Example") { part2(example) }
        check(2973, "P2 Puzzle") { part2(puzzle) }
    }
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
