package ec2026.day1

import common.Challenge
import common.benchmark
import common.loadFiles
import common.splitToInts
import kotlin.math.sqrt

private val example = loadFiles("ec2026/day1", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFiles("ec2026/day1", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzle[0]) } // 16.8µs
    benchmark { part2(puzzle[1]) } // 22.5µs
    benchmark { part3(puzzle[2]) } // 43.1µs
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
        val lowerBound = sqrt(remainingPineCones.toDouble()).toInt()
        val realSize = if (remainingPineCones <= lowerBound * lowerBound) lowerBound else lowerBound + 1
        realSize * realSize - remainingPineCones
    }


private fun part2(input: String): Int = input
    .splitToInts(",")
    .sumOf { remainingPineCones ->
        val lowerBound = remainingPineCones / 4 + 1
        val realSize = lowerBound
            .coerceAtLeast(3)
            .let { if (remainingPineCones <= 4 * it - 4) it else it + 1 }
        4 * realSize - 4 - remainingPineCones
    }


private fun part3(input: String): Int = input
    .splitToInts(",")
    .sumOf { remainingPineCones ->
        val lowerBound = sqrt(remainingPineCones.toDouble() + 1.0).toInt()
        val realSize = lowerBound
            .coerceAtLeast(3)
            .nextOdd()
            .let { if (remainingPineCones <= it * it - 1) it else it + 2 }
        realSize * realSize - 1 - remainingPineCones
    }


private fun Int.nextOdd(): Int = this + (this + 1) % 2
