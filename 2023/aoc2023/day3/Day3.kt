package aoc2023.day3

import common.benchmark
import common.fromClasspathFile
import common.linesAsCharArrays
import kotlin.test.assertEquals

private val exampleInput = "aoc2023/day3/example.txt".fromClasspathFile().linesAsCharArrays()
private val puzzleInput = "aoc2023/day3/input.txt".fromClasspathFile().linesAsCharArrays()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(4361, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(532445, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(467835, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(79842967, it) }

    benchmark { part1(puzzleInput) } // 547µs
    benchmark { part2(puzzleInput) } // 615µs
}

sealed interface ItemAtCoordinate
data class SymbolAtCoordinate(val symbol: Char, val row: Int, val col: Int) : ItemAtCoordinate
data class IntAtCoordinate(val value: Int, val row: Int, val colFirst: Int, val colLast: Int) : ItemAtCoordinate

private fun part1(input: List<CharArray>): Int {
    val (symbols, numbers) = tokeniseInput(input)
    return numbers
        .filter { number ->
            symbols.any {
                it.row in (number.row - 1..number.row + 1) &&
                        it.col in (number.colFirst - 1..number.colLast + 1)
            }
        }
        .sumOf { it.value }
}

private fun part2(input: List<CharArray>): Int {
    val (symbols, numbers) = tokeniseInput(input)
    return symbols
        .filter { it.symbol == '*' }
        .mapNotNull { gear ->
            val touchingNumbers = numbers.filter {
                gear.row in (it.row - 1..it.row + 1) && gear.col in (it.colFirst - 1..it.colLast + 1)
            }.map { it.value }
            if (touchingNumbers.size == 2) touchingNumbers[0] * touchingNumbers[1] else null
        }.sum()
}

private fun tokeniseInput(input: List<CharArray>): Pair<List<SymbolAtCoordinate>, List<IntAtCoordinate>> {
    val result = arrayListOf<SymbolAtCoordinate>() to arrayListOf<IntAtCoordinate>()
    input.forEachIndexed { row, line ->
        var value = 0
        var startsAt = -1
        var index = 0
        for (elem in line) {
            if (elem in '0'..'9') {
                value = value * 10 + elem.digitToInt()
                if (startsAt < 0) startsAt = index
            } else {
                if (startsAt >= 0) {
                    result.second.add(IntAtCoordinate(value, row, startsAt, index - 1))
                    value = 0
                    startsAt = -1
                }
                if (elem != '.') {
                    result.first.add(SymbolAtCoordinate(elem, row, index))
                }
            }
            index++
        }
        if (startsAt >= 0) result.second.add(IntAtCoordinate(value, row, startsAt, index - 1))
    }
    return result
}
