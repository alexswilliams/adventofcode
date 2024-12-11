package aoc2024.day1

import common.*
import kotlin.math.*

private val example = loadFilesToLines("aoc2024/day1", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day1", "input.txt").single()

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzle) } // 385µs
    benchmark { part2(puzzle) } // 464µs
}

internal object Day1 : Challenge {
    override fun assertCorrect() {
        check(11, "P1 Example") { part1(example) }
        check(2086478, "P1 Puzzle") { part1(puzzle) }

        check(31, "P2 Example") { part2(example) }
        check(24941624, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int =
    input.asTwoLists()
        .map { list -> list.sorted() }
        .let { lists -> lists[0].zip(lists[1]) }
        .sumOf { (left, right) -> (left - right).absoluteValue }

private fun part2(input: List<String>): Int =
    input.asTwoLists().let { (leftList, rightList) ->
        leftList.sumOf { number -> number * rightList.count { it == number } }
    }

private fun List<String>.asTwoLists() = this.map { it.splitToInts("   ") }.transpose()
