package ec2025.day6

import common.*

private val examples = loadFiles("ec2025/day6", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFiles("ec2025/day6", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzles[0]) } // 2.3µs
    benchmark { part2(puzzles[1]) } // 3.7µs
//    benchmark(1) { part3(puzzles[2]) } // 27.6 SECONDS :(
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(5, "P1 Example") { part1(examples[0]) }
        check(138, "P1 Puzzle") { part1(puzzles[0]) }

        check(11, "P2 Example") { part2(examples[1]) }
        check(4084, "P2 Puzzle") { part2(puzzles[1]) }

        check(34, "P3 Example (1)") { part3(examples[2], distance = 10, repeats = 1) }
        check(72, "P3 Example (2)") { part3(examples[2], distance = 10, repeats = 2) }
        check(110, "P3 Example (3, mine)") { part3(examples[2], distance = 10, repeats = 3) }
        check(426, "P3 Example (12x10, mine)") { part3(examples[2], distance = 12, repeats = 10) }
        check(3796, "P3 Example (100, mine)") { part3(examples[2], distance = 10, repeats = 100) }
        check(3442321, "P3 Example (1000)") { part3(examples[2]) }
        check(1667539613, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: String, mentor: Char = 'A', novice: Char = 'a'): Int {
    var mentors = 0
    return input.fold(0) { acc, ch ->
        when (ch) {
            mentor -> acc.also { mentors++ }
            novice -> acc + mentors
            else -> acc
        }
    }
}

private fun part2(input: String): Int =
    part1(input) + part1(input, 'B', 'b') + part1(input, 'C', 'c')

private fun part3(input: String, distance: Int = 1000, repeats: Int = 1000): Int = input
    .repeat(repeats)
    .let { input ->
        input.mapIndexed { index, ch ->
            if (ch.isUpperCase()) 0
            else input
                .substring(((index - distance).coerceIn(input.indices)..((index + distance).coerceIn(input.indices))))
                .count { it.isUpperCase() && (it.lowercaseChar() == ch) }
        }.sum()
    }
