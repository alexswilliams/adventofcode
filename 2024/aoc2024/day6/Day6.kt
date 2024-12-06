package aoc2024.day6

import aoc2024.day6.Facing.*
import common.*
import kotlinx.coroutines.*

private val example = loadFilesToGrids("aoc2024/day6", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2024/day6", "input.txt").single()

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzle) } // 67µs
    benchmark(50) { part2(puzzle) } // 20ms
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(41, "P1 Example") { part1(example) }
        check(4515, "P1 Puzzle") { part1(puzzle) }

        check(6, "P2 Example") { part2(example) }
        check(1309, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int = visitedOnWalk(grid).countTrue()

private fun part2(grid: Grid): Int = runBlocking(Dispatchers.Default) {
    val start = grid.locationOf('^')
    visitedOnWalk(grid).filterTrue()
        .map { async { walkUntilLoop(grid, it, start) } }.awaitAll()
        .count { it }
}


private fun visitedOnWalk(grid: Grid, start: Location1616 = grid.locationOf('^')): BooleanGrid {
    val visited = Array(grid.height) { BooleanArray(grid.width) }.apply { this[start.row()][start.col()] = true }
    var facing = Up
    var next = start
    while (true) {
        val candidate = facing.nextCell(next, 1)
        if (!(candidate isWithin grid)) return visited
        if (grid.at(candidate) == '#')
            facing = facing.turnRight()
        else {
            next = candidate
            if (!visited.at(next)) visited[next.row()][next.col()] = true
        }
    }
}

private fun walkUntilLoop(grid: Grid, blocker: Location1616, start: Location1616 = grid.locationOf('^')): Boolean {
    val visitedWithDirection = Array(Facing.entries.size) { Array(grid.height) { BooleanArray(grid.width) } }.apply { this[Up.ordinal][start.row()][start.col()] = true }
    var facing = Up
    var next = start
    while (true) {
        val candidate = facing.nextCell(next, 1)
        if (!(candidate isWithin grid)) return false
        if (visitedWithDirection[facing.ordinal].at(candidate)) return true
        if (candidate == blocker || grid.at(candidate) == '#')
            facing = facing.turnRight()
        else {
            next = candidate
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
