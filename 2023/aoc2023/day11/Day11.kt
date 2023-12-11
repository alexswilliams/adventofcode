package aoc2023.day11

import common.benchmark
import common.fromClasspathFile
import common.linesAsCharArrays
import common.mapCartesianNotNull
import common.sumOfIndexed
import kotlin.math.abs
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day11/example.txt".fromClasspathFile().linesAsCharArrays()
private val puzzleInput = "aoc2023/day11/input.txt".fromClasspathFile().linesAsCharArrays()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(374, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(9609130, it) }
    part2(exampleInput, 10).also { println("[Example x 10] Part 2: $it") }.also { assertEquals(1030, it) }
    part2(exampleInput, 100).also { println("[Example x 100] Part 2: $it") }.also { assertEquals(8410, it) }
    part2(puzzleInput, 1_000_000).also { println("[Puzzle x 1m] Part 2: $it") }.also { assertEquals(702152204842L, it) }
    benchmark { part1(puzzleInput) } // 256µs
    benchmark { part2(puzzleInput, 1_000_000) } // 214µs
}

private fun part1(input: List<CharArray>): Long = sumDistances(expandGalaxy(input, 2))
private fun part2(input: List<CharArray>, scaleFactor: Int): Long = sumDistances(expandGalaxy(input, scaleFactor))

private fun sumDistances(expanded: List<Location>): Long = expanded.sumOfIndexed(0L) { index, galaxy ->
    (0..index).sumOf { abs(expanded[it].row() - galaxy.row()) + abs(expanded[it].col() - galaxy.col()) }
}

private fun expandGalaxy(input: List<CharArray>, scaleFactor: Int): List<Location> {
    val galaxies = input.mapCartesianNotNull { rowNum, colNum, c -> if (c == '#') rowNum by colNum else null }
    val blankRowsBefore = (input.indices).runningFold(0) { prev: Int, index: Int -> if (galaxies.any { it.rowInt() == index + 1 }) prev else prev + 1 }
    val blankColsBefore = (input[0].indices).runningFold(0) { prev: Int, index: Int -> if (galaxies.any { it.colInt() == index + 1 }) prev else prev + 1 }
    return galaxies.map { galaxy -> (galaxy.row() + blankRowsBefore[galaxy.rowInt()] * (scaleFactor - 1)) by (galaxy.col() + blankColsBefore[galaxy.colInt()] * (scaleFactor - 1)) }
}

private typealias Location = Long

private infix fun Int.by(col: Int): Location = (this.toLong() shl 32) or col.toLong()
private infix fun Long.by(col: Long): Location = (this shl 32) or col
private fun Location.row() = this shr 32
private fun Location.col() = this and 0xffffffff
private fun Location.rowInt() = (this shr 32).toInt()
private fun Location.colInt() = (this and 0xffffffff).toInt()
