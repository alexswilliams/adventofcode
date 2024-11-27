package aoc2023.day9

import common.*


private val examples = loadFilesToLines("aoc2023/day9", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day9", "input.txt")


internal fun main() {
    Day9.assertCorrect()
    benchmark { part1(puzzles[0]) } // 285µs
    benchmark { part2(puzzles[0]) } // 248µs
}

internal object Day9 : Challenge {
    override fun assertCorrect() {
        check(114, "P1 Example") { part1(examples[0]) }
        check(1666172641, "P1 Puzzle") { part1(puzzles[0]) }

        check(2, "P2 Example") { part2(examples[0]) }
        check(933, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private fun part1(input: List<String>) = sumOfNextValues(input) { numbers, step -> numbers.last() + step }
private fun part2(input: List<String>) = sumOfNextValues(input) { numbers, step -> numbers.first() - step }

private fun sumOfNextValues(input: List<String>, findStepAbove: (List<Int>, Int) -> Int): Int =
    input.map { it.splitToInts(" ") }
        .sumOf { initial ->
            generateSequence(initial) { line -> if (line.all { it == 0 }) null else line.zipWithNext { a, b -> b - a } }
                .toList()
                .foldRight(0, findStepAbove)
        }
