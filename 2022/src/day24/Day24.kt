package day24

import common.*
import java.util.*
import kotlin.math.*
import kotlin.test.*

private val exampleInput = "day24/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day24/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 18
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 54

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 301, took 262.52ms

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 859, took 624.33ms
}

private fun part1(input: List<String>): Int {
    val start = Point2D(0, input.first().indexOf('.'))
    val target = Point2D(input.lastIndex, input.last().indexOf('.'))
    return turnsForRoute(input, start, target, 0)
}

private fun part2(input: List<String>): Int {
    val start = Point2D(0, input.first().indexOf('.'))
    val target = Point2D(input.lastIndex, input.last().indexOf('.'))
    return turnsForRoute(input, start, target, turnsForRoute(input, target, start, turnsForRoute(input, start, target, 0)))
}

private fun turnsForRoute(input: List<String>, start: Point2D, target: Point2D, startTime: Int): Int {
    val height = input.size - 2
    val width = input[0].length - 2
    val allRows = 1..height
    val allCols = 1..width

    val leftMoving = input.map { row -> row.mapIndexedNotNull { index, c -> if (c == '<') index - 1 else null } }
    val rightMoving = input.map { row -> row.mapIndexedNotNull { index, c -> if (c == '>') index - 1 else null } }
    val inputTransposed = input.transposeToChars()
    val upwardMoving = inputTransposed.map { col -> col.mapIndexedNotNull { index, c -> if (c == '^') index - 1 else null } }
    val downwardMoving = inputTransposed.map { col -> col.mapIndexedNotNull { index, c -> if (c == 'v') index - 1 else null } }

    val workQueue = PriorityQueue<Pair<Int, Point2D>> { o1, o2 -> (heuristic(o1, target) - heuristic(o2, target)).sign }
    val seen = HashSet<Pair<Int, Point2D>>()
    workQueue.offer(startTime to start)

    do {
        val (oldTime, pos) = workQueue.poll()
        if (pos == target) return oldTime
        val time = oldTime + 1

        val proposals = listOf(
            pos,
            pos.copy(row = pos.row - 1),
            pos.copy(row = pos.row + 1),
            pos.copy(col = pos.col - 1),
            pos.copy(col = pos.col + 1)
        )
            .filter { it == start || it == target || (it.row in allRows && it.col in allCols) }
            .filter { toTest ->
                !leftMoving[toTest.row].any { rotatedDown(it, time, width) == toTest.col }
                        && !rightMoving[toTest.row].any { rotatedUp(it, time, width) == toTest.col }
                        && !upwardMoving[toTest.col].any { rotatedDown(it, time, height) == toTest.row }
                        && !downwardMoving[toTest.col].any { rotatedUp(it, time, height) == toTest.row }
            }.filter { ((time to it) !in seen) }

        proposals.forEach {
            if (it == target) return time
            workQueue.offer(time to it)
            seen.add(time to it)
        }
    } while (workQueue.isNotEmpty())
    throw Exception("No solution found")
}

private fun rotatedUp(it: Int, time: Int, width: Int) = (it + time) % width + 1

private fun rotatedDown(it: Int, time: Int, width: Int) = (it - (time % width) + width) % width + 1

private fun heuristic(o1: Pair<Int, Point2D>, target: Point2D) = o1.first + manhattan(o1.second, target)

private fun manhattan(a: Point2D, b: Point2D): Int = (a.row - b.row).absoluteValue + (a.col - b.col).absoluteValue


private typealias Point2D = Int

private val Point2D.row get():Int = this shr 8
private val Point2D.col get():Int = this.toByte().toInt()
private fun Point2D.copy(row: Int = this.row, col: Int = this.col): Point2D = Point2D(row, col)
private fun Point2D.asString() = "↓$row→$col"
private fun Point2D(row: Int, col: Int): Point2D = (row shl 8) or (col and 0x000000ff)
