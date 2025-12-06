package aoc2022.day6

import common.*
import kotlin.test.*

private val example = loadFiles("aoc2022/day6", "example1.txt", "example2.txt", "example3.txt", "example4.txt", "example5.txt")
private val puzzle = loadFiles("aoc2022/day6", "input.txt").single()

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzle) } // 122.9µs
    benchmark { part2(puzzle) } // 541.9µs
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(7, "P1 Example 1") { part1(example[0]) }
        check(5, "P1 Example 2") { part1(example[1]) }
        check(6, "P1 Example 3") { part1(example[2]) }
        check(10, "P1 Example 4") { part1(example[3]) }
        check(11, "P1 Example 5") { part1(example[4]) }
        check(1142, "P1 Puzzle") { part1(puzzle) }

        check(19, "P2 Example 1") { part2(example[0]) }
        check(23, "P2 Example 2") { part2(example[1]) }
        check(23, "P2 Example 3") { part2(example[2]) }
        check(29, "P2 Example 4") { part2(example[3]) }
        check(26, "P2 Example 5") { part2(example[4]) }
        check(2803, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: String) = solveForRunLength(input, 4)
private fun part2(input: String) = solveForRunLength(input, 14)

private fun solveForRunLength(input: String, runLength: Int) = input
    .windowedSequence(runLength, 1).withIndex()
    .first { it.value.toSet().size == it.value.length }
    .index + runLength
