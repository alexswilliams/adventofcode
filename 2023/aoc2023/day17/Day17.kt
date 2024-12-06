package aoc2023.day17

import common.*
import kotlin.math.*


private val examples = loadFilesToGrids("aoc2023/day17", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToGrids("aoc2023/day17", "input.txt")

internal fun main() {
    Day17.assertCorrect()
    benchmark(10) { part1(puzzles[0]) } // 99ms
    benchmark(10) { part2(puzzles[0]) } // 1.1s
}

internal object Day17 : Challenge {
    override fun assertCorrect() {
        check(102, "P1 Example 1") { part1(examples[0]) }
        check(9, "P1 Example 3") { part1(examples[2]) } // from a reddit comment
        check(684, "P1 Puzzle") { part1(puzzles[0]) }

        check(94, "P2 Example 1") { part2(examples[0]) }
        check(71, "P2 Example 2") { part2(examples[1]) }
        check(40, "P2 Example 3") { part2(examples[2]) }
        check(0, "P2 Puzzle") { part2(puzzles[0]) } // 820 is too low, 834 is too high
    }
}

private enum class Heading(val nextCell: (Location1616) -> Location1616) {
    Up({ it.minusRow() }),
    Down({ it.plusRow() }),
    Left({ it.minusCol() }),
    Right({ it.plusCol() });

    fun normalNeighbours() = when (this) {
        Up -> arrayOf(Left, Right, Up)
        Down -> arrayOf(Left, Right, Down)
        Left -> arrayOf(Up, Down, Left)
        Right -> arrayOf(Up, Down, Right)
    }

    fun saturatedNeighbours() = when (this) {
        Up, Down -> arrayOf(Left, Right)
        Left, Right -> arrayOf(Up, Down)
    }

}

private fun part1(grid: Grid): Int {
    val start = 0 by16 0
    val target = (grid.height - 1) by16 (grid.width - 1)

    data class Work(val pos: Location1616, val heading: Heading, val steps: Int)

    val work = TreeQueue<Work> { manhattanDistance(target, it.pos) }.apply {
        offer(Work(start, Heading.Right, 0), 0)
        offer(Work(start, Heading.Down, 0), 0)
    }
    val shortest = hashMapOf<Work, Int>().apply {
        put(Work(start, Heading.Right, 0), 0)
        put(Work(start, Heading.Down, 0), 0)
    }

    while (true) {
        val u = work.poll() ?: throw Error("Map explored with no route to target")
        val totalHeatLoss = shortest[u]!!
        if (u.pos == target) return totalHeatLoss

        val neighbours = when (u.steps) {
            0, 1 -> u.heading.normalNeighbours()
            2 -> u.heading.saturatedNeighbours()
            else -> throw Error()
        }
        for (newHeading in neighbours) {
            val n = newHeading.nextCell(u.pos)
            if (n.row() !in grid.rowIndices || n.col() !in grid.colIndices) continue

            val newVisitedKey = Work(n, newHeading, if (newHeading == u.heading) u.steps + 1 else 0)
            val newDistance = totalHeatLoss + grid[n.row()][n.col()].digitToInt()
            val oldDistance = shortest[newVisitedKey] ?: Int.MAX_VALUE
            if (newDistance < oldDistance) {
                shortest[newVisitedKey] = newDistance
                work.offer(Work(n, newHeading, newVisitedKey.steps), newDistance)
            }
        }
    }
}

private fun manhattanDistance(a: Location1616, b: Location1616): Int = abs(a.row() - b.row()) + abs(a.col() - b.col())

private fun part2(grid: Grid): Int {
    val start = 0 by16 0
    val target = (grid.height - 1) by16 (grid.width - 1)

    data class Work(val pos: Location1616, val heading: Heading, val steps: Int)

    val work = TreeQueue<Work> { manhattanDistance(target, it.pos) }.apply {
        offer(Work(start, Heading.Right, 0), 0)
        offer(Work(start, Heading.Down, 0), 0)
    }
    val shortest = hashMapOf<Work, Pair<Int, List<Work>>>().apply {
        put(Work(start, Heading.Right, 0), 0 to listOf(Work(start, Heading.Right, 0)))
        put(Work(start, Heading.Down, 0), 0 to listOf(Work(start, Heading.Down, 0)))
    }

    while (true) {
        val u = work.poll() ?: throw Error("Map explored with no route to target")
        val totalHeatLoss = shortest[u]!!
        if (u.pos == target && u.steps >= 3) return totalHeatLoss.first

        val neighbours = when (u.steps) {
            in 0..<3 -> arrayOf(u.heading)
            9 -> u.heading.saturatedNeighbours()
            else -> u.heading.normalNeighbours()
        }
        for (newHeading in neighbours) {
            val n = newHeading.nextCell(u.pos)
            if (n.row() !in grid.rowIndices || n.col() !in grid.colIndices) continue

            val newVisitedKey = Work(n, newHeading, if (newHeading == u.heading) u.steps + 1 else 0)
            val newDistance = totalHeatLoss.first + grid[n.row()][n.col()].digitToInt()
            val oldDistance = shortest[newVisitedKey]?.first ?: Int.MAX_VALUE
            if (newDistance < oldDistance) {
                shortest[newVisitedKey] = newDistance to totalHeatLoss.second.plus(newVisitedKey)
                work.offer(Work(n, newHeading, newVisitedKey.steps), newDistance)
            }
        }
    }
}

