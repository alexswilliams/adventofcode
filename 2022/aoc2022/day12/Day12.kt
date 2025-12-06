package aoc2022.day12

import common.*
import java.util.*


private val example = loadFiles("aoc2022/day12", "example.txt").single()
private val puzzle = loadFiles("aoc2022/day12", "input.txt").single()

internal fun main() {
    Day12.assertCorrect()
    benchmark { part1(puzzle) } // 2.0ms
    benchmark { part2(puzzle) } // 1.6ms
}

internal object Day12 : Challenge {
    override fun assertCorrect() {
        check(31, "P1 Example") { part1(example) }
        check(391, "P1 Puzzle") { part1(puzzle) }

        check(29, "P2 Example") { part2(example) }
        check(386, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: String) = distanceByDijkstra(input, startAtAny = 'S', endAtAny = 'E') { next, now -> next - now <= 1 }
private fun part2(input: String) = distanceByDijkstra(input, startAtAny = 'E', endAtAny = 'a') { next, now -> next - now >= -1 }


private fun distanceByDijkstra(input: String, startAtAny: Char, endAtAny: Char, isMoveAllowed: (Char, Char) -> Boolean): Int {
    val grid = input.filter { it.isLetter() }.toCharArray()
    val columnCount = grid.size / (input.length - grid.size + 1)

    val smallestDistance = IntArray(grid.size) { if (grid[it] == startAtAny) 0 else Int.MAX_VALUE }
    val priorityQueue = PriorityQueue<Int>(grid.size) { o1, o2 -> smallestDistance[o1].compareTo(smallestDistance[o2]) }
        .apply { grid.indices.forEach(::offer) }

    while (true) {
        val u = priorityQueue.poll()
        if (grid[u] == endAtAny) return smallestDistance[u]

        val distanceViaU = smallestDistance[u] + 1
        for (n in neighboursOf(u, columnCount, grid, isMoveAllowed)) {
            if (distanceViaU < smallestDistance[n]) {
                smallestDistance[n] = distanceViaU
                // Java's PriorityQueue can't decrease an existing key; delete and re-add is (much) slower but equivalent
                priorityQueue.remove(n)
                priorityQueue.offer(n)
            }
        }
    }
}

private fun neighboursOf(idx: Int, cols: Int, grid: CharArray, isMoveAllowed: (Char, Char) -> Boolean): Iterable<Int> {
    val col = idx % cols
    val thisHeight = heightOf(grid[idx])
    return ArrayList<Int>(4).apply {
        if (col > 0 && isMoveAllowed(heightOf(grid[idx - 1]), thisHeight)) add(idx - 1)
        if (col < cols - 1 && isMoveAllowed(heightOf(grid[idx + 1]), thisHeight)) add(idx + 1)
        if (idx >= cols && isMoveAllowed(heightOf(grid[idx - cols]), thisHeight)) add(idx - cols)
        if (idx < grid.size - cols && isMoveAllowed(heightOf(grid[idx + cols]), thisHeight)) add(idx + cols)
    }
}

private fun heightOf(c: Char) =
    when (c) {
        'S' -> 'a'
        'E' -> 'z'
        else -> c
    }
