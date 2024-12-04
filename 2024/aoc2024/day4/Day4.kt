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
    grid.mapCartesianNotNull { row, col, char -> if (char == 'X') row by16 col else null }.sumOf {
        countTrue(
            grid.cellsEqualTo(MAS, startAt = it, offsets = LEFT),
            grid.cellsEqualTo(MAS, startAt = it, offsets = RIGHT),
            grid.cellsEqualTo(MAS, startAt = it, offsets = UP),
            grid.cellsEqualTo(MAS, startAt = it, offsets = DOWN),
            grid.cellsEqualTo(MAS, startAt = it, offsets = LEFT_UP),
            grid.cellsEqualTo(MAS, startAt = it, offsets = RIGHT_UP),
            grid.cellsEqualTo(MAS, startAt = it, offsets = LEFT_DOWN),
            grid.cellsEqualTo(MAS, startAt = it, offsets = RIGHT_DOWN),
        )
    }

private val MAS = charArrayOf('M', 'A', 'S')
private val LEFT_UP = listOf(-1 to -1, -2 to -2, -3 to -3)
private val RIGHT_UP = listOf(-1 to 1, -2 to 2, -3 to 3)
private val LEFT_DOWN = listOf(1 to -1, 2 to -2, 3 to -3)
private val RIGHT_DOWN = listOf(1 to 1, 2 to 2, 3 to 3)
private val LEFT = listOf(0 to -1, 0 to -2, 0 to -3)
private val RIGHT = listOf(0 to 1, 0 to 2, 0 to 3)
private val UP = listOf(-1 to 0, -2 to 0, -3 to 0)
private val DOWN = listOf(1 to 0, 2 to 0, 3 to 0)


private fun part2(grid: Grid): Int =
    grid.mapCartesianNotNull { row, col, char -> if (char == 'A' && row in 1..grid.height - 2 && col in 1..grid.width - 2) row by16 col else null }
        .count { a ->
            2 == countTrue(
                grid.cellsEqualTo(MS, startAt = a, offsets = LEFT_UP__RIGHT_DOWN),
                grid.cellsEqualTo(MS, startAt = a, offsets = LEFT_DOWN__RIGHT_UP),
                grid.cellsEqualTo(MS, startAt = a, offsets = RIGHT_UP__LEFT_DOWN),
                grid.cellsEqualTo(MS, startAt = a, offsets = RIGHT_DOWN__LEFT_UP)
            )
        }

private val MS = charArrayOf('M', 'S')
private val LEFT_UP__RIGHT_DOWN = listOf(-1 to -1, 1 to 1)
private val LEFT_DOWN__RIGHT_UP = listOf(1 to -1, -1 to 1)
private val RIGHT_UP__LEFT_DOWN = listOf(-1 to 1, 1 to -1)
private val RIGHT_DOWN__LEFT_UP = listOf(1 to 1, -1 to -1)


private fun countTrue(vararg condition: Boolean): Int = condition.count { it }

private fun Grid.cellsEqualTo(expected: CharArray, startAt: Location1616, offsets: List<Pair<Int, Int>>): Boolean {
    expected.forEachIndexed { index, ch ->
        val r = startAt.row() + offsets[index].first
        val c = startAt.col() + offsets[index].second
        if (r !in this.rowIndices || c !in this.colIndices) return false
        if (this[r][c] != ch) return false
    }
    return true
}
