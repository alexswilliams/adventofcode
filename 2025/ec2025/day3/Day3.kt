package ec2025.day3

import common.*

private val examples = loadFiles("ec2025/day3", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFiles("ec2025/day3", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day3.assertCorrect()
    benchmark { part1(puzzles[0]) } // 21.1µs
    benchmark { part2(puzzles[1]) } // 47.0µs
    benchmark(100) { part3(puzzles[2]) } // 735.9µs
}

internal object Day3 : Challenge {
    override fun assertCorrect() {
        check(29, "P1 Example") { part1(examples[0]) }
        check(2908, "P1 Puzzle") { part1(puzzles[0]) }

        check(781, "P2 Example") { part2(examples[1]) }
        check(272, "P2 Puzzle") { part2(puzzles[1]) }

        check(3, "P3 Example") { part3(examples[2]) }
        check(3122, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: String): Int =
    input.splitToInts(",").toSet().sum()

private fun part2(input: String): Int =
    input.splitToInts(",").toSortedSet().take(20).sum()

private fun part3(input: String): Int =
    input.splitToInts(",").groupingBy { it }.eachCount().values.max()
