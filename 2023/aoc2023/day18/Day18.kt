package aoc2023.day18

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("aoc2023/day18", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day18", "input.txt")

internal fun main() {
    Day18.assertCorrect()
    benchmark(100) { part1(puzzles[0]) } // 418µs
    benchmark(1) { part2(puzzles[0]) } // 8.3s :(
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
    val vertical = mutableMapOf<Int, MutableList<Pair<Int, Char>>>()
    val horizontal = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()

    var pos = 0 to 0
    for ((direction, amount) in instructions) {
        when (direction) {
            'L' -> pos = pos.moveH(-amount, horizontal)
            'R' -> pos = pos.moveH(amount, horizontal)
            'U' -> pos = pos.moveV(-amount, vertical)
            'D' -> pos = pos.moveV(amount, vertical)
        }
    }

    return vertical.entries.sumOf { (rowIndex, row) ->
        val scans = row.sortedBy { it.first }.zipWithNext()
        val horizontalRuns = horizontal[rowIndex].orEmpty()
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
        total + 1
    }
}

private fun Pair<Int, Int>.moveH(amount: Int, map: MutableMap<Int, MutableList<Pair<Int, Int>>>): Pair<Int, Int> =
    (first + amount to second)
        .also { map.addToListAtKey(second, if (amount < 0) it.first to first else first to it.first) }

private fun Pair<Int, Int>.moveV(amount: Int, map: MutableMap<Int, MutableList<Pair<Int, Char>>>): Pair<Int, Int> {
    repeat(abs(amount) + 1) { r -> map.addToListAtKey(second + r * amount.sign, first to (if (amount < 0) 'U' else 'D')) }
    return first to second + amount
}

private fun <K, V> MutableMap<K, MutableList<V>>.addToListAtKey(key: K, value: V) {
    compute(key) { _, lst -> lst?.apply { add(value) } ?: mutableListOf(value) }
}
