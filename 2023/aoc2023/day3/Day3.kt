package aoc2023.day3

import common.*

private val examples = loadFilesToGrids("aoc2023/day3", "example.txt")
private val puzzles = loadFilesToGrids("aoc2023/day3", "input.txt")

internal fun main() {
    Day3.assertCorrect()
    benchmark { part1(puzzles[0]) } // 140µs
    benchmark { part2(puzzles[0]) } // 122µs
}

internal object Day3 : Challenge {
    override fun assertCorrect() {
        check(4361, "P1 Example") { part1(examples[0]) }
        check(532445, "P1 Puzzle") { part1(puzzles[0]) }

        check(467835, "P2 Example") { part2(examples[0]) }
        check(79842967, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private sealed interface ItemAtCoordinate
private data class SymbolAtCoordinate(val symbol: Char, val row: Int, val col: Int) : ItemAtCoordinate
private data class IntAtCoordinate(val value: Int, val row: Int, val colFirst: Int, val colLast: Int) :
    ItemAtCoordinate {
    fun isColumnAdjacentTo(symbol: SymbolAtCoordinate) = symbol.col in (colFirst - 1..colLast + 1)
}

private fun part1(input: Grid): Int {
    val (symbols, numbers) = tokeniseInput(input)
    val symbolsBoundingRow = Array(symbols.size) { row ->
        symbols[row] +
                (if (row > 0) symbols[row - 1] else listOf()) +
                (if (row < symbols.lastIndex) symbols[row + 1] else listOf())
    }
    return numbers.asSequence().flatten()
        .filter { number -> symbolsBoundingRow[number.row].any { number.isColumnAdjacentTo(it) } }
        .sumOf { it.value }
}

private fun part2(input: Grid): Int {
    val (symbols, numbers) = tokeniseInput(input)
    val numbersBoundingRow = Array(numbers.size) { row ->
        numbers[row] +
                (if (row > 0) numbers[row - 1] else listOf()) +
                (if (row < numbers.lastIndex) numbers[row + 1] else listOf())
    }
    return symbols.asSequence().flatten()
        .filter { it.symbol == '*' }
        .sumOf { gear ->
            val touchingNumbers = numbersBoundingRow[gear.row].filter { it.isColumnAdjacentTo(gear) }
            if (touchingNumbers.size == 2) touchingNumbers[0].value * touchingNumbers[1].value
            else 0
        }
}

private fun tokeniseInput(input: Grid): Pair<List<List<SymbolAtCoordinate>>, List<List<IntAtCoordinate>>> {
    val symbols = List<MutableList<SymbolAtCoordinate>>(input.size) { ArrayList(15) }
    val numbers = List<MutableList<IntAtCoordinate>>(input.size) { ArrayList(15) }
    input.forEachIndexed { row, line ->
        var value = 0
        var startsAt = -1
        line.forEachIndexed { col, elem ->
            if (elem in '0'..'9') {
                value = value * 10 + elem.digitToInt()
                if (startsAt < 0) startsAt = col
            } else {
                if (elem != '.') symbols[row].add(SymbolAtCoordinate(elem, row, col))
                if (startsAt >= 0) {
                    numbers[row].add(IntAtCoordinate(value, row, startsAt, col - 1))
                    value = 0
                    startsAt = -1
                }
            }
        }
        if (startsAt >= 0) numbers[row].add(IntAtCoordinate(value, row, startsAt, line.lastIndex))
    }
    return symbols to numbers
}
