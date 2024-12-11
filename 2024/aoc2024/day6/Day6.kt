package aoc2024.day6

import aoc2024.day6.Facing.*
import common.*
import kotlinx.coroutines.*
import java.util.HashSet.*

private val example = loadFilesToGrids("aoc2024/day6", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2024/day6", "input.txt").single()

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzle) } // 39µs
    benchmark(100) { part2(puzzle) } // 8.4ms single or 3.8ms parallel
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(41, "P1 Example") { part1(example) }
        check(4515, "P1 Puzzle") { part1(puzzle) }

        check(6, "P2 Example") { part2(example) }
        check(1309, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int = visitedOnWalk(grid, grid.locationOf('^')).countTrue()

private fun part2(grid: Grid): Int = runBlocking(Dispatchers.Default) {
    val start = grid.locationOf('^')
    val blockers = grid.allLocationOf('#')
    val blockersForRow = blockers.groupBy { it.row() }.mapValues { it.value.toIntArray().apply { sort() } }
    val blockersForCol = blockers.groupBy { it.col() }.mapValues { it.value.toIntArray().apply { sort() } }

    visitedOnWalk(grid, start).filterTrue()
//        .count { walkUntilLoop(grid, it, start, blockersForRow, blockersForCol) }
        .map { async { walkUntilLoop(grid, it, start, blockersForRow, blockersForCol) } }.awaitAll()
        .count { it }
}


private fun visitedOnWalk(grid: Grid, start: Location1616): BooleanGrid {
    val visited = Array(grid.height) { BooleanArray(grid.width) }.apply { this[start.row()][start.col()] = true }
    var facing = Up
    var next = start
    while (true) {
        val candidate = facing.advance(next)
        if (!(candidate isWithin grid)) return visited
        if (grid.at(candidate) == '#')
            facing = facing.turnRight()
        else {
            next = candidate
            if (!visited.at(next)) visited[next.row()][next.col()] = true
        }
    }
}

private fun walkUntilLoop(grid: Grid, blocker: Location1616, start: Location1616, rowBlockers: Map<Int, IntArray>, colBlockers: Map<Int, IntArray>): Boolean {
    val visitedWithDirectionSet = newHashSet<Int>(50)
    var facing = Up
    var next = start
    while (true) {
        val spaceBeforeNextBarrier = findSpaceBeforeNextBarrier(facing, next, rowBlockers, colBlockers)
        val candidate = orChooseBlocker(facing, next, blocker, spaceBeforeNextBarrier)
        if (!(candidate isWithin grid)) return false
        if (!visitedWithDirectionSet.add(candidate shl 2 or facing.ordinal)) return true
        facing = facing.turnRight()
        next = candidate
    }
}

private fun orChooseBlocker(facing: Facing, pos: Location1616, blocker: Location1616, proposed: Location1616): Location1616 {
    when (facing) {
        Up -> if (blocker.col() == pos.col() && (proposed == -1 || proposed.row() <= blocker.row()) && blocker.row() < pos.row()) return blocker.plusRow()
        Down -> if (blocker.col() == pos.col() && (proposed == -1 || proposed.row() >= blocker.row()) && blocker.row() > pos.row()) return blocker.minusRow()
        Left -> if (blocker.row() == pos.row() && (proposed == -1 || proposed.col() <= blocker.col()) && blocker.col() < pos.col()) return blocker.plusCol()
        Right -> if (blocker.row() == pos.row() && (proposed == -1 || proposed.col() >= blocker.col()) && blocker.col() > pos.col()) return blocker.minusCol()
    }
    return proposed
}

private fun findSpaceBeforeNextBarrier(facing: Facing, pos: Location1616, rowBlockers: Map<Int, IntArray>, colBlockers: Map<Int, IntArray>): Location1616 {
    return when (facing) {
        Up -> (colBlockers[pos.col()] ?: return -1).let { blockersInCol ->
            val indexOfNextBlocker = -blockersInCol.binarySearch(pos) - 2
            if (indexOfNextBlocker < 0) return -1
            blockersInCol[indexOfNextBlocker].plusRow()
        }

        Down -> (colBlockers[pos.col()] ?: return -1).let { blockersInCol ->
            val indexOfNextBlocker = -blockersInCol.binarySearch(pos) - 1
            if (indexOfNextBlocker > blockersInCol.lastIndex) return -1
            blockersInCol[indexOfNextBlocker].minusRow()
        }

        Left -> (rowBlockers[pos.row()] ?: return -1).let { blockersInRow ->
            val indexOfNextBlocker = -blockersInRow.binarySearch(pos) - 2
            if (indexOfNextBlocker < 0) return -1
            blockersInRow[indexOfNextBlocker].plusCol()
        }

        Right -> (rowBlockers[pos.row()] ?: return -1).let { blockersInRow ->
            val indexOfNextBlocker = -blockersInRow.binarySearch(pos) - 1
            if (indexOfNextBlocker > blockersInRow.lastIndex) return -1
            blockersInRow[indexOfNextBlocker].minusCol()
        }
    }
}

private enum class Facing {
    Up, Down, Left, Right;

    fun turnRight() = when (this) {
        Up -> Right
        Right -> Down
        Down -> Left
        Left -> Up
    }

    fun advance(pos: Location1616) = when (this) {
        Up -> pos.minusRow()
        Down -> pos.plusRow()
        Left -> pos.minusCol()
        Right -> pos.plusCol()
    }
}
