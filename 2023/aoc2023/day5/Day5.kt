package aoc2023.day5

import common.*
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day5/example.txt".fromClasspathFile()
private val puzzleInput = "aoc2023/day5/input.txt".fromClasspathFile()

internal fun main() {
    Day5.assertPart1Correct()
    Day5.assertPart2Correct()
    benchmark { part1(puzzleInput) } // 230µs
    benchmark { part2(puzzleInput) } // 369µs
}

internal object Day5 : TwoPartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(35L, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(600279879L, it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(46L, it) }
        part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(20191102L, it) }
    }
}

private data class Mapping(val source: LongRange, val transform: Long)

private fun part1(input: String): Long {
    val maps = parseMappings(input)
    return parseSeeds(input).minOf { seed ->
        maps.fold(seed) { acc, mappings -> acc + (mappings.firstOrNull { acc in it.source }?.transform ?: 0) }
    }
}

private fun part2(input: String): Long {
    val seedRanges = parseSeeds(input).chunked(2) { (start, count) -> start..<(start + count) }
    return parseMappings(input).fold(seedRanges) { ranges, mappings ->
        ranges.flatMap { range ->
            mappings.map { range.intersecting(it.source).shiftedUpBy(it.transform) }
                .plusElement(range.keepingBelow(mappings.first().source.first))
                .plusElement(range.keepingAbove(mappings.last().source.last))
        }.filterNot { it.isEmpty() }
    }.minOf { it.first }
}

private fun parseSeeds(input: String): List<Long> =
    input.lineSequence().first().substringAfter(": ").splitToLongs(" ")

private fun parseMappings(input: String): List<List<Mapping>> =
// • no ranges overlap within a mapping group;
// • some ranges in a group have gaps between them which have been padded with an identity transformation
    input.split("\n\n").drop(1).map { mappingGroup ->
        mappingGroup.lines().drop(1).map { line ->
            line.splitToLongs(" ")
                .let { (target, source, count) -> Mapping(source = source..<(source + count), transform = target - source) }
        }.let { mappings ->
            mappings.plus(mappings.sortedBy { it.source.first }
                .zipWithNext { lower, higher -> Mapping(source = LongRange.gapBetween(lower.source, higher.source), transform = 0) }
                .filterNot { it.source.isEmpty() })
        }.sortedBy { it.source.first }
    }

private fun LongRange.Companion.gapBetween(lower: LongRange, higher: LongRange) = lower.last + 1..<higher.first
