package ec2024.day13

import common.*
import kotlin.math.*

private val examples = loadFilesToGrids("ec2024/day13", "example.txt", "example3.txt")
private val puzzles = loadFilesToGrids("ec2024/day13", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day13.assertCorrect()
    benchmark { part1(puzzles[0]) } // 49µs
    benchmark { part2(puzzles[1]) } // 520µs
    benchmark(100) { part3(puzzles[2]) } // 5.8ms
}

internal object Day13 : Challenge {
    override fun assertCorrect() {
        check(28, "P1 Example") { part1(examples[0]) }
        check(143, "P1 Puzzle") { part1(puzzles[0]) }

        check(28, "P2 Example") { part2(examples[0]) }
        check(600, "P2 Puzzle") { part2(puzzles[1]) }

        check(8, "P3 Example") { part3(examples[1]) }
        check(539, "P3 Puzzle") { part3(puzzles[2]) }
    }
}

private fun part1(input: Grid): Int = shortestPathLength(input)
private fun part2(input: Grid): Int = shortestPathLength(input)
private fun part3(input: Grid): Int = shortestPathLength(input)

private fun shortestPathLength(input: Grid): Int =
    aStarSearch(
        starts = input.mapCartesianNotNull { row, col, char -> if (char == 'S') row by16 col else null },
        end = input.mapCartesianNotNull { row, col, char -> if (char == 'E') row by16 col else null }.single(),
        grid = input
    )


private fun aStarSearch(starts: Collection<Location1616>, end: Location1616, grid: Grid, heuristic: (Location1616) -> Int = { manhattan(it, end, grid) }): Int {
    val heap = TreeQueue<Location1616>(heuristic)
    val shortestPath = Array(grid.size) { row -> IntArray(grid[0].size) { Int.MAX_VALUE } }
    starts.forEach {
        shortestPath[it.row()][it.col()] = 0
        heap.offer(it, weight = 0)
    }

    while (true) {
        val u = heap.poll() ?: throw Error("All routes explored with no solution")
        val distanceToU = shortestPath[u.row()][u.col()]
        if (u == end) return distanceToU

        for (n in neighboursOf(u, grid)) {
            val originalDistance = shortestPath[n.row()][n.col()]
            val newDistance = distanceBetween(u, n, grid) + distanceToU + 1
            if (newDistance < originalDistance) {
                heap.offerOrReposition(n, originalDistance, newDistance)
                shortestPath[n.row()][n.col()] = newDistance
            }
        }
    }
}

private fun manhattan(t1: Location1616, t2: Location1616, grid: Grid) =
    (t1.row() - t2.row()).absoluteValue +
            (t1.col() - t2.col()).absoluteValue +
            (grid[t1.row()][t1.col()].heightOr0() - grid[t2.row()][t2.col()].heightOr0()).absoluteValue

private fun Char.heightOr0(): Int = this.digitToIntOrNull() ?: 0

private val allowed = "SE0123456789".toCharArray()

private fun neighboursOf(tile: Location1616, grid: Grid): Collection<Location1616> = buildList(4) {
    if (tile.col() > 0 && grid[tile.row()][tile.col() - 1] in allowed) add(tile.minusCol())
    if (tile.col() < grid[0].lastIndex && grid[tile.row()][tile.col() + 1] in allowed) add(tile.plusCol())
    if (tile.row() > 0 && grid[tile.row() - 1][tile.col()] in allowed) add(tile.minusRow())
    if (tile.row() < grid.lastIndex && grid[tile.row() + 1][tile.col()] in allowed) add(tile.plusRow())
}

private fun distanceBetween(a: Location1616, b: Location1616, grid: Grid): Int {
    val heightA = grid[a.row()][a.col()].heightOr0()
    val heightB = grid[b.row()][b.col()].heightOr0()
    val bigger = max(heightA, heightB)
    val smaller = min(heightA, heightB)
    return min(bigger - smaller, smaller + 10 - bigger)
}
