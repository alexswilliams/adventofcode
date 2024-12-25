package aoc2024.day25

import common.*

private val example = loadFilesToLines("aoc2024/day25", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day25", "input.txt").single()

internal fun main() {
    Day25.assertCorrect()
    benchmark { part1(puzzle) } // 563µs
}

internal object Day25 : Challenge {
    override fun assertCorrect() {
        check(3, "P1 Example") { part1(example) }
        check(3155, "P1 Puzzle") { part1(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val (locks, keys) = input.windowed(7, 8).partition { it[0][0] == '#' }
    val lockNumbers = locks.map { schematicToNumberList(it) }
    val keyNumbers = keys.map { schematicToNumberList(it) }

    return countCartesianProductOf(lockNumbers, keyNumbers) { lock, key ->
        lock.allZipped(key) { lockPin, keyPin -> lockPin + keyPin <= 5 }
    }
}

private fun schematicToNumberList(schematic: List<String>) =
    schematic.transposedView().map { pin -> pin.count { it == '#' } - 1 }.toIntArray()
