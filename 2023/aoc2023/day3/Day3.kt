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

    benchmark { part1(puzzleInput) } // 530µs
    benchmark { part2(puzzleInput) } // 542µs
}

private sealed interface ItemAtCoordinate
private data class SymbolAtCoordinate(val symbol: Char, val row: Int, val col: Int) : ItemAtCoordinate
private data class IntAtCoordinate(val value: Int, val row: Int, val colFirst: Int, val colLast: Int) :
    ItemAtCoordinate {
    fun isAdjacentTo(symbol: SymbolAtCoordinate): Boolean =
        symbol.row in (row - 1..row + 1) && symbol.col in (colFirst - 1..colLast + 1)
}

private fun part1(input: List<CharArray>): Int {
    val (symbols, numbers) = tokeniseInput(input)
    return numbers
        .filter { number ->
            symbols.any {
                number.isAdjacentTo(it)
            }
        }
        .sumOf { it.value }
}

private fun part2(input: List<CharArray>): Int {
    val (symbols, numbers) = tokeniseInput(input)
    return symbols
        .filter { it.symbol == '*' }
        .sumOf { gear ->
            val touchingNumbers = numbers.filter {
                it.isAdjacentTo(gear)
            }
            if (touchingNumbers.size == 2)
                touchingNumbers[0].value * touchingNumbers[1].value
            else 0
        }
}

private fun tokeniseInput(input: List<CharArray>): Pair<List<SymbolAtCoordinate>, List<IntAtCoordinate>> {
    val result = ArrayList<SymbolAtCoordinate>(1000) to ArrayList<IntAtCoordinate>(1500)
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
                if (elem != '.') result.first.add(SymbolAtCoordinate(elem, row, index))
            }
            index++
        }
        if (startsAt >= 0) result.second.add(IntAtCoordinate(value, row, startsAt, index - 1))
    }
    return result
}
