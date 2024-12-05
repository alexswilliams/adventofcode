package aoc2024.day4

import common.*

private val example = loadFilesToGrids("aoc2024/day4", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2024/day4", "input.txt").single()

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzle) } // 444µs
    benchmark { part2(puzzle) } // 209µs
}

internal object Day4 : Challenge {
    override fun assertCorrect() {
        check(18, "P1 Example") { part1(example) }
        check(2575, "P1 Puzzle") { part1(puzzle) }

        check(9, "P2 Example") { part2(example) }
        check(2041, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int =
    grid.mapCartesianNotNull { row, col, char -> if (char == 'X') row by16 col else null }
        .sumOf { center -> ALL_8_DIRECTIONS.count { grid.cellsEqualTo("MAS", center, it) } }

private fun part2(grid: Grid): Int =
    grid.mapCartesianNotNull { row, col, char -> if (char == 'A' && row in 1..grid.height - 2 && col in 1..grid.width - 2) row by16 col else null }
        .count { center -> 2 == DIAG_OPPOSITES.count { grid.cellsEqualTo("MS", center, it) } }


private val DIAGONALS = (0..3).map { rotation -> (1..3).map { rotateACW(it to it, rotation) } }
private val HORIZ_VERT = (0..3).map { rotation -> (1..3).map { rotateACW(0 to it, rotation) } }
private val ALL_8_DIRECTIONS = DIAGONALS + HORIZ_VERT
private val DIAG_OPPOSITES = (0..3).map { rotation -> listOf(-1, 1).map { rotateACW(it to it, rotation) } }


private tailrec fun rotateACW(a: Pair<Int, Int>, times: Int = 1): Pair<Int, Int> =
    if (times == 0) a
    else rotateACW(-a.second to a.first, times = times - 1)

private fun Grid.cellsEqualTo(expected: String, startAt: Location1616, offsets: List<Pair<Int, Int>>): Boolean {
    expected.forEachIndexed { index, ch ->
        val r = startAt.row() + offsets[index].first
        val c = startAt.col() + offsets[index].second
        if (r !in this.rowIndices || c !in this.colIndices) return false
        if (this[r][c] != ch) return false
    }
    return true
}
