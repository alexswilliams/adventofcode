package aoc2022.day24

import common.*
import java.util.*
import kotlin.math.*
import kotlin.test.*
import kotlin.time.*

private val exampleInput = "aoc2022/day24/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day24/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 18
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 54

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 301, took 133.54ms

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 859, took 267.42ms

    println(measureTime { repeat(10) { part1(puzzleInput) } }.div(10))
    println(measureTime { repeat(10) { part2(puzzleInput) } }.div(10))
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

    val leftMoving = input.map { row -> BooleanArray(row.length) { row[it] == '<' } }
    val rightMoving = input.map { row -> BooleanArray(row.length) { row[it] == '>' } }
    val inputTransposed = input.transposeToChars()
    val upwardMoving = inputTransposed.map { col -> BooleanArray(col.size) { col[it] == '^' } }
    val downwardMoving = inputTransposed.map { col -> BooleanArray(col.size) { col[it] == 'v' } }

    val workQueue = PriorityQueue<Pair<Int, Point2D>> { o1, o2 -> (heuristic(o1, target) - heuristic(o2, target)).sign }
    val seen = HashSet<Pair<Int, Point2D>>(2000)
    workQueue.offer(startTime to start)

    do {
        val (oldTime, pos) = workQueue.poll() ?: throw Exception("No solution found")
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
                val colRotatedLeft = (toTest.col - 1 + time + width) % width + 1
                val colRotatedRight = (toTest.col - 1 - (time % width) + width) % width + 1
                val rowRotatedUp = (toTest.row - 1 + time + height) % height + 1
                val rowRotatedDown = (toTest.row - 1 - (time % height) + height) % height + 1
                !leftMoving[toTest.row][colRotatedLeft]
                        && !rightMoving[toTest.row][colRotatedRight]
                        && !upwardMoving[toTest.col][rowRotatedUp]
                        && !downwardMoving[toTest.col][rowRotatedDown]
            }.filter { ((time to it) !in seen) }

        proposals.forEach {
            if (it == target) return time
            workQueue.offer(time to it)
            seen.add(time to it)
        }
    } while (true)
}

private fun heuristic(o1: Pair<Int, Point2D>, target: Point2D) = o1.first + manhattan(o1.second, target)

private fun manhattan(a: Point2D, b: Point2D): Int = (a.row - b.row).absoluteValue + (a.col - b.col).absoluteValue


private typealias Point2D = Int

private val Point2D.row get():Int = this shr 8
private val Point2D.col get():Int = this.toByte().toInt()
private fun Point2D.copy(row: Int = this.row, col: Int = this.col): Point2D = Point2D(row, col)
private fun Point2D(row: Int, col: Int): Point2D = (row shl 8) or (col and 0x000000ff)
