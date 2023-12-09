package aoc2023.day9

import common.*
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day9/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day9/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(114, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(1666172641, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(2, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(933, it) }
    benchmark { part1(puzzleInput) } // 801µs
    benchmark { part2(puzzleInput) } // 548µs
}

private fun part1(input: List<String>) = sumOfNextValues(input) { ints, step -> ints.last() + step }
private fun part2(input: List<String>) = sumOfNextValues(input) { ints, step -> ints.first() - step }

private fun sumOfNextValues(input: List<String>, findStepAbove: (List<Int>, Int) -> Int): Int =
    input.map { it.splitToInts(" ") }
        .sumOf { startLine ->
            val differences = mutableListOf(startLine)
            while (differences.last().any { it != 0 }) {
                differences.add(differences.last().zipWithNext { a, b -> b - a })
            }
            differences.foldRight(0, findStepAbove)
        }
