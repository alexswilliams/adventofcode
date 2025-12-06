package aoc2025.day5

import common.*

private val example = loadFilesToLines("aoc2025/day5", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day5", "input.txt").single()

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzle) } // 136.9µs
    benchmark { part2(puzzle) } // 961.2µs
}

internal object Day5 : Challenge {
    override fun assertCorrect() {
        check(3, "P1 Example") { part1(example) }
        check(798, "P1 Puzzle") { part1(puzzle) }

        check(14, "P2 Example") { part2(example) }
        check(366181852921027, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val (freshRanges, available) = input.partitionOnLineBreak(
        { ranges -> ranges.map { it.splitToLongs("-").let { (lo, hi) -> lo..hi } } },
        { ids -> ids.map { it.toLong() } }
    )
    return available.count { freshRanges.any { range -> range.contains(it) } }
}

private fun part2(input: List<String>): Long =
    input.takeWhile { it.isNotBlank() }
        .map { it.splitToLongs("-").let { (lo, hi) -> lo..hi } }
        .mergeAdjacentLongRanges()
        .sumOf { it.size }
