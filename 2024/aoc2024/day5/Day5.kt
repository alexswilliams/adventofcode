package aoc2024.day5

import common.*

private val example = loadFilesToLines("aoc2024/day5", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day5", "input.txt").single()

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzle) } // 470µs
    benchmark { part2(puzzle) } // 436µs
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
        .sumOf {
            it.sortedWith { a, b ->
                if (a in ruleset[b].orEmpty()) -1
                else if (b in ruleset[a].orEmpty()) 1
                else 0
            }[it.size / 2]
        }
}

private fun parseRuleset(input: List<String>) =
    input.takeWhile { it.isNotBlank() }.map { it.split("|") }.groupBy({ it[0].toInt() }, { it[1].toInt() }).mapValues { it.value.toSet() }

private fun parsePageLists(input: List<String>) =
    input.takeLastWhile { it.isNotBlank() }.map { it.splitToInts(",") }

private fun pageListIsSorted(pageList: Collection<Int>, ruleset: Map<Int, Set<Int>>): Boolean =
    with(mutableListOf<Int>()) { pageList.all { page -> hasNoOverlapWith(ruleset[page].orEmpty()).also { add(page) } } }
