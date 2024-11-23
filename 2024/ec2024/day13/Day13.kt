package ec2024.day13

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("ec2024/day13", "example.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day13", "input.txt", "input2.txt", "input3.txt")

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

private data class Tile(val r: Int, val c: Int, val height: Int)

private fun part1(input: List<String>): Int = shortestPathLength(input)
private fun part2(input: List<String>): Int = shortestPathLength(input)
private fun part3(input: List<String>): Int = shortestPathLength(input)

private fun shortestPathLength(input: List<String>): Int =
    aStarSearch(
        starts = input.flatMapIndexed { row, line -> line.mapIndexedNotNull { col, ch -> if (ch == 'S') Tile(row, col, 0) else null } },
        end = input.flatMapIndexed { row, line -> line.mapIndexedNotNull { col, ch -> if (ch == 'E') Tile(row, col, 0) else null } }.single(),
        grid = input.mapIndexed { row, line ->
            line.mapIndexed { col, ch ->
                when (ch) {
                    in '0'..'9', 'S', 'E' -> Tile(row, col, ch.digitToIntOrNull() ?: 0)
                    else -> null
                }
            }
        })


private fun aStarSearch(starts: Collection<Tile>, end: Tile, grid: List<List<Tile?>>, heuristic: (Tile) -> Int = { manhattan(it, end) }): Int {
    val heap = TreeQueue<Tile>(heuristic)
    val shortestPath = Array(grid.size) { row -> IntArray(grid[0].size) { Int.MAX_VALUE } }
    starts.forEach {
        shortestPath[it.r][it.c] = 0
        heap.offer(it, weight = 0)
    }

    while (true) {
        val u = heap.poll() ?: throw Error("All routes explored with no solution")
        val distanceToU = shortestPath[u.r][u.c]
        if (u == end) return distanceToU

        for (n in neighboursOf(u, grid)) {
            val originalDistance = shortestPath[n.r][n.c]
            val newDistance = distanceBetween(u, n) + distanceToU + 1
            if (newDistance < originalDistance) {
                heap.offerOrReposition(n, originalDistance, newDistance)
                shortestPath[n.r][n.c] = newDistance
            }
        }
    }
}

private fun manhattan(t1: Tile, t2: Tile) = (t1.r - t2.r).absoluteValue + (t1.c - t2.c).absoluteValue + (t1.height - t2.height).absoluteValue

private fun neighboursOf(tile: Tile, grid: List<List<Tile?>>): Collection<Tile> = buildList(4) {
    if (tile.c > 0) grid[tile.r][tile.c - 1]?.also { add(it) }
    if (tile.c < grid[0].lastIndex) grid[tile.r][tile.c + 1]?.also { add(it) }
    if (tile.r > 0) grid[tile.r - 1][tile.c]?.also { add(it) }
    if (tile.r < grid.lastIndex) grid[tile.r + 1][tile.c]?.also { add(it) }
}

private fun distanceBetween(a: Tile, b: Tile): Int {
    val bigger = max(a.height, b.height)
    val smaller = min(a.height, b.height)
    return min(bigger - smaller, smaller + 10 - bigger)
}
