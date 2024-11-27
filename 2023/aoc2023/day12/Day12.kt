package aoc2023.day12

import common.*
import kotlinx.coroutines.*
import java.time.*


private val examples = loadFilesToLines("aoc2023/day12", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day12", "input.txt")

internal fun main() {
    Day12.assertCorrect()
    benchmark { part1(puzzles[0]) } // 715µs
    benchmark(10) { part2(puzzles[0]) } // 9ms
}

internal object Day12 : Challenge {
    override fun assertCorrect() {
        check(21, "P1 Example") { part1(examples[0]) }
        check(7032, "P1 Puzzle") { part1(puzzles[0]) }

        check(525152, "P2 Example") { part2(examples[0]) }
        // TODO: dynamic programming
        part2(puzzles[0]).also { println("[Puzzle] Part 2: $it") }
    }
}

private fun part1(input: List<String>): Int {
    return input.sumOf { line ->
        val (templateInput, runLengthString) = line.split(' ', limit = 2)
        val runLengths = runLengthString.splitToInts(",")
        val template = ".$templateInput."

        numberOfArrangements(runLengths, template.toCharArray())
    }
}


private fun part2(input: List<String>): Long {
    return runBlocking(Dispatchers.Default) {
        input.mapIndexed { index, line ->
            val (templateInput, runLengthString) = line.split(' ', limit = 2)
            val runLengthsOnce = runLengthString.splitToInts(",")
            val runLengths = listOf(runLengthsOnce, runLengthsOnce, runLengthsOnce, runLengthsOnce, runLengthsOnce).flatten()
            val template = ".$templateInput?$templateInput?$templateInput?$templateInput?$templateInput."

            async {
                println("[${Instant.now()}]  Starting Row $index")
                numberOfArrangements(runLengths, template.toCharArray()).toLong()
                    .also { println("[${Instant.now()}]  Finished Row $index -> size $it") }
            }
        }.awaitAll().sum()
    }
}

private fun numberOfArrangements(runLengths: List<Int>, templateArray: CharArray): Int {
    return runLengths.fold(listOf(1)) { prefixes, runLength ->
        val prefixLengths = ArrayList<Int>(1000)
        prefixes.forEach {
            prefixLengthsAfterPlacingNextRun(templateArray, runLength, it, prefixLengths)
        }
        prefixLengths
    }.filterNot { templateArray.existsBetween('#', it) }.size
}

private fun prefixLengthsAfterPlacingNextRun(template: CharArray, runLength: Int, prefixLength: Int, prefixesToKeep: ArrayList<Int>) {
    var startAt = prefixLength
    val lastIndex = template.lastIndex - runLength
    while (true) {
        while (true) {
            if (startAt > lastIndex) return
            if (template[startAt - 1] == '#') return
            if (shouldAdvance(template, startAt, runLength)) startAt++
            else break
        }
        prefixesToKeep.add(startAt + runLength + 1)
        startAt++
    }
}

private fun shouldAdvance(template: CharArray, startAt: Int, runLength: Int) =
    template.existsBetween('.', startAt, startAt + runLength - 1) ||
            template[startAt + runLength] == '#'

private fun CharArray.existsBetween(c: Char, startAt: Int = 0, endAt: Int = this.lastIndex): Boolean {
    for (i in startAt..endAt)
        if (this[i] == c)
            return true
    return false
}