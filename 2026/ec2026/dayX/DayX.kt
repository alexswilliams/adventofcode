package ec2026.dayX

import common.Challenge
import common.benchmark
import common.loadFiles

private val example = loadFiles("ec2026/dayX", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFiles("ec2026/dayX", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzle[0]) } // µs
    benchmark { part2(puzzle[1]) } // µs
    benchmark { part3(puzzle[2]) } // µs
}

internal object Day1 : Challenge {
    override fun assertCorrect() {
        check(0, "P1 Example") { part1(example[0]) }
        check(0, "P1 Puzzle") { part1(puzzle[0]) }

        check(0, "P2 Example") { part2(example[1]) }
        check(0, "P2 Puzzle") { part2(puzzle[1]) }

        check(0, "P3 Example") { part3(example[2]) }
        check(0, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: String): Int = 0


private fun part2(input: String): Int = 0


private fun part3(input: String): Int = 0
