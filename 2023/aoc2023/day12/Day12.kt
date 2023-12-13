package aoc2023.day12

import common.fromClasspathFileToLines
import common.splitToInts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day12/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day12/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(21, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(7032, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(525152, it) }
//    val x = measureTimedValue { part2(puzzleInput).also { println("[Puzzle] Part 2: $it") } } // TODO: dynamic programming
//    println(x.duration)
//    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(0, it) }
//    benchmark { part1(puzzleInput) } // 2.46ms
//    benchmark(10) { part2(exampleInput) } // 60ms
}

private fun part1(input: List<String>): Int {
    return input.sumOf { line ->
        val (templateInput, runLengthString) = line.split(' ', limit = 2)
        val runLengths = runLengthString.splitToInts(",")
        val template = ".$templateInput."

        numberOfArrangements(runLengths, template)
    }
}


private fun part2(input: List<String>): Int {
    return runBlocking(Dispatchers.Default) {
        input.mapIndexed { index, line ->
            val (templateInput, runLengthString) = line.split(' ', limit = 2)
            val runLengthsOnce = runLengthString.splitToInts(",")
            val runLengths = listOf(runLengthsOnce, runLengthsOnce, runLengthsOnce, runLengthsOnce, runLengthsOnce).flatten()
            val template = ".$templateInput?$templateInput?$templateInput?$templateInput?$templateInput."

            async {
                println("[${Instant.now()}]  Starting Row $index")
                numberOfArrangements(runLengths, template)
                    .also { println("[${Instant.now()}]  Finished Row $index -> size $it") }
            }
        }.awaitAll().sum()
    }
}

private fun numberOfArrangements(runLengths: List<Int>, template: String) =
    runLengths.fold(listOf(".")) { prefixes, runLength ->
        prefixes.flatMap { placementsForRun(template, runLength, it, it.length) }
    }.filter { template.indexOf('#', it.length) == -1 }.size

private fun placementsForRun(template: String, runLength: Int, prefix: String, prefixLength: Int): List<String> {
    var startIndex = prefixLength
    val prefixesToKeep = arrayListOf<String>()
    while (true) {
        while (true) {
            if (startIndex + runLength > template.lastIndex) return prefixesToKeep
            if (template[startIndex - 1] == '#') return prefixesToKeep
            if (template.indexOf('.', startIndex) < (startIndex + runLength) || template[startIndex + runLength] == '#') startIndex++
            else break
        }
        prefixesToKeep.add(prefix + template.substring(prefixLength, startIndex) + runs[runLength] + ".")
        startIndex++
    }
}

private val runs = (0..20).map { "#".repeat(it) }.toTypedArray()
