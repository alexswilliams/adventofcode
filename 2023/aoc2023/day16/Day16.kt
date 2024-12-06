package aoc2023.day16

import aoc2023.day16.Heading.*
import common.*
import kotlinx.coroutines.*


private val examples = loadFilesToGrids("aoc2023/day16", "example.txt")
private val puzzles = loadFilesToGrids("aoc2023/day16", "input.txt")

internal fun main() {
    Day16.assertCorrect()
    benchmark { part1(puzzles[0]) } // 432µs
    benchmark(100) { part2(puzzles[0]) } // 86ms or 44.8ms parallel
}

internal object Day16 : Challenge {
    override fun assertCorrect() {
        check(46, "P1 Example") { part1(examples[0]) }
        check(7870, "P1 Puzzle") { part1(puzzles[0]) }

        check(51, "P2 Example") { part2(examples[0]) }
        check(8143, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private enum class Heading(val nextCell: (Location1616) -> Location1616) {
    Up({ it.minusRow() }),
    Down({ it.plusRow() }),
    Left({ it.minusCol() }),
    Right({ it.plusCol() })
}

private fun part1(grid: Grid): Int = countCellsEnergisedByStartingAt(grid, 0 by16 0, Right)

private fun part2(grid: Grid): Int = runBlocking(Dispatchers.Default) {
    listOf(
        async { grid.rowIndices.maxOf { row -> countCellsEnergisedByStartingAt(grid, row by16 0, Right) } },
        async { grid.rowIndices.maxOf { row -> countCellsEnergisedByStartingAt(grid, row by16 grid.colIndices.last, Left) } },
        async { grid.colIndices.maxOf { col -> countCellsEnergisedByStartingAt(grid, 0 by16 col, Down) } },
        async { grid.colIndices.maxOf { col -> countCellsEnergisedByStartingAt(grid, grid.rowIndices.last by16 col, Up) } }
    ).awaitAll().max()
}


private fun countCellsEnergisedByStartingAt(grid: Grid, startAt: Location1616, startHeading: Heading): Int {
    data class Work(val pos: Location1616, val heading: Heading)

    val work = ArrayDeque(listOf(Work(startAt, startHeading)))
    val visitedByDirection = Array(grid.height) { IntArray(grid.width) }
        .apply { this[startAt.row()][startAt.col()] = 1 shl startHeading.ordinal }

    val rows = grid.rowIndices
    val cols = grid.colIndices
    fun visit(next: Location1616, heading: Heading) {
        if (next.row() in rows && next.col() in cols && visitedByDirection[next.row()][next.col()] and (1 shl heading.ordinal) == 0) {
            visitedByDirection[next.row()][next.col()] = visitedByDirection[next.row()][next.col()] or (1 shl heading.ordinal)
            work.addLast(Work(next, heading))
        }
    }

    while (true) {
        val (pos, heading) = work.removeFirstOrNull() ?: break
        val opAtPos = grid[pos.row()][pos.col()]
        when (opAtPos) {
            '.' -> visit(heading.nextCell(pos), heading)
            '|' -> if (heading == Right || heading == Left) {
                visit(pos.minusRow(), Up)
                visit(pos.plusRow(), Down)
            } else visit(heading.nextCell(pos), heading)
            '-' -> if (heading == Up || heading == Down) {
                visit(pos.minusCol(), Left)
                visit(pos.plusCol(), Right)
            } else visit(heading.nextCell(pos), heading)
            '\\' -> when (heading) {
                Up -> visit(pos.minusCol(), Left)
                Down -> visit(pos.plusCol(), Right)
                Left -> visit(pos.minusRow(), Up)
                Right -> visit(pos.plusRow(), Down)
            }
            '/' -> when (heading) {
                Up -> visit(pos.plusCol(), Right)
                Down -> visit(pos.minusCol(), Left)
                Left -> visit(pos.plusRow(), Down)
                Right -> visit(pos.minusRow(), Up)
            }
            else -> throw Error()
        }
    }
    return visitedByDirection.sumOf { it.count { i -> i != 0 } }
}
