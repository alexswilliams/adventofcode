package aoc2023.day12

import common.*
import kotlinx.coroutines.*


private val example = loadFilesToLines("aoc2023/day12", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2023/day12", "input.txt").single()

internal fun main() {
    Day12.assertCorrect()
    benchmark { part1(puzzle) } // 2.19ms, 2.18ms parallel
    benchmark(100) { part2(puzzle) } // 33.1ms, 7.2ms parallel
}

internal object Day12 : Challenge {
    override fun assertCorrect() {
        check(21, "P1 Example") { part1(example) }
        check(7032, "P1 Puzzle") { part1(puzzle) }

        check(525152, "P2 Example") { part2(example) }
        check(1493340882140L, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: List<String>): Long =
    sumOfPlacementsForAll(
        input.splitOnSpaces()
            .map { simplify(it[0], it[1].splitToInts(",")) })

private fun part2(input: List<String>): Long =
    sumOfPlacementsForAll(
        input.splitOnSpaces()
            .map { simplify(listOf(it[0]).repeat(5).joinToString("?"), it[1].splitToInts(",").repeat(5)) })


private typealias SearchState = Int

private fun searchState(patternStartAt: Int, runStartAt: Int): SearchState = (patternStartAt shl 8) or runStartAt
private val SearchState.startAt
    get() = this shr 8
private val SearchState.runStartAt
    get() = this and 0xff

private fun sumOfPlacementsForAll(springs: List<Pair<String, List<Int>>>): Long =
    runBlocking(Dispatchers.Default) {
        springs.map { (pattern, runLengths) -> async { countPlacements(pattern, runLengths, searchState(0, 0)) } }
            .awaitAll()
            .sum()
    }


private fun countPlacements(
    pattern: String,
    runLengths: List<Int>,
    state: SearchState,
    cache: MutableMap<SearchState, Long> = mutableMapOf(),
    groupCache: Array<String> = Array(pattern.length) { i ->
        val endOfGroup = pattern.indexOf('.', i)
        pattern.substring(i, if (endOfGroup == -1) pattern.length else endOfGroup)
    },
): Long {
    cache[state]?.let { return it }
    if (isSuccess(pattern, runLengths, state)) return 1

    return placeRunInGroup(pattern, state.startAt, runLengths, state.runStartAt, groupCache)
        .sumOf { next ->
            if (isSuccess(pattern, runLengths, next)) 1
            else if (willNeverSucceed(pattern, runLengths, next)) 0
            else countPlacements(pattern, runLengths, next, cache, groupCache).also { cache[next] = it }
        }
}

private fun isSuccess(pattern: String, runLengths: List<Int>, state: SearchState): Boolean =
    state.runStartAt > runLengths.lastIndex
            && (state.startAt > pattern.lastIndex || !pattern.appearsOnOrAfter('#', state.startAt))

private fun willNeverSucceed(pattern: String, runLengths: List<Int>, state: SearchState): Boolean =
    ((state.runStartAt > runLengths.lastIndex && state.startAt <= pattern.lastIndex && pattern.appearsOnOrAfter('#', state.startAt))
            || (state.runStartAt <= runLengths.lastIndex && state.startAt > pattern.lastIndex)
            || (runLengths.sumFrom(state.runStartAt) + runLengths.size - state.runStartAt - 1 > pattern.length - state.startAt))

private fun nextNonDotIndex(index: Int, pattern: String): Int = if (index <= pattern.lastIndex && pattern[index] == '.') index + 1 else index

private fun placeRunInGroup(pattern: String, startAt: Int, runLengths: List<Int>, runIndex: Int, groupCache: Array<String>): List<SearchState> {
    val group = groupCache[startAt]
    val runLen = runLengths[runIndex]
    val results = mutableListOf<SearchState>()
    // if the group is all ???? it's valid to skip it entirely
    if ('#' !in group) results.add(searchState(nextNonDotIndex(startAt + group.length + 1, pattern), runIndex))

    (0..(group.length - runLen)).forEach { position ->
        if (group.length == runLen && position == 0
            || (group.doesNotAppearBefore('#', position)
                    && (position + runLen == group.length || group[position + runLen] != '#'))
        ) results.add(searchState(nextNonDotIndex(startAt + runLen + position + 1, pattern), runIndex + 1))
    }
    return results
}

private fun simplify(springs: String, groupings: List<Int>): Pair<String, List<Int>> {
    var str = springs.removeDuplicatesOf('.')
    var lst = groupings.toMutableList()

    var strBefore: String
    do {
        strBefore = str
        repeat(2) {
            while (true) {
                str = str.trimStart('.')
                val group = str.substringBefore('.')
                if (str.isEmpty() || lst.isEmpty() || group.isEmpty()) break
                val length = group.length
                val runLen = lst[0]
                str = if (length == runLen && '#' in group)
                    str.removeRange(0, runLen).also { lst.removeFirst() }
                else if (length < runLen)
                    str.removeRange(0, length)
                else if (group[0] == '#' && group[runLen] != '#')
                    str.removeRange(0, (runLen + 1).coerceAtMost(length)).also { lst.removeFirst() }
                else if (length > runLen && group.startsWith("?#") && group[runLen] == '#' && (length == runLen + 1 || group[runLen + 1] != '#'))
                    str.removeRange(0, (runLen + 2).coerceAtMost(length)).also { lst.removeFirst() }
                else break
            }
            str = str.reversed()
            lst.reverse()
        }
    } while (strBefore != str)

    if (lst.isEmpty()) return str to lst
    if (lst.size == 1 && '.' !in str && str.length == lst[0]) return "" to emptyList()
    return str to lst
}

