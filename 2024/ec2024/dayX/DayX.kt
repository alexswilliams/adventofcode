package ec2024.dayX

import common.*

private val examples = loadFilesToLines("ec2024/dayX", "example.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/dayX", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    DayX.assertCorrect()
    benchmark { part1(puzzles[0]) }
    benchmark { part2(puzzles[1]) }
    benchmark(100) { part3(puzzles[2]) }
}

internal object DayX : Challenge {
    override fun assertCorrect() {
        check(0, "P1 Example") { part1(examples[0]) }
        check(0, "P1 Puzzle") { part1(puzzles[0]) }

        check(0, "P2 Example") { part2(examples[1]) }
        check(0, "P2 Puzzle") { part2(puzzles[1]) }

        check(0, "P3 Example") { part3(examples[2]) }
        check(0, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): Int = 0

private fun part2(input: List<String>): Int = 0

private fun part3(input: List<String>): Int = 0
