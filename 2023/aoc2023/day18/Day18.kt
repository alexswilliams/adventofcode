package aoc2023.day18

import common.*

private val examples = loadFilesToLines("aoc2023/day18", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day18", "input.txt")

internal fun main() {
    Day18.assertCorrect()
    benchmark { part1(puzzles[0]) } // 447µs
    benchmark { part2(puzzles[0]) } // 644µs
}

internal object Day18 : Challenge {
    override fun assertCorrect() {
        check(62, "P1 Example") { part1(examples[0]) }
        check(70026, "P1 Puzzle") { part1(puzzles[0]) }

        check(952408144115, "P2 Example") { part2(examples[0]) }
        check(68548301037382, "P2 Puzzle") { part2(puzzles[0]) }
    }
}


private fun part1(input: List<String>): Long =
    countFilledSquares(input.map { it.split(' ') }.map { it[0][0] to it[1].toInt() })

private fun part2(input: List<String>): Long =
    countFilledSquares(input.map { it.split(' ')[2] }.map { directions[it[7]]!! to it.substring(2, 7).toInt(16) })

private val directions = mapOf('0' to 'R', '1' to 'D', '2' to 'L', '3' to 'U')


private fun countFilledSquares(instructions: List<Pair<Char, Int>>): Long {
    val horizontal = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
    val verticalRanges = mutableListOf<Triple<Int, Char, IntRange>>()

    var pos = 0 to 0
    for ((direction, amount) in instructions) {
        when (direction) {
            'L' -> pos = pos.moveH(-amount, horizontal)
            'R' -> pos = pos.moveH(amount, horizontal)
            'U' -> pos = pos.moveV(-amount, verticalRanges)
            'D' -> pos = pos.moveV(amount, verticalRanges)
        }
    }

    val rowsOfInterest = horizontal.keys.sorted()
    val rowsWithHorizontals = rowsOfInterest
        .sumOf { rowNumber -> countFilledSquaresInRow(verticalRanges, horizontal, rowNumber) }
    val rowsWithOnlyVerticals = rowsOfInterest.zipWithNext().filterNot { it.first + 1 == it.second }.map { (a, b) -> (a + 1)..<b }
        .sumOf { rowRange -> rowRange.size * countFilledSquaresInRow(verticalRanges, horizontal, rowRange.first) }

    return rowsWithHorizontals + rowsWithOnlyVerticals
}

private fun countFilledSquaresInRow(verticalRanges: List<Triple<Int, Char, IntRange>>, horizontal: Map<Int, List<Pair<Int, Int>>>, rowNumber: Int): Long {
    val scans = verticalRanges.filter { rowNumber in it.third }.sortedBy { it.first }.zipWithNext()
    val horizontalRuns = horizontal[rowNumber].orEmpty()
    var nextIsInside = true
    var total = 0L
    for ((a, b) in scans) {
        when ("${a.second}${b.second}") {
            "DD", "UU" ->
                total += b.first - a.first

            "DU", "UD" -> {
                total += if (nextIsInside || a.first to b.first in horizontalRuns) b.first - a.first else 1
                nextIsInside = !nextIsInside
            }
        }
    }
    return total + 1
}

private fun Pair<Int, Int>.moveH(amount: Int, map: MutableMap<Int, MutableList<Pair<Int, Int>>>): Pair<Int, Int> {
    val next = (first + amount to second)
    map.addToListAtKey(second, if (amount < 0) next.first to first else first to next.first)
    return next
}

private fun Pair<Int, Int>.moveV(amount: Int, verticalRanges: MutableList<Triple<Int, Char, IntRange>>): Pair<Int, Int> {
    verticalRanges.add(Triple(first, if (amount < 0) 'U' else 'D', if (amount < 0) second + amount..second else second..second + amount))
    return first to second + amount
}

private fun <K, V> MutableMap<K, MutableList<V>>.addToListAtKey(key: K, value: V) {
    compute(key) { _, lst -> lst?.apply { add(value) } ?: mutableListOf(value) }
}
