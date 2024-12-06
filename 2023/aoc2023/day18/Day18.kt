package aoc2023.day18

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("aoc2023/day18", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day18", "input.txt")

internal fun main() {
    Day18.assertCorrect()
    benchmark(100) { part1(puzzles[0]) } // 898µs
    benchmark(1) { part2(puzzles[0]) } // 10s :(
}

internal object Day18 : Challenge {
    override fun assertCorrect() {
        check(62, "P1 Example") { part1(examples[0]) }
        check(70026, "P1 Puzzle") { part1(puzzles[0]) }

        check(952408144115, "P2 Example") { part2(examples[0]) }
        check(68548301037382, "P2 Puzzle") { part2(puzzles[0]) }
    }
}


private fun part1(input: List<String>): Long {
    val instructions = input.map { it.split(' ') }.map { it[0][0] to it[1].toInt() }
    return countFilledSquares(instructions)
}

private fun part2(input: List<String>): Long {
    val instructions = input.map { it.split(' ')[2] }.map { directions[it[7]]!! to it.substring(2, 7).toInt(16) }
    return countFilledSquares(instructions)
}

private val directions = mapOf('0' to 'R', '1' to 'D', '2' to 'L', '3' to 'U')


private fun countFilledSquares(instructions: List<Pair<Char, Int>>): Long {
    val up = mutableMapOf<Int, MutableList<Int>>()
    val down = mutableMapOf<Int, MutableList<Int>>()
    val horizontal = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()

    fun Pair<Int, Int>.moveH(amount: Int, map: MutableMap<Int, MutableList<Pair<Int, Int>>>) =
        (first + amount to second)
            .also { map.addToListAtKey(second, if (amount < 0) it.first to first else first to it.first) }

    fun Pair<Int, Int>.moveV(amount: Int, map: MutableMap<Int, MutableList<Int>>): Pair<Int, Int> {
        repeat(abs(amount) + 1) { r -> map.addToListAtKey(second + r * amount.sign, first) }
        return first to second + amount
    }

    var pos = 0 to 0
    for ((direction, amount) in instructions) {
        when (direction) {
            'L' -> pos = pos.moveH(-amount, horizontal)
            'R' -> pos = pos.moveH(amount, horizontal)
            'U' -> pos = pos.moveV(-amount, up)
            'D' -> pos = pos.moveV(amount, down)
        }
    }

    return (up.keys union down.keys).sumOf { row ->
        val scans = (up[row].orEmpty().map { it to "U" } + down[row].orEmpty().map { it to "D" }).sortedBy { it.first }.zipWithNext()
        val horizontalRuns = horizontal[row].orEmpty()
        var nextIsInside = true
        var total = 0L
        for ((a, b) in scans) {
            when (a.second + b.second) {
                "DD", "UU" -> total += b.first - a.first
                "DU", "UD" -> {
                    total +=
                        if (nextIsInside) b.first - a.first
                        else if (a.first to b.first in horizontalRuns) b.first - a.first
                        else 1
                    nextIsInside = !nextIsInside
                }
            }
        }
        total + 1
    }
}

private fun <K, V> MutableMap<K, MutableList<V>>.addToListAtKey(key: K, value: V) {
    compute(key) { _, lst -> lst?.apply { add(value) } ?: mutableListOf(value) }
}
