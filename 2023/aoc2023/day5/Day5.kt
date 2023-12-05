package aoc2023.day5

import common.benchmark
import common.fromClasspathFile
import common.intersecting
import common.keepingAbove
import common.keepingBelow
import common.shiftedUpBy
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day5/example.txt".fromClasspathFile()
private val puzzleInput = "aoc2023/day5/input.txt".fromClasspathFile()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(35L, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(600279879L, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(46L, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(20191102L, it) }
    benchmark { part1(puzzleInput) } // 230µs
    benchmark { part2(puzzleInput) } // 552µs
}

data class Mapping(val source: LongRange, val transform: Long)

private fun part1(input: String): Long {
    val seeds = input.lineSequence().first().substringAfter(": ").split(' ').map { it.toLong() }
    val maps = parseMappings(input)
    return seeds.minOf { seed ->
        maps.fold(seed) { acc, mappings -> acc + (mappings.firstOrNull { acc in it.source }?.transform ?: 0) }
    }
}

private fun part2(input: String): Long {
    val seedRanges = input.lineSequence().first().substringAfter(": ").split(' ').map { it.toLong() }
        .chunked(2) { (a, b) -> a..<(a + b) }
    val maps = parseMappings(input)

    var inputRanges = seedRanges
    for (map in maps) {
        val lowestSource = map.first().source.first
        val highestSource = map.last().source.last
        inputRanges = inputRanges.flatMap { inputRange ->
            listOf(
                listOf(
                    inputRange.keepingBelow(lowestSource),
                    inputRange.keepingAbove(highestSource)
                ),
                map.map { inputRange.intersecting(it.source).shiftedUpBy(it.transform) },
            ).flatten().filterNot { it.isEmpty() }
        }
    }

    return inputRanges.minOf { it.first }
}

// • no ranges overlap within a mapping group;
// • some ranges in a group have gaps between them which have been padded with an identity transformation
private fun parseMappings(input: String): List<List<Mapping>> =
    input.split("\n\n").drop(1).map { block ->
        block.lines().drop(1).map { line ->
            line.split(' ')
                .map { it.toLong() }
                .let { Mapping(it[1]..<(it[1] + it[2]), it[0] - it[1]) }
        }
            .sortedBy { it.source.first }
            .let {
                it.plus(it.zipWithNext()
                    .filter { (lower, higher) -> higher.source.first - lower.source.last > 1 }
                    .map { (lower, higher) -> Mapping(lower.source.last + 1..<higher.source.first, 0) })
            }
            .sortedBy { it.source.first }
    }
