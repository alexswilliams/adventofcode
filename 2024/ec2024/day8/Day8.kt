package ec2024.day8

import common.*
import kotlin.math.*

private const val exampleInput = 13
private val example2Input = Triple(3, 5, 50L)
private val example3Input = Triple(2, 5, 160L)
private const val puzzleInput = 4099027
private val puzzle2Input = Triple(375, 1111, 20240000L)
private val puzzle3Input = Triple(904813, 10, 202400000L)

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzleInput) } // 530ns
    benchmark { part2(puzzle2Input) } // 10µs
    benchmark { part3(puzzle3Input) } //
}

internal object Day8 : Challenge {
    override fun assertCorrect() {
        check(21, "P1 Example") { part1(exampleInput) }
        check(6470302, "P1 Puzzle") { part1(puzzleInput) }

        check(27, "P2 Example") { part2(example2Input) }
        check(125560657, "P2 Puzzle") { part2(puzzle2Input) }

        check(2, "P3 Example") { part3(example3Input) }
        check(0, "P3 Puzzle") { part3(puzzle3Input) }
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
    throw Error("I don't understand the question")
}
