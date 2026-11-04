package ec2026.day1

import common.Challenge
import common.benchmark
import common.loadFiles
import common.splitToInts
import kotlin.math.sqrt
import kotlin.math.truncate

private val example = loadFiles("ec2026/day1", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFiles("ec2026/day1", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzle[0]) } // 19.7µs
    benchmark { part2(puzzle[1]) } // 23.2µs
    benchmark { part2(puzzle[2]) } // 35.1µs
}

internal object Day1 : Challenge {
    override fun assertCorrect() {
        check(11, "P1 Example") { part1(example[0]) }
        check(1185, "P1 Puzzle") { part1(puzzle[0]) }

        check(9, "P2 Example") { part2(example[1]) }
        check(293, "P2 Puzzle") { part2(puzzle[1]) }

        check(61, "P3 Example") { part3(example[2]) }
        check(645421, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: String): Int = input
    .splitToInts(",")
    .sumOf { remainingPineCones ->
        val lowerBound = truncate(sqrt(remainingPineCones.toDouble())).toInt()
        if (remainingPineCones == lowerBound * lowerBound) 0
        else (lowerBound + 1) * (lowerBound + 1) - remainingPineCones
    }


private fun part2(input: String): Int = input
    .splitToInts(",")
    .sumOf { remainingPineCones ->
        val lowerBound = truncate(remainingPineCones.toDouble() / 4.0 + 1.0).toInt()
        if (lowerBound <= 2) {
            return@sumOf 4 * 2 - remainingPineCones
        }
        if (remainingPineCones == 4 * (lowerBound - 1)) 0
        else 4 * (lowerBound + 1 - 1) - remainingPineCones
    }


private fun part3(input: String): Int = input
    .splitToInts(",")
    .sumOf { remainingPineCones ->
        val lowerBound = truncate(sqrt(remainingPineCones.toDouble() + 1.0)).toInt()
        if (lowerBound <= 2) {
            return@sumOf 3 * 3 - 1 - remainingPineCones
        }
        if (lowerBound % 2 == 0) {
            return@sumOf (lowerBound + 1) * (lowerBound + 1) - 1 - remainingPineCones
        }
        if (remainingPineCones == lowerBound * lowerBound - 1) 0
        else (lowerBound + 2) * (lowerBound + 2) - 1 - remainingPineCones
    }

