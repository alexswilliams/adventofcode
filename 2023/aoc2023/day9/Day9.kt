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
    benchmark { part1(puzzleInput) } // 557µs
    benchmark { part2(puzzleInput) } // 381µs
}

private fun part1(input: List<String>) = sumOfNextValues(input) { (_, last), step -> last + step }
private fun part2(input: List<String>) = sumOfNextValues(input) { (first, _), step -> first - step }

private fun sumOfNextValues(input: List<String>, findStepAbove: (Pair<Int, Int>, Int) -> Int): Int =
    input.map { it.splitToInts(" ") }
        .sumOf { initial ->
            generateSequence(initial) { line -> if (line.all { it == 0 }) null else line.zipWithNext { a, b -> b - a } }
                .map { it.first() to it.last() }
                .toList()
                .foldRight(0, findStepAbove)
        }
