package ec2024.day1

import common.*

private val examples = loadFiles("ec2024/day1", "example.txt", "example2.txt", "example3.txt")
private val puzzles = loadFiles("ec2024/day1", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzles[0]) } // 66µs
    benchmark { part2(puzzles[1]) } // 78µs
    benchmark { part2(puzzles[2]) } // 254µs
}

internal object Day1 : Challenge {
    override fun assertCorrect() {
        check(5, "P1 Example") { part1(examples[0]) }
        check(1321, "P1 Puzzle") { part1(puzzles[0]) }

        check(28, "P2 Example") { part2(examples[1]) }
        check(5643, "P2 Puzzle") { part2(puzzles[1]) }

        check(30, "P3 Example") { part3(examples[2]) }
        check(28175, "P3 Puzzle") { part3(puzzles[2]) }
    }
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

private fun baseValue(c: Char) = when (c) {
    'B' -> 1
    'C' -> 3
    'D' -> 5
    else -> 0
}
