package ec2025.samsproblem

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("samsproblem", "example1.txt")
private val puzzles = loadFilesToLines("samsproblem", "input1.txt")

internal fun main() {
    DaySam.assertCorrect()
    benchmark { solveLinear(puzzles[0]) } // 41.5µs
    benchmark { solveMaxHeightPath(puzzles[0]) } // 16.9µs
}

internal object DaySam : Challenge {
    override fun assertCorrect() {
        check(16, "Linear Example") { solveLinear(examples[0]) }
        check(16, "MaxHeight Example") { solveMaxHeightPath(examples[0]) }
        check(13, "Linear Puzzle") { solveLinear(puzzles[0]) }
        check(13, "MaxHeight Puzzle") { solveMaxHeightPath(puzzles[0]) }
    }
}

private fun solveLinear(input: List<String>): Int {
    val grid = parseGrid(input)
    val maxHeight = grid.maxOf { it.max() }
    return (grid[0][0]..maxHeight).first { height ->
        pathAvailable(grid, height, 0 by16 0, grid.lastIndex by16 grid[0].lastIndex)
    }
}

private fun pathAvailable(grid: List<List<Int>>, passableHeight: Int, start: Location1616, end: Location1616): Boolean {
    val heap = TreeQueue(start to 0) { it: Location1616 -> it.manhattanTo(end) }
    val shortestPath = Array(grid.size) { IntArray(grid[0].size) { Int.MAX_VALUE } }
        .apply { this[start.row()][start.col()] = 0 }

    while (true) {
        val u = heap.poll() ?: return false // all routes explored, no route to end
        val distanceToU = shortestPath[u.row()][u.col()]
        if (u == end) return true // route found

        for (n in neighboursOf(u, grid, passableHeight)) {
            val originalDistance = shortestPath[n.row()][n.col()]
            val newDistance = distanceToU + 1
            if (newDistance < originalDistance) {
                heap.offerOrReposition(n, originalDistance, newDistance)
                shortestPath[n.row()][n.col()] = newDistance
            }
        }
    }
}

private fun neighboursOf(pos: Location1616, grid: List<List<Int>>, passableHeight: Int) = buildList {
    if (pos.col() > 0 && grid[pos.row()][pos.col() - 1] <= passableHeight) add(pos.minusCol())
    if (pos.row() > 0 && grid[pos.row() - 1][pos.col()] <= passableHeight) add(pos.minusRow())
    if (pos.col() < grid[0].lastIndex && grid[pos.row()][pos.col() + 1] <= passableHeight) add(pos.plusCol())
    if (pos.row() < grid.lastIndex && grid[pos.row() + 1][pos.col()] <= passableHeight) add(pos.plusRow())
}


private fun solveMaxHeightPath(input: List<String>): Int {
    val grid = parseGrid(input)
    val start = 0 by16 0
    val end = grid.lastIndex by16 grid[0].lastIndex

    val heap = TreeQueue(start to grid[start.row()][start.col()])
    val lowestTimes = Array(grid.size) { IntArray(grid[0].size) { Int.MAX_VALUE } }
        .apply { this[start.row()][start.col()] = grid[start.row()][start.col()] }

    while (true) {
        var weight: Int
        val u = heap.poll { weight = it } ?: throw Error("No path to end square, shouldn't be possible")
        val lowestTimeToU = lowestTimes[u.row()][u.col()]
        if (u == end) return lowestTimeToU

        for (n in neighboursOf(u, grid)) {
            val bestTimeSoFar = lowestTimes[n.row()][n.col()]
            val newBestTime = max(grid[n.row()][n.col()], weight)
            if (newBestTime < bestTimeSoFar) {
                heap.offerOrReposition(n, bestTimeSoFar, newBestTime)
                lowestTimes[n.row()][n.col()] = newBestTime
            }
        }
    }
}

private fun neighboursOf(pos: Location1616, grid: List<List<Int>>) = buildList {
    if (pos.col() > 0) add(pos.minusCol())
    if (pos.row() > 0) add(pos.minusRow())
    if (pos.col() < grid[0].lastIndex) add(pos.plusCol())
    if (pos.row() < grid.lastIndex) add(pos.plusRow())
}

private fun parseGrid(input: List<String>) =
    input.splitOnSpaces().map { line -> line.filterNotBlank().map { it.toInt() } }
