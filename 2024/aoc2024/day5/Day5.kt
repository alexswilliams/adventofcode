package aoc2024.day5

import common.*

private val example = loadFilesToLines("aoc2024/day5", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day5", "input.txt").single()

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzle) } // 2.6ms
    benchmark(10) { part2(puzzle) } // 15.3ms
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
    val rules = input.takeWhile { it.isNotBlank() }.map { it.split("|") }.groupBy({ it[0].toInt() }, { it[1].toInt() }).mapValues { it.value.toSet() }
    val pages = input.takeLastWhile { it.isNotBlank() }.map { it.splitToInts(",") }
    return pages.filter { pageList ->
        pageList.mapIndexed { index, page -> (rules[page].orEmpty() intersect pageList.take(index)).isEmpty() }.all { it }
    }.sumOf { it[it.size / 2] }
}

private fun part2(input: List<String>): Int {
    val rules = input.takeWhile { it.isNotBlank() }.map { it.split("|") }.groupBy({ it[0].toInt() }, { it[1].toInt() }).mapValues { it.value.toSet() }
    val pages = input.takeLastWhile { it.isNotBlank() }.map { it.splitToInts(",") }
    val failingPageLists = pages.filter { pageList ->
        pageList.mapIndexed { index, page -> (rules[page].orEmpty() intersect pageList.take(index)).isNotEmpty() }.any { it }
    }
    return failingPageLists.sumOf { list ->
        val tail = mutableListOf<Int>()
        val prefix = list.toMutableList()
        while (prefix.isNotEmpty()) {
            val relevantRules = (rules.keys intersect prefix).map { it to (rules[it].orEmpty() intersect prefix) }
            val fewestFollowers = relevantRules.minByOrNull { it.second.size } ?: (prefix.last() to emptySet())
            fewestFollowers.second.forEach { if (it !in tail) tail.addFirst(it) }
            if (fewestFollowers.first !in tail) tail.addFirst(fewestFollowers.first)
            prefix.remove(fewestFollowers.first)
        }
        tail[tail.size / 2]
    }
}

