package ec2024.day13

import common.ThreePartChallenge
import common.TreeStack
import common.benchmark
import common.fromClasspathFileToLines
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.test.assertEquals

private const val rootFolder = "ec2024/day13"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day13.assertPart1Correct()
    Day13.assertPart2Correct()
    Day13.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 49µs
    benchmark { part2(puzzle2Input) } // 520µs
    benchmark(100) { part3(puzzle3Input) } // 5.8ms
}

internal object Day13 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(28, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(143, it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(28, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(600, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(8, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(539, it) }
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
    val heap = TreeStack<Tile>(heuristic)
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
