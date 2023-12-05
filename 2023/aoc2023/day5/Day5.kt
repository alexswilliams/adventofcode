package aoc2023.day5

import common.benchmark
import common.fromClasspathFileToLines
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day5/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day5/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(35L, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(600279879L, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(46L, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(20191102L, it) }
    benchmark { part1(puzzleInput) } // 240µs
//    benchmark { part2(puzzleInput) } // hahah like 10 minutes, // TODO
}

data class Mapping(val target: LongRange, val source: LongRange)

private fun part1(input: List<String>): Long {
    val seedsLine = input.first().substringAfter(": ").split(' ').map { it.toLong() }
    val maps = parseMappings(input)
    return seedsLine.minOf { seed -> followMaps(maps, seed) }
}

private fun part2(input: List<String>): Long {
    val seedRanges = input.first().substringAfter(": ").split(' ').map { it.toLong() }
        .chunked(2) { (a, b) -> a..<(a + b) }
    val maps = parseMappings(input)
    return seedRanges.minOf { seedRange ->
        println("considering range $seedRange (${(seedRange.last - seedRange.first)/1_000_000}m values)")
        seedRange.minOf { seed -> followMaps(maps, seed) }
    }
}

private fun followMaps(maps: List<List<Mapping>>, seed: Long) = maps.fold(seed) { acc, mappings ->
    val mapping = mappings.firstOrNull { acc in it.source } ?: Mapping(acc..acc, acc..acc)
    val offset = acc - mapping.source.first
    mapping.target.first + offset
}

private fun parseMappings(input: List<String>): List<List<Mapping>> {
    val maps = input.drop(2).joinToString("\n").split("\n\n").map { block ->
        block.lines().drop(1).map { line ->
            line.split(' ').map { it.toLong() }
                .let { Mapping(it[0]..<(it[0] + it[2]), it[1]..<(it[1] + it[2])) }
        }
    }
    return maps
}

