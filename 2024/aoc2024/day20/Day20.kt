package aoc2024.day20

import common.*
import kotlin.math.*

private val example = loadFilesToGrids("aoc2024/day20", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2024/day20", "input.txt").single()

internal fun main() {
    Day20.assertCorrect()
    benchmark(100) { part1(puzzle, 100) } // 703µs
    benchmark(100) { part2(puzzle, 100) } // 30.6ms
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
    val basePath = snakePath(start, end, grid)
    val baselineTime = basePath.size

    val cache = Array(grid.height) { IntArray(grid.width) { Int.MAX_VALUE } }
        .apply { basePath.forEachIndexed { timeToPos, pos -> this.set(pos, baselineTime - timeToPos) } }

    val neighbours = IntArray(4 * tunnelRadius * (tunnelRadius + 1) / 2 + 1)
    return basePath.sumOfIndexed { timeToTunnelStart, tunnelStart ->
        var shortcutCount = 0
        for (tunnelEnd in allFloorTilesWithin(grid, tunnelStart, tunnelRadius, neighbours)) {
            if (tunnelEnd == -1) break
            if (baselineTime - (timeToTunnelStart + tunnelStart.manhattanTo(tunnelEnd) + cache.at(tunnelEnd)) >= timeSavingWhereCheatingBecomesMorallyJustifiable)
                shortcutCount++
        }
        shortcutCount
    }
}

private fun allFloorTilesWithin(grid: Grid, pos: Location1616, radius: Int, neighbours: IntArray): IntArray {
    var i = 0
    val minRow = (pos.row() - radius).coerceAtLeast(1)
    val maxRow = (pos.row() + radius).coerceAtMost(grid.height - 2)
    (minRow..maxRow).forEach { row ->
        val maxCol = (pos.col() + radius - (pos.row() - row)).coerceAtMost(grid.width - 2)
        val minCol = (pos.col() - radius + (pos.row() - row)).coerceAtLeast(1)
        (minCol..maxCol).forEach { col ->
            if ((pos.row() - row).absoluteValue + (pos.col() - col).absoluteValue in 2..radius) {
                if (grid[row][col].isFloor()) neighbours[i++] = (row by16 col)
            }
        }
    }
    return neighbours.also { neighbours[i] = -1 }
}

private fun snakePath(start: Location1616, end: Location1616, grid: Grid): List<Location1616> {
    return buildList(grid.height * grid.width / 2) {
        var current = start
        var previous = start
        add(start)
        do {
            val tmp = current
            current = when {
                current.plusRow() != previous && grid.at(current.plusRow()).isFloor() -> current.plusRow()
                current.minusRow() != previous && grid.at(current.minusRow()).isFloor() -> current.minusRow()
                current.plusCol() != previous && grid.at(current.plusCol()).isFloor() -> current.plusCol()
                current.minusCol() != previous && grid.at(current.minusCol()).isFloor() -> current.minusCol()
                else -> error("Unexpected end of grid")
            }
            add(current)
            previous = tmp
        } while (current != end)
    }
}

private fun Char.isFloor(): Boolean = this == '.' || this == 'E'
