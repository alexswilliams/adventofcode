package aoc2023.day16

import aoc2023.day16.Heading.*
import common.*


private val examples = loadFilesToGrids("aoc2023/day16", "example.txt")
private val puzzles = loadFilesToGrids("aoc2023/day16", "input.txt")

internal fun main() {
    Day16.assertCorrect()
    benchmark { part1(puzzles[0]) } // 1.2ms
    benchmark(10) { part2(puzzles[0]) } // 492ms
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

private fun part2(grid: Grid): Int = max(
    grid.rowIndices.maxOf { row -> countCellsEnergisedByStartingAt(grid, row by16 0, Right) },
    grid.rowIndices.maxOf { row -> countCellsEnergisedByStartingAt(grid, row by16 grid.colIndices.last, Left) },
    grid.colIndices.maxOf { col -> countCellsEnergisedByStartingAt(grid, 0 by16 col, Down) },
    grid.colIndices.maxOf { col -> countCellsEnergisedByStartingAt(grid, grid.rowIndices.last by16 col, Up) }
)


private fun countCellsEnergisedByStartingAt(grid: Grid, startAt: Location1616, startHeading: Heading): Int {
    data class Work(val pos: Location1616, val heading: Heading)

    val work = ArrayDeque<Work>(listOf(Work(startAt, startHeading)))
    val visitedByDirection = Array(4) { Array(grid.height) { BooleanArray(grid.width) } }
        .apply { this[startHeading.ordinal][startAt.row()][startAt.col()] = true }

    fun visit(next: Location1616, heading: Heading) {
        if (next.row() in grid.rowIndices && next.col() in grid.colIndices) {
            if (!visitedByDirection[heading.ordinal][next.row()][next.col()]) {
                visitedByDirection[heading.ordinal][next.row()][next.col()] = true
                work.addLast(Work(next, heading))
            }
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
    return grid.mapCartesianNotNull { row, col, _ -> (0..3).any { visitedByDirection[it][row][col] } }.count { it }
}
