package aoc2023.day12

import common.*


private val example = loadFilesToLines("aoc2023/day12", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2023/day12", "input.txt").single()

internal fun main() {
    Day12.assertCorrect()
    benchmark { part1(puzzle) } // 715µs now 2.7ms
    benchmark(100) { part2(puzzle) } // 9ms now 44.4ms
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
private val SearchState.startAt get() = this shr 8
private val SearchState.runStartAt get() = this and 0xff

private fun sumOfPlacementsForAll(springs: List<Pair<String, List<Int>>>): Long =
    springs.sumOf { (pattern, runLengths) -> countPlacements(pattern, runLengths, searchState(0, 0)) }


private fun countPlacements(pattern: String, runLengths: List<Int>, state: SearchState, cache: MutableMap<SearchState, Long> = mutableMapOf()): Long {
    cache[state]?.let { return it }
    if (isSuccess(pattern, runLengths, state)) return 1
    if (willNeverSucceed(pattern, runLengths, state)) return 0

    val proposedNextStates = placeRunInGroup(pattern, state.startAt, runLengths, state.runStartAt)

    val successStateCount = proposedNextStates.count { isSuccess(pattern, runLengths, it) }
    val needFurtherWork = proposedNextStates.filterNot { isSuccess(pattern, runLengths, it) || willNeverSucceed(pattern, runLengths, it) }
    return successStateCount + needFurtherWork.sumOf { next ->
        countPlacements(pattern, runLengths, next, cache)
            .also { cache[next] = it }
    }
}

private fun isSuccess(pattern: String, runLengths: List<Int>, state: SearchState): Boolean =
    state.runStartAt > runLengths.lastIndex
            && (state.startAt > pattern.lastIndex || '#' !in pattern.substring(state.startAt))

private fun willNeverSucceed(pattern: String, runLengths: List<Int>, state: SearchState): Boolean =
    ((state.runStartAt > runLengths.lastIndex && state.startAt <= pattern.lastIndex && '#' in pattern.substring(state.startAt))
            || (state.runStartAt <= runLengths.lastIndex && state.startAt > pattern.lastIndex)
            || (runLengths.sumFrom(state.runStartAt) + runLengths.size - state.runStartAt - 1 > pattern.length - state.startAt))

private fun nextNonDotIndex(index: Int, pattern: String): Int = if (index <= pattern.lastIndex && pattern[index] == '.') index + 1 else index

private fun placeRunInGroup(pattern: String, startAt: Int, runLengths: List<Int>, runIndex: Int): List<SearchState> {
    val endOfGroup = pattern.indexOf('.', startAt)
    val group = pattern.substring(startAt, if (endOfGroup == -1) pattern.length else endOfGroup)
    val runLen = runLengths[runIndex]
    val results = mutableListOf<SearchState>()
    // if the group is all ???? it's valid to skip it entirely
    if ('#' !in group) results.add(searchState(nextNonDotIndex(startAt + group.length + 1, pattern), runIndex))

    var position = -1
    while (group.length >= runLen + position + 1) {
        position++
        val endOfRun = position + runLen
        if (group.length == runLen && position == 0
            || ('#' !in group.substring(0, position) && (endOfRun == group.length || group[endOfRun] != '#'))
        ) results.add(searchState(nextNonDotIndex(startAt + runLen + position + 1, pattern), runIndex + 1))
    }
    return results
}

fun simplify(springs: String, groupings: List<Int>): Pair<String, List<Int>> {
    var str = springs
    var lst = groupings.toMutableList()
    val removeTrivialCases = {
        while (true) {
            str = removeDuplicatesOfChar(str, '.').trimStart('.')
            val groups = str.split('.')
            if (str.isEmpty() || lst.isEmpty() || groups.isEmpty()) break
            val length = groups[0].length
            val runLen = lst[0]
            str = if (length == runLen && '#' in groups[0])
                str.removeRange(0, runLen).also { lst.removeFirst() }
            else if (length < runLen) {
                if ('#' in groups[0]) throw Error("Group to remove contained hash")
                else str.removeRange(0, length)
            } else if (groups[0][0] == '#' && groups[0][runLen] != '#')
                str.removeRange(0, (runLen + 1).coerceAtMost(length)).also { lst.removeFirst() }
            else if (length > runLen && groups[0].startsWith("?#") && groups[0][runLen] == '#' && (length == runLen + 1 || groups[0][runLen + 1] != '#'))
                str.removeRange(0, (runLen + 2).coerceAtMost(length)).also { lst.removeFirst() }
            else break
        }
    }

    var strBefore: String
    do {
        strBefore = str
        repeat(2) {
            removeTrivialCases()
            str = str.reversed()
            lst.reverse()
        }
    } while (strBefore != str)

    if (lst.isEmpty()) return str to lst
    if (lst.size == 1 && '.' !in str && str.length == lst[0]) return "" to emptyList()
    return str to lst
}

private fun removeDuplicatesOfChar(workingList: String, ch: Char): String {
    var output = workingList
    var i = 0
    while (i < output.length - 1) {
        if (output[i] == ch && output[i + 1] == ch) {
            var j = i + 1
            while (j < output.length && output[j] == ch) j++
            output = output.removeRange(i + 1, j)
        }
        i++
    }
    return output
}
