package ec2025.day6

import common.*

private val examples = loadFilesToLines("ec2025/day6", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day6", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzles[0]) } // 23.0µs
    benchmark { part2(puzzles[1]) } // 73.5µs
    benchmark(1) { part3(puzzles[2]) } // 27.6 SECONDS :(
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(5, "P1 Example") { part1(examples[0]) }
        check(138, "P1 Puzzle") { part1(puzzles[0]) }

        check(11, "P2 Example") { part2(examples[1]) }
        check(4084, "P2 Puzzle") { part2(puzzles[1]) }

        check(34, "P3 Example (1)") { part3(examples[2], distance = 10, repeats = 1) }
        check(72, "P3 Example (2)") { part3(examples[2], distance = 10, repeats = 2) }
        check(3442321, "P3 Example (1000)") { part3(examples[2]) }
        check(1667539613, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): Int = input[0]
    .filter { c -> c == 'a' || c == 'A' }
    .runningFold("") { acc, ch -> acc + ch }
    .filter { it.endsWith("a") }
    .sumOf { it.count { c -> c == 'A' } }

private fun part2(input: List<String>): Int = input[0]
    .runningFold("") { acc, ch -> acc + ch }
    .filter { it.isNotEmpty() }
    .filter { it.last().isLowerCase() }
    .sumOf { it.count { c -> c == it.last().uppercaseChar() } }

private fun part3(input: List<String>, distance: Int = 1000, repeats: Int = 1000): Int = input[0]
    .repeat(repeats)
    .let { input ->
        input.mapIndexed { index, ch ->
            if (ch.isUpperCase()) 0
            else input
                .substring(((index - distance).coerceIn(input.indices)..((index + distance).coerceIn(input.indices))))
                .count { it.isUpperCase() && (it.lowercaseChar() == ch) }
        }.sum()
    }
