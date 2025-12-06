package aoc2025.day6

import common.*

private val example = loadFilesToLines("aoc2025/day6", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day6", "input.txt").single()

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzle) } // 290.4µs
    benchmark { part2(puzzle) } // 318.7µs
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(4277556, "P1 Example") { part1(example) }
        check(4449991244405, "P1 Puzzle") { part1(puzzle) }

        check(3263827, "P2 Example") { part2(example) }
        check(9348430857627, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Long =
    input.splitOnSpaces().map { it.filterNotBlank() }
        .transpose()
        .sumOf { problem ->
            if (problem.last() == "*") problem.dropLast(1).productOf { it.toLong() }
            else problem.dropLast(1).sumOf { it.toLong() }
        }

private fun part2(input: List<String>): Long {
    val operations = input.last().withIndex().filter { it.value == '+' || it.value == '*' }
    val numbers = input.dropLast(1).map { line -> line.padEnd(input.maxOf { it.length }, ' ') }
        .transposedView()
        .map { digits -> digits.fold(0L) { acc, it -> if (it == ' ') acc else acc * 10L + it.digitToInt() } }

    return operations.plus(IndexedValue(numbers.lastIndex + 2, 'x'))
        .zipWithNext()
        .sumOf { (thisOp, nextOp) ->
            val numbersForOp = numbers.subList(thisOp.index, nextOp.index - 1)
            if (thisOp.value == '*') numbersForOp.product() else numbersForOp.sum()
        }
}
