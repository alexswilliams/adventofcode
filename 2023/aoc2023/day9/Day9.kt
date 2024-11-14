package aoc2023.day9

import common.TwoPartChallenge
import common.benchmark
import common.fromClasspathFileToLines
import common.splitToInts
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day9/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day9/input.txt".fromClasspathFileToLines()

internal fun main() {
    Day9.assertPart1Correct()
    Day9.assertPart2Correct()
    benchmark { part1(puzzleInput) } // 285µs
    benchmark { part2(puzzleInput) } // 248µs
}

internal object Day9 : TwoPartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(114, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(1666172641, it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(2, it) }
        part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(933, it) }
    }
}

private fun part1(input: List<String>) = sumOfNextValues(input) { numbers, step -> numbers.last() + step }
private fun part2(input: List<String>) = sumOfNextValues(input) { numbers, step -> numbers.first() - step }

private fun sumOfNextValues(input: List<String>, findStepAbove: (List<Int>, Int) -> Int): Int =
    input.map { it.splitToInts(" ") }
        .sumOf { initial ->
            generateSequence(initial) { line -> if (line.all { it == 0 }) null else line.zipWithNext { a, b -> b - a } }
                .toList()
                .foldRight(0, findStepAbove)
        }
