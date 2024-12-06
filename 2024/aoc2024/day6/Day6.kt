package aoc2024.day6

import aoc2024.day6.Facing.*
import common.*
import kotlinx.coroutines.*

private val example = loadFilesToGrids("aoc2024/day6", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2024/day6", "input.txt").single()

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzle) } // 71µs
    benchmark(1) { part2(puzzle) } // 129ms
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(41, "P1 Example") { part1(example) }
        check(4515, "P1 Puzzle") { part1(puzzle) }

        check(6, "P2 Example") { part2(example) }
        check(1309, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int = walk(grid).tilesCovered

private fun part2(grid: Grid): Int = runBlocking(Dispatchers.Default) {
    grid.allLocationOf('.')
        .map { async { walk(grid, it) } }.awaitAll()
        .count { it.loopFormed }
}


private data class Result(val tilesCovered: Int, val loopFormed: Boolean)

private fun walk(grid: Grid, blocker: Location1616? = null): Result {
    val start = grid.locationOf('^')
    val visited = Array(grid.height) { BooleanArray(grid.width) }.apply { this[start.row()][start.col()] = true }
    val visitedWithDirection = Array(Facing.entries.size) { Array(grid.height) { BooleanArray(grid.width) } }.apply { this[Up.ordinal][start.row()][start.col()] = true }

    var facing = Up
    var next = start
    var count = 1
    while (true) {
        val candidate = facing.nextCell(next, 1)
        if (!(candidate isWithin grid)) return Result(count, false)
        if (visitedWithDirection[facing.ordinal].at(candidate)) return Result(count, true)

        if (candidate == blocker || grid.at(candidate) == '#')
            facing = facing.turnRight()
        else {
            next = candidate
            if (!visited.at(next)) {
                visited[next.row()][next.col()] = true
                count++
            }
            visitedWithDirection[facing.ordinal][candidate.row()][candidate.col()] = true
        }
    }
}

private enum class Facing(val nextCell: (Location1616, Int) -> Location1616) {
    Up({ pos, amt -> pos.minusRow(amt) }),
    Down({ pos, amt -> pos.plusRow(amt) }),
    Left({ pos, amt -> pos.minusCol(amt) }),
    Right({ pos, amt -> pos.plusCol(amt) });

    fun turnRight() = when (this) {
        Up -> Right
        Right -> Down
        Down -> Left
        Left -> Up
    }
}
