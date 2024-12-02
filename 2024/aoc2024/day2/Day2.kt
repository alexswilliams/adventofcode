package aoc2024.day2

import common.*

private val example = loadFilesToLines("aoc2024/day2", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day2", "input.txt").single()

internal fun main() {
    Day2.assertCorrect()
    benchmark { part1(puzzle) } // 134µs
    benchmark { part2(puzzle) } // 308µs
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check(2, "P1 Example") { part1(example) }
        check(534, "P1 Puzzle") { part1(puzzle) }

        check(4, "P2 Example") { part2(example) }
        check(577, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int = input.map { it.splitToInts(" ") }.count { isSafe(it) }

private fun part2(input: List<String>): Int = input.map { it.splitToInts(" ") }.count { line ->
    val pairs = line.zipWithNext()
    val unsafeIndexAsc = pairs.indexOfFirst { !ascending(it) }
    val unsafeIndexDesc = pairs.indexOfFirst { !descending(it) }
    if (unsafeIndexAsc == -1 || unsafeIndexDesc == -1)
        true
    else sequenceOf(
        { line.subList(0, unsafeIndexAsc) + line.subList(unsafeIndexAsc + 1, line.size) },
        { line.subList(0, unsafeIndexAsc + 1) + line.subList(unsafeIndexAsc + 2, line.size) },
        { line.subList(0, unsafeIndexDesc) + line.subList(unsafeIndexDesc + 1, line.size) },
        { line.subList(0, unsafeIndexDesc + 1) + line.subList(unsafeIndexDesc + 2, line.size) }
    ).any { newPairs -> isSafe(newPairs()) }
}

private fun isSafe(line: List<Int>) = with(line.zipWithNext()) { all { ascending(it) } || all { descending(it) } }
private fun descending(it: Pair<Int, Int>) = (it.first - it.second) in -3..-1
private fun ascending(it: Pair<Int, Int>) = (it.first - it.second) in 1..3
