package aoc2023.day12

import common.fromClasspathFileToLines
import common.splitToInts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.time.measureTimedValue


private val exampleInput = "aoc2023/day12/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day12/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(21, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(7032, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(525152, it) }
    val x = measureTimedValue { part2(puzzleInput).also { println("[Puzzle] Part 2: $it") } } // TODO: dynamic programming
    println(x.duration)
//    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(0, it) }
//    benchmark { part1(puzzleInput) } //
//    benchmark { part2(puzzleInput) } //
}

private fun part1(input: List<String>): Int {
    return input.sumOf { line ->
        val (templateInput, runLengthString) = line.split(' ', limit = 2)
        val runLengths = runLengthString.splitToInts(",")
        val template = ".$templateInput."

        val size = runLengths.fold(setOf(".")) { prefixes: Set<String>, runLength: Int ->
            prefixes.flatMapTo(mutableSetOf()) { i(template, runLength, it) }
        }.filter { template.indexOf('#', it.length) == -1 }.size
        size
    }
}


private fun part2(input: List<String>): Int {
    return runBlocking(Dispatchers.Default) {
        input.mapIndexed { index, line ->
            val (templateInput, runLengthString) = line.split(' ', limit = 2)
            val runLengthsShort = runLengthString.splitToInts(",")
            val runLengths = listOf(runLengthsShort, runLengthsShort, runLengthsShort, runLengthsShort, runLengthsShort).flatten()
            val template = ".$templateInput?$templateInput?$templateInput?$templateInput?$templateInput."

            async {
                runLengths.fold(setOf(".")) { prefixes: Set<String>, runLength: Int ->
                    prefixes.flatMapTo(mutableSetOf()) { i(template, runLength, it) }
                }.filter { template.indexOf('#', it.length) == -1 }.size.also { println("Row $index -> size $it") }
            }
        }.awaitAll().sum()
    }
}

private fun i(template: String, runLength: Int, prefix: String): List<String> {
    var startIndex = prefix.length
    var knownPrefix = prefix
    val prefixesToKeep = arrayListOf<String>()
    while (true) {
        // Advance so that startIndex points to the first char to be replaced with a #
        while (true) {
            val before = template[startIndex - 1]
            if (before == '#') return prefixesToKeep // advancing beyond an existing # will always make the run too long, so stop here
            if (startIndex + runLength > template.lastIndex) return prefixesToKeep
            val substring = template.substring(startIndex, startIndex + runLength)
            val after = template[startIndex + runLength]
            if (substring.contains('.') || after == '#') {
                knownPrefix += if (template[startIndex] == '?') '.' else template[startIndex]
                startIndex++
            } else break
        }
        val string = template.substring(startIndex).replaceRange(0, runLength, "#".repeat(runLength))
        if (runLength == firstRunLength(string)) {
            prefixesToKeep.add(knownPrefix + "#".repeat(runLength) + ".")
        } else
            return prefixesToKeep

        knownPrefix += "."
        startIndex++
    }
}

private fun firstRunLength(string: String): Int = string.dropWhile { it != '#' }.takeWhile { it == '#' }.length


