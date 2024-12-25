package aoc2024.day25

import common.*

private val example = loadFilesToLines("aoc2024/day25", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day25", "input.txt").single()

internal fun main() {
    Day25.assertCorrect()
    benchmark { part1(puzzle) } // 719µs
}

internal object Day25 : Challenge {
    override fun assertCorrect() {
        check(3, "P1 Example") { part1(example) }
        check(3155, "P1 Puzzle") { part1(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val (locks, keys) = input.windowed(7, 8).map { it.transposeToStrings() }.partition { it[0][0] == '#' }
    val lockNumbers = schematicToNumberList(locks)
    val keyNumbers = schematicToNumberList(keys)
    var count = 0
    forEachCartesianProductOf(lockNumbers, keyNumbers) { lock, key -> if (lock.indices.all { lock[it] + key[it] <= 5 }) count++ }
    return count
}

private fun schematicToNumberList(schematics: List<List<String>>) =
    schematics.map { device -> device.map { pin -> pin.count { it == '#' } - 1 } }
