package ec2024.day1

import common.ThreePartChallenge
import common.benchmark
import common.frequency
import common.fromClasspathFile
import kotlin.test.assertEquals

private val exampleInput = "ec2024/day1/example.txt".fromClasspathFile()
private val example2Input = "ec2024/day1/example2.txt".fromClasspathFile()
private val example3Input = "ec2024/day1/example3.txt".fromClasspathFile()
private val puzzleInput = "ec2024/day1/input.txt".fromClasspathFile()
private val puzzle2Input = "ec2024/day1/input2.txt".fromClasspathFile()
private val puzzle3Input = "ec2024/day1/input3.txt".fromClasspathFile()

fun main() {
    Day1.assertPart1Correct()
    Day1.assertPart2Correct()
    Day1.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 66µs
    benchmark { part2(puzzle2Input) } // 78µs
    benchmark { part2(puzzle3Input) } // 254µs
}

object Day1 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(5, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(1321, it) }
    }

    override fun assertPart2Correct() {
        part2(example2Input).also { println("[Example] Part 2: $it") }.also { assertEquals(28, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(5643, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(30, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(28175, it) }
    }
}

private fun baseValue(c: Char) = when (c) {
    'B' -> 1
    'C' -> 3
    'D' -> 5
    else -> 0
}

private fun part1(input: String): Int =
    input.frequency().sumOf { it.second * baseValue(it.first) }

private fun part2(input: String): Int =
    input.chunked(2).sumOf { baseValue(it[0]) + baseValue(it[1]) + if (it.contains('x')) 0 else 2 }

private fun part3(input: String): Int =
    input.chunked(3).sumOf {
        baseValue(it[0]) + baseValue(it[1]) + baseValue(it[2]) + when (it.count { it == 'x' }) {
            0 -> 6
            1 -> 2
            else -> 0
        }
    }
