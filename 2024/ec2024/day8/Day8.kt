package ec2024.day8

import common.*
import kotlin.math.*
import kotlin.test.*

private const val exampleInput = 13
private val example2Input = Triple(3, 5, 50L)
private val example3Input = Triple(2, 5, 160L)
private const val puzzleInput = 4099027
private val puzzle2Input = Triple(375, 1111, 20240000L)
private val puzzle3Input = Triple(904813, 10, 202400000L)

internal fun main() {
    Day8.assertPart1Correct()
    Day8.assertPart2Correct()
    Day8.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 530ns
    benchmark { part2(puzzle2Input) } // 10µs
    benchmark { part3(puzzle3Input) } //
}

internal object Day8 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(21, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(6470302, it) }
    }

    override fun assertPart2Correct() {
        part2(example2Input).also { println("[Example] Part 2: $it") }.also { assertEquals(27, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(125560657, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(2, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(0, it) }
    }
}


private fun part1(input: Int): Int =
    (sqrt(input.toFloat()).toInt() + 1).let { n -> (n * n - input) * (2 * n - 1) }

private fun part2(input: Triple<Int, Int, Long>): Long {
    val (step, modulus, supplied) = input

    val thicknesses = generateSequence(1) { prev -> (prev * step) % modulus }
    var remaining = supplied
    thicknesses.forEachIndexed { layer, thickness ->
        remaining -= thickness * (2 * layer + 1)
        if (remaining < 0)
            return (2 * layer + 1) * -remaining
    }
    throw Error()
}

private fun part3(input: Triple<Int, Int, Long>): Long {
    val (step, modulus, supplied) = input

    val thicknesses = generateSequence(1) { prev -> (prev * step) % modulus + modulus }
    val emptyCount = { height: Int, width: Int -> (((step * width) % modulus) * height) % modulus }
    throw Error()
}
