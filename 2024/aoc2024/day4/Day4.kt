package aoc2024.day4

import common.*

private val example = loadFilesToLines("aoc2024/day4", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day4", "input.txt").single()

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzle) } // 570µs
    benchmark { part2(puzzle) } // 195µs
}

internal object Day4 : Challenge {
    override fun assertCorrect() {
        check(18, "P1 Example") { part1(example) }
        check(2575, "P1 Puzzle") { part1(puzzle) }

        check(9, "P2 Example") { part2(example) }
        check(2041, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int =
    input.sumOf { it.countOccurrences("XMAS") + it.reversed().countOccurrences("XMAS") } +
            input.transposeToStrings().sumOf { it.countOccurrences("XMAS") + it.reversed().countOccurrences("XMAS") } +
            input.asArrayOfCharArrays().mapCartesianNotNull { row, col, char -> if (char == 'X') row by16 col else null }.sumOf {
                (if (it.row() >= 3 && it.col() >= 3 && input[it.row() - 1][it.col() - 1] == 'M' && input[it.row() - 2][it.col() - 2] == 'A' && input[it.row() - 3][it.col() - 3] == 'S') 1 else 0) +
                        (if (it.row() >= 3 && it.col() < input[0].length - 3 && input[it.row() - 1][it.col() + 1] == 'M' && input[it.row() - 2][it.col() + 2] == 'A' && input[it.row() - 3][it.col() + 3] == 'S') 1 else 0) +
                        (if (it.row() < input.size - 3 && it.col() >= 3 && input[it.row() + 1][it.col() - 1] == 'M' && input[it.row() + 2][it.col() - 2] == 'A' && input[it.row() + 3][it.col() - 3] == 'S') 1 else 0) +
                        if (it.row() < input.size - 3 && it.col() < input[0].length - 3 && input[it.row() + 1][it.col() + 1] == 'M' && input[it.row() + 2][it.col() + 2] == 'A' && input[it.row() + 3][it.col() + 3] == 'S') 1 else 0
            }

private fun part2(input: List<String>): Int =
    input.asArrayOfCharArrays().mapCartesianNotNull { row, col, char -> if (char == 'A' && row in 1..input.size - 2 && col in 1..input.size - 2) row by16 col else null }
        .count {
            val leftUp = if (input[it.row() - 1][it.col() - 1] == 'M' && input[it.row() + 1][it.col() + 1] == 'S') 1 else 0
            val rightUp = if (input[it.row() - 1][it.col() + 1] == 'M' && input[it.row() + 1][it.col() - 1] == 'S') 1 else 0
            val leftDown = if (input[it.row() + 1][it.col() - 1] == 'M' && input[it.row() - 1][it.col() + 1] == 'S') 1 else 0
            val rightDown = if (input[it.row() + 1][it.col() + 1] == 'M' && input[it.row() - 1][it.col() - 1] == 'S') 1 else 0
            leftUp + rightUp + leftDown + rightDown == 2
        }
