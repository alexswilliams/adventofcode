package aoc2024.day5

import common.*

private val example = loadFilesToLines("aoc2024/day5", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day5", "input.txt").single()

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzle) } // 722µs
    benchmark(10) { part2(puzzle) } // 7.9ms
}

internal object Day5 : Challenge {
    override fun assertCorrect() {
        check(143, "P1 Example") { part1(example) }
        check(6051, "P1 Puzzle") { part1(puzzle) }

        check(123, "P2 Example") { part2(example) }
        check(5093, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val ruleset = parseRuleset(input)
    return parsePageLists(input)
        .filter { pageList -> pageListIsSorted(pageList, ruleset) }
        .sumOf { it[it.size / 2] }
}

private fun part2(input: List<String>): Int {
    val ruleset = parseRuleset(input)
    return parsePageLists(input)
        .filterNot { pageList -> pageListIsSorted(pageList, ruleset) }
        .sumOf { pageList ->
            val sortedTail = mutableSetOf<Int>()
            val unsortedPrefix = pageList.toMutableSet()
            var middleItem: Int
            do {
                val nextMiddleItem = ruleWithFewestConstraintsStillApplicableToPrefix(ruleset, unsortedPrefix)
                ruleset[nextMiddleItem].orEmpty().forEach { if (it in unsortedPrefix) sortedTail.add(it) }
                sortedTail.add(nextMiddleItem)
                unsortedPrefix.remove(nextMiddleItem)
                middleItem = nextMiddleItem
            } while (sortedTail.size < unsortedPrefix.size)
            middleItem
        }
}

private fun parseRuleset(input: List<String>) =
    input.takeWhile { it.isNotBlank() }.map { it.split("|") }.groupBy({ it[0].toInt() }, { it[1].toInt() }).mapValues { it.value.toSet() }

private fun parsePageLists(input: List<String>) =
    input.takeLastWhile { it.isNotBlank() }.map { it.splitToInts(",") }

private fun pageListIsSorted(pageList: Collection<Int>, ruleset: Map<Int, Set<Int>>): Boolean =
    with(mutableListOf<Int>()) { pageList.all { page -> hasNoOverlapWith(ruleset[page].orEmpty()).also { add(page) } } }

private fun ruleWithFewestConstraintsStillApplicableToPrefix(ruleset: Map<Int, Set<Int>>, unsortedPrefix: Collection<Int>) =
    (ruleset.keys.filter { it in unsortedPrefix })
        .minByOrNull { leader -> ruleset[leader].orEmpty().count { it in unsortedPrefix } } ?: unsortedPrefix.first()
