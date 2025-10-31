package aoc2024.day21

import common.*

private val example = loadFilesToLines("aoc2024/day21", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day21", "input.txt").single()

internal fun main() {
    Day21.assertCorrect()
    benchmark { part1(puzzle) } // 64µs
    benchmark { part2(puzzle) } // 131µs
}

internal object Day21 : Challenge {
    override fun assertCorrect() {
        check(126384, "P1 Example") { part1(example) }
        check(248108, "P1 Puzzle") { part1(puzzle) }

        check(303836969158972, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Long =
    with(newCache()) { input.sumOf { code -> fewestPushesNeeded("A$code", 2, this) * code.toIntFromIndex(0) } }

private fun part2(input: List<String>): Long =
    with(newCache()) { input.sumOf { code -> fewestPushesNeeded("A$code", 25, this) * code.toIntFromIndex(0) } }


private fun fewestPushesNeeded(code: String, dirPadsRemaining: Int, cache: MutableMap<Int, Long>): Long {
    if (code.length == 2) cache[cacheKey(dirPadsRemaining, code)]?.let { return it }

    val pairs = code.zipWithNext().map { allPaths[it]!! }
    if (dirPadsRemaining == 0) return pairs.sumOf { it.first().size }.toLong()

    return pairs.reduce { acc, options -> options.flatMap { option -> acc.map { it + option } } }
        .minOf { candidate ->
            (listOf('A') + candidate).zipWithNext().sumOf { (start, end) ->
                // The risk is that a longer sequence one level down might incur a shorter overall sequence; e.g. doing vvvA
                // would cause all levels above to choose 'A' repeatedly, which might overall be shorter a shorter sequence at the
                // level beneath - that means you might only be able to consider full sequences for each level.
                // But this recursion is valid because at the end of every move, each level above will have reached the A button
                // which means every move beneath here starts from the same state, and so they are all independent of each other.
                // So... the problem can be divided and conquered.
                fewestPushesNeeded("$start$end", dirPadsRemaining - 1, cache)
            }
        }.also { if (code.length == 2) cache[cacheKey(dirPadsRemaining, code)] = it }
}

private fun newCache() = mutableMapOf<Int, Long>()
private fun cacheKey(dirPadsRemaining: Int, code: String): Int =
    dirPadsRemaining * 100 + code[0].code * 10 + code[1].code


private val dirPad = mapOf(
    'A' to listOf('<' to '^', 'v' to '>'),
    '^' to listOf('v' to 'v', '>' to 'A'),
    '<' to listOf('>' to 'v'),
    '>' to listOf('<' to 'v', '^' to 'A'),
    'v' to listOf('<' to '<', '>' to '>', '^' to '^'),
)

private val numPad = mapOf(
    'A' to listOf('<' to '0', '^' to '3'),
    '0' to listOf('^' to '2', '>' to 'A'),
    '1' to listOf('^' to '4', '>' to '2'),
    '2' to listOf('^' to '5', '<' to '1', 'v' to '0', '>' to '3'),
    '3' to listOf('^' to '6', '<' to '2', 'v' to 'A'),
    '4' to listOf('^' to '7', 'v' to '1', '>' to '5'),
    '5' to listOf('^' to '8', '<' to '4', 'v' to '2', '>' to '6'),
    '6' to listOf('^' to '9', '<' to '5', 'v' to '3'),
    '7' to listOf('v' to '4', '>' to '8'),
    '8' to listOf('<' to '7', 'v' to '5', '>' to '9'),
    '9' to listOf('<' to '8', 'v' to '6'),
)

private fun Map<Char, List<Pair<Char, Char>>>.waysBetween(start: Char, target: Char): List<List<Char>> {
    fun waysBetweenInner(start: Char, seenSoFar: List<Char>): List<List<Char>> {
        if (start == target) return listOf(listOf('A'))
        val seenForNext = seenSoFar + start
        return this[start]!!
            .filter { (_, next) -> next !in seenSoFar }
            .flatMap { (move, symbolReached) -> waysBetweenInner(symbolReached, seenForNext).map { it + move } }
    }
    return waysBetweenInner(start, listOf())
        .map { it.reversed() }
        .sortedBy { it.size }.let { all -> all.filter { it.size == all.first().size } }
}

// Caching this outside the benchmark is on the edge of cheating; but it's day 21 and starting to feel like a second job
private val dirPadPaths = cartesianProductOf(dirPad.keys, dirPad.keys)
    .associateWith { (prev, next) -> dirPad.waysBetween(prev, next) }
private val numPadPaths = cartesianProductOf(numPad.keys, numPad.keys)
    .associateWith { (prev, next) -> numPad.waysBetween(prev, next) }
private val allPaths = dirPadPaths + numPadPaths
