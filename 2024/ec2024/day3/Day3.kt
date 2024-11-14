package ec2024.day3

import common.*
import kotlin.test.assertEquals

private const val rootFolder = "ec2024/day3"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day3.assertPart1Correct()
    Day3.assertPart2Correct()
    Day3.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 41µs
    benchmark { part2(puzzle2Input) } // 698µs
    benchmark(100) { part3(puzzle3Input) } // 4.62ms
}

internal object Day3 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(35, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(135, it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(35, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(2646, it) }
    }

    override fun assertPart3Correct() {
        part3(exampleInput).also { println("[Example] Part 3: $it") }.also { assertEquals(29, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(10398, it) }
    }
}


private fun part1(input: List<String>): Int {
    val level = levelToSparseGrid(input)
    return countAllSoilRemoved(level, ::castleAdjacent)
}

private fun part2(input: List<String>): Int = part1(input)

private fun part3(input: List<String>): Int {
    val level = levelToSparseGrid(input)
    return countAllSoilRemoved(level, ::queenAdjacent)
}


private fun levelToSparseGrid(input: List<String>): MutableSet<Long> = input.flatMapIndexedTo(mutableSetOf()) { row, line ->
    line.mapIndexedNotNull { col, it -> if (it == '#') row by col else null }
}

private fun removeEdgePieces(currentLevel: Set<Long>, hasAllNeighbours: (Long, Set<Long>) -> Boolean) =
    currentLevel.mapNotNullTo(mutableSetOf()) {
        if (hasAllNeighbours(it, currentLevel)) it
        else null
    }

private fun countAllSoilRemoved(level: Set<Long>, hasAllNeighbours: (Long, Set<Long>) -> Boolean): Int {
    return if (level.isEmpty()) 0
    else level.size + countAllSoilRemoved(removeEdgePieces(level, hasAllNeighbours), hasAllNeighbours)
}

private fun castleAdjacent(pos: Long, grid: Set<Long>) =
    grid.contains(pos.plusCol()) && grid.contains(pos.minusCol()) && grid.contains(pos.plusRow()) && grid.contains(pos.minusRow())

private fun queenAdjacent(pos: Long, grid: Set<Long>) =
    castleAdjacent(pos, grid) &&
            grid.contains(pos.minusCol().minusRow()) && grid.contains(pos.minusCol().plusRow()) &&
            grid.contains(pos.plusCol().minusRow()) && grid.contains(pos.plusCol().plusRow())
