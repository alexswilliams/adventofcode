package ec2024.day13

import common.*
import java.util.*
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
    benchmark { part1(puzzleInput) } // 48µs
    benchmark(100) { part2(puzzle2Input) } // 1.0ms
    benchmark(100) { part3(puzzle3Input) } // 21.2ms
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

private data class Tile(val r: Int, val c: Int, val height: Int, val distToTarget: Int, val pos: Location = r by c) {
    override fun equals(other: Any?) = (pos == (other as? Tile)?.pos) == true
    override fun hashCode(): Int = pos.hashCode()
}

private fun part1(input: List<String>): Int {
    val allStarts = input.flatMapIndexed { row, it -> it.mapIndexedNotNull { col, ch -> if (ch == 'S') row by col else null } }
    val end = input.flatMapIndexed { row, it -> it.mapIndexedNotNull { col, ch -> if (ch == 'E') row by col else null } }.single()
    val grid = input.mapIndexed { row, string ->
        string.mapIndexed { col, ch ->
            when (ch) {
                'S', 'E' -> Tile(row, col, 0, mhd(row by col, end))
                in '0'..'9' -> Tile(row, col, ch.digitToInt(), mhd(row by col, end))
                else -> null
            }
        }
    }
    return dijkstra(allStarts, end, grid)
}

fun mhd(a: Location, b: Location) = (a.row() - b.row()).absoluteValue.toInt() + (a.col() - b.col()).absoluteValue.toInt()

private fun part2(input: List<String>): Int = part1(input)
private fun part3(input: List<String>): Int = part1(input)

private fun dijkstra(starts: Collection<Location>, end: Location, grid: List<List<Tile?>>): Int {
    val smallestDistance = Array(grid.size) { row -> IntArray(grid[0].size) { Int.MAX_VALUE } }
        .apply { starts.forEach { location -> this[location.rowInt()][location.colInt()] = 0 } }
    val visited = Array(grid.size) { BooleanArray(grid[0].size) }
        .apply { starts.forEach { location -> this[location.rowInt()][location.colInt()] = true } }

    val priorityQueue = PriorityQueue<Tile> { a, b -> (smallestDistance[a.r][a.c] + a.distToTarget).compareTo(smallestDistance[b.r][b.c] + b.distToTarget) }
        .apply { starts.forEach { start -> offer(Tile(start.rowInt(), start.colInt(), 0, mhd(start, end))) } }

    while (true) {
        val u = priorityQueue.poll()
        val distanceSoFar = smallestDistance[u.r][u.c]
        if (u.pos == end) return distanceSoFar
        for ((n, distance) in neighboursOf(u, grid)) {
            if (visited[n.r][n.c]) continue
            val candidateDistance = distance + distanceSoFar + 1
            if (smallestDistance[n.r][n.c] > candidateDistance) {
                smallestDistance[n.r][n.c] = candidateDistance
                priorityQueue.remove(n)
                priorityQueue.offer(n)
            }
        }
        visited[u.r][u.c] = true
    }
    throw Error()
}


private fun neighboursOf(tile: Tile, grid: List<List<Tile?>>): Collection<Pair<Tile, Int>> = buildList {
    if (tile.c > 0) grid[tile.r][tile.c - 1]?.let { add(it to stepsBetween(tile, it)) }
    if (tile.c < grid[0].lastIndex) grid[tile.r][tile.c + 1]?.let { add(it to stepsBetween(tile, it)) }
    if (tile.r > 0) grid[tile.r - 1][tile.c]?.let { add(it to stepsBetween(tile, it)) }
    if (tile.r < grid.lastIndex) grid[tile.r + 1][tile.c]?.let { add(it to stepsBetween(tile, it)) }
}

private fun stepsBetween(a: Tile, b: Tile): Int {
    val bigger = max(a.height, b.height)
    val smaller = min(a.height, b.height)
    return min(bigger - smaller, smaller + 10 - bigger)
}
