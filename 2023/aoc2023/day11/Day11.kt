package aoc2023.day11

import common.*
import kotlin.math.*

private val examples = loadFilesToGrids("aoc2023/day11", "example.txt")
private val puzzles = loadFilesToGrids("aoc2023/day11", "input.txt")

internal fun main() {
    Day11.assertCorrect()
    benchmark { part1(puzzles[0]) } // 256µs
    benchmark { part2(puzzles[0], 1_000_000) } // 214µs
}

internal object Day11 : Challenge {
    override fun assertCorrect() {
        check(374, "P1 Example") { part1(examples[0]) }
        check(9609130, "P1 Puzzle") { part1(puzzles[0]) }

        check(1030, "P2 Example (x10)") { part2(examples[0], 10) }
        check(8410, "P2 Example (x100)") { part2(examples[0], 100) }
        check(702152204842L, "P2 Puzzle") { part2(puzzles[0], 1_000_000) }
    }
}

private fun part1(input: Grid): Long = sumDistances(expandGalaxy(input, 2))
private fun part2(input: Grid, scaleFactor: Int): Long = sumDistances(expandGalaxy(input, scaleFactor))

private fun sumDistances(expanded: List<Location>): Long = expanded.sumOfIndexed(0L) { index, galaxy ->
    (0..index).sumOf { abs(expanded[it].row() - galaxy.row()) + abs(expanded[it].col() - galaxy.col()) }
}

private fun expandGalaxy(input: Grid, scaleFactor: Int): List<Location> {
    val galaxies = input.allLocationOf('#')
    val blankRowsBefore = (input.indices).runningFold(0) { prev: Int, index: Int -> if (galaxies.any { it.row() == index + 1 }) prev else prev + 1 }
    val blankColsBefore = (input[0].indices).runningFold(0) { prev: Int, index: Int -> if (galaxies.any { it.col() == index + 1 }) prev else prev + 1 }
    return galaxies.map { galaxy -> (galaxy.row() + blankRowsBefore[galaxy.row()] * (scaleFactor - 1)) by (galaxy.col() + blankColsBefore[galaxy.col()] * (scaleFactor - 1)) }
}
