package aoc2024.day19

import common.*

private val example = loadFilesToLines("aoc2024/day19", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day19", "input.txt").single()

internal fun main() {
    Day19.assertCorrect()
    benchmark(100) { part1(puzzle) } // 3.2ms
    benchmark(100) { part2(puzzle) } // 8.0ms
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

private fun isSolvable(pattern: String, sortedTowels: Array<String>, startAt: Int = 0, cache: IntArray = IntArray(pattern.length) { -1 }): Boolean {
    cache[startAt].let { if (it >= 0) return it == 1 }
    val indexOfFirstMatch = sortedTowels.binarySearch(pattern.substring(startAt))
    if (indexOfFirstMatch >= 0) {
        cache[startAt] = 1
        return true
    }
    var nextMatch = -indexOfFirstMatch - 2
    while (nextMatch >= 0 && pattern[startAt] == sortedTowels[nextMatch][0]) {
        if (pattern.startsWith(sortedTowels[nextMatch], startAt)) {
            if (isSolvable(pattern, sortedTowels, startAt + sortedTowels[nextMatch].length, cache))
                return true
        }
        nextMatch--
    }
    return false.also { cache[startAt] = 0 }
}

private fun solutionCount(pattern: String, sortedTowels: Array<String>, startAt: Int = 0, cache: LongArray = LongArray(pattern.length) { -1 }): Long {
    if (startAt == pattern.length) return 1L
    cache[startAt].let { if (it >= 0) return it }
    val indexOfFirstMatch = sortedTowels.binarySearch(pattern.substring(startAt))
    var nextMatch = if (indexOfFirstMatch < 0) -indexOfFirstMatch - 2 else indexOfFirstMatch
    var ways = 0L
    while (nextMatch >= 0 && pattern[startAt] == sortedTowels[nextMatch][0]) {
        if (pattern.startsWith(sortedTowels[nextMatch], startAt)) {
            ways += solutionCount(pattern, sortedTowels, startAt + sortedTowels[nextMatch].length, cache)
        }
        nextMatch--
    }
    return ways.also { cache[startAt] = it }
}
