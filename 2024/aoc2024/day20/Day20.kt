package aoc2024.day20

import common.*
import kotlinx.collections.immutable.*
import kotlin.math.*

private val example = loadFilesToGrids("aoc2024/day20", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2024/day20", "input.txt").single()

internal fun main() {
    Day20.assertCorrect()
    benchmark(100) { part1(puzzle, 100) } // 4.4ms
    benchmark(10) { part2(puzzle, 100) } // 215.1ms
}

internal object Day20 : Challenge {
    override fun assertCorrect() {
        check(5, "P1 Example") { part1(example, 20) }
        check(1409, "P1 Puzzle") { part1(puzzle, 100) }

        check(285, "P2 Example") { part2(example, 50) }
        check(1012821, "P2 Puzzle") { part2(puzzle, 100) }
    }
}

private fun part1(grid: Grid, threshold: Int) =
    countShortcuts(grid, timeSavingWhereCheatingBecomesMorallyJustifiable = threshold, tunnelRadius = 2)

private fun part2(grid: Grid, threshold: Int) =
    countShortcuts(grid, timeSavingWhereCheatingBecomesMorallyJustifiable = threshold, tunnelRadius = 20)


private fun countShortcuts(grid: Grid, timeSavingWhereCheatingBecomesMorallyJustifiable: Int, tunnelRadius: Int): Int {
    val start = grid.locationOf('S')
    val end = grid.locationOf('E')
    val basePath = aStarPath(start, end, grid)
    val baselineTime = basePath.size

    val cache = mutableMapOf<Location1616, Int>()
        .apply { basePath.forEachIndexed { timeToPos, pos -> this[pos] = baselineTime - timeToPos } }

    return basePath.asSequence()
        .flatMapIndexed { timeToPos, pos ->
            allFloorTilesWithin(grid, pos, tunnelRadius).map { it to (timeToPos + pos.manhattanTo(it)) }
        }
        .map { (pos, timeToPos) -> baselineTime - timeToPos - cache.getOrPut(pos) { aStarTime(pos, end, grid) } }
        .filter { it >= timeSavingWhereCheatingBecomesMorallyJustifiable }
        .groupingBy { it }.eachCount()
        .values.sum()
}

private fun allFloorTilesWithin(grid: Grid, pos: Location1616, radius: Int): List<Location1616> = buildList(radius * 4) {
    ((pos.row() - radius).coerceAtLeast(1)..(pos.row() + radius).coerceAtMost(grid.height - 2)).forEach { row ->
        val maxCol = (pos.col() + radius - (pos.row() - row)).coerceAtMost(grid.width - 2)
        val minCol = (pos.col() - radius + (pos.row() - row)).coerceAtLeast(1)
        (minCol..maxCol).forEach { col ->
            if ((pos.row() - row).absoluteValue + (pos.col() - col).absoluteValue in 2..radius) {
                if (grid[row][col] != '#') add(row by16 col)
            }
        }
    }
}


fun aStarTime(start: Location1616, end: Location1616, grid: Grid): Int {
    val work = TreeQueue(start to 0) { it.manhattanTo(end) }
    val lowestDistanceTo = Array(grid.height) { IntArray(grid.width) { Int.MAX_VALUE } }.apply { set(start, 0) }
    val neighbours = IntArray(4)
    while (true) {
        val u = work.poll() ?: error("No path from start to end")
        val uDistance = lowestDistanceTo.at(u)
        if (u == end) return uDistance
        for (n in neighboursOf(u, grid, '#', neighbours)) {
            if (n == -1) continue
            val oldNeighbourDistance = lowestDistanceTo.at(n)
            if (uDistance + 1 < oldNeighbourDistance) {
                lowestDistanceTo.set(n, uDistance + 1)
                work.offerOrReposition(n, oldNeighbourDistance, uDistance + 1)
            }
        }
    }
}

fun aStarPath(start: Location1616, end: Location1616, grid: Grid): List<Location1616> {
    val work = TreeQueue(start to 0) { it.manhattanTo(end) }
    val shortestPathTo = Array(grid.height) { Array<PersistentList<Location1616>?>(grid.width) { null } }.apply { set(start, persistentListOf(start)) }
    val neighbours = IntArray(4)
    while (true) {
        val u = work.poll() ?: error("No path from start to end")
        val pathToU = shortestPathTo.at(u)!!
        if (u == end) return pathToU
        for (n in neighboursOf(u, grid, '#', neighbours)) {
            if (n == -1) continue
            val oldPathToN = shortestPathTo.at(n)?.size ?: Int.MAX_VALUE
            if (pathToU.size + 1 < oldPathToN) {
                shortestPathTo.set(n, pathToU.plus(n))
                work.offerOrReposition(n, oldPathToN, pathToU.size + 1)
            }
        }
    }
}
