package aoc2024.day19

import common.*

private val example = loadFilesToLines("aoc2024/day19", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day19", "input.txt").single()

internal fun main() {
    Day19.assertCorrect()
    benchmark(100) { part1(puzzle) } // 4.2ms
    benchmark(100) { part2(puzzle) } // 10.4ms
}

internal object Day19 : Challenge {
    override fun assertCorrect() {
        check(6, "P1 Example") { part1(example) }
        check(260, "P1 Puzzle") { part1(puzzle) }

        check(16, "P2 Example") { part2(example) }
        check(639963796864990L, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val towels = input.first().split(", ").sorted().toTypedArray()
    return input.subList(2, input.size).count { pattern -> isSolvable(pattern, towels) }
}

private fun part2(input: List<String>): Long {
    val towels = input.first().split(", ").sorted().toTypedArray()
    return input.subList(2, input.size).sumOf { pattern -> solutionCount(pattern, towels) }
}

private fun isSolvable(pattern: String, sortedTowels: Array<String>, cache: MutableMap<String, Boolean> = mutableMapOf()): Boolean {
    cache[pattern]?.let { return it }
    val indexOfFirstMatch = sortedTowels.binarySearch(pattern)
    if (indexOfFirstMatch >= 0) return true.also { cache[pattern] = true }
    var nextMatch = -indexOfFirstMatch - 2
    while (nextMatch >= 0 && pattern.startsWith(sortedTowels[nextMatch][0])) {
        if (pattern.startsWith(sortedTowels[nextMatch]) &&
            isSolvable(pattern.substring(sortedTowels[nextMatch].length), sortedTowels, cache)
        ) return true
        nextMatch--
    }
    return false.also { cache[pattern] = false }
}

private fun solutionCount(pattern: String, sortedTowels: Array<String>, cache: MutableMap<String, Long> = mutableMapOf()): Long {
    cache[pattern]?.let { return it }
    if (pattern.isEmpty()) return 1L
    val indexOfFirstMatch = sortedTowels.binarySearch(pattern)
    var nextMatch = if (indexOfFirstMatch < 0) -indexOfFirstMatch - 2 else indexOfFirstMatch
    var ways = 0L
    while (nextMatch >= 0 && pattern.startsWith(sortedTowels[nextMatch][0])) {
        if (pattern.startsWith(sortedTowels[nextMatch])) {
            ways += solutionCount(pattern.substring(sortedTowels[nextMatch].length), sortedTowels, cache)
        }
        nextMatch--
    }
    return ways.also { cache[pattern] = it }
}
