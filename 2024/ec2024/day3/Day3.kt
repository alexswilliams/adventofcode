package ec2024.day3

import common.*

private val examples = loadFilesToLines("ec2024/day3", "example.txt")
private val puzzles = loadFilesToLines("ec2024/day3", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day3.assertCorrect()
    benchmark { part1(puzzles[0]) } // 41µs
    benchmark { part2(puzzles[1]) } // 698µs
    benchmark(100) { part3(puzzles[2]) } // 4.62ms
}

internal object Day3 : Challenge {
    override fun assertCorrect() {
        check(35, "P1 Example") { part1(examples[0]) }
        check(135, "P1 Puzzle") { part1(puzzles[0]) }

        check(35, "P2 Example") { part2(examples[0]) }
        check(2646, "P2 Puzzle") { part2(puzzles[1]) }

        check(29, "P3 Example") { part3(examples[0]) }
        check(10398, "P3 Puzzle") { part3(puzzles[2]) }
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
