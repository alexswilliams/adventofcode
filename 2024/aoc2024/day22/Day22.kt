package aoc2024.day22

import common.*

private val examples = loadFilesToLines("aoc2024/day22", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("aoc2024/day22", "input.txt").single()

internal fun main() {
    Day22.assertCorrect()
    benchmark(10) { part1(puzzle) } // 27.9ms
    benchmark(10) { part2(puzzle) } // 847ms
}

internal object Day22 : Challenge {
    override fun assertCorrect() {
        check(37327623, "P1 Example") { part1(examples[0]) }
        check(15613157363, "P1 Puzzle") { part1(puzzle) }

        check(23, "P2 Example") { part2(examples[1]) }
        check(1784, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Long =
    input.map { it.toLong() }.sumOf { seed ->
        (1..2000).fold(seed) { acc, _ -> hash(acc) }
    }

private fun part2(input: List<String>): Int {
    val sequences = mutableMapOf<List<Int>, MutableList<Int>>()

    input.map { it.toLong() }.forEach { seed ->
        val sequencesForRun = mutableMapOf<List<Int>, Int>()
        var last4Differences = first4Differences(seed)
        var lastResult = hash(hash(hash(hash(seed))))
        val lastPrice = (lastResult % 10).toInt()
        sequencesForRun[last4Differences] = lastPrice

        repeat(1996) {
            val result = hash(lastResult)
            val price = (result % 10).toInt()

            val diff = price - (lastResult % 10).toInt()
            last4Differences = last4Differences.drop(1).plus(diff)

            sequencesForRun.computeIfAbsent(last4Differences) { _ -> price }

            lastResult = result
        }

        sequencesForRun.forEach { (diffSequence, bestPrice) -> sequences.merge(diffSequence, mutableListOf(bestPrice)) { existing, _ -> existing.also { it.add(bestPrice) } } }
    }

    return sequences.maxOf { it.value.sum() }
}

private fun first4Differences(seed: Long): List<Int> {
    return listOf(seed, hash(seed), hash(hash(seed)), hash(hash(hash(seed))), hash(hash(hash(hash(seed)))))
        .map { (it % 10).toInt() }.zipWithNext().map { (a, b) -> b - a }
}


private fun hash(result: Long): Long {
    val a = ((result * 64) xor result) % 16777216
    val b = ((a / 32) xor a) % 16777216
    val c = ((b * 2048) xor b) % 16777216
    return c
}
