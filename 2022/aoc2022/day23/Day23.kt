package aoc2022.day23

import aoc2022.day23.Direction.*
import common.*
import kotlin.test.*

private val exampleInput = "aoc2022/day23/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day23/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 110
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 20

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 3849, took 5.63ms

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 995, took 426.31ms
}

private fun part1(input: List<String>): Int {
    val initialElfPositions = parseInput(input).toIntArray()
    val (after10Rounds, _) = positionsAfterTurns(initialElfPositions, 10)

    val width = after10Rounds.maxOf { it.col } - after10Rounds.minOf { it.col } + 1
    val height = after10Rounds.maxOf { it.row } - after10Rounds.minOf { it.row } + 1
    return width * height - after10Rounds.size
}

private fun part2(input: List<String>): Int {
    val initialElfPositions = parseInput(input).toIntArray()
    val (_, turnsNeeded) = positionsAfterTurns(initialElfPositions, Int.MAX_VALUE)
    return Int.MAX_VALUE - turnsNeeded + 1
}


private fun parseInput(input: List<String>): List<Point2D> = input
    .flatMapIndexed { row, values -> values.mapIndexed { col, char -> if (char == '.') null else Point2D(row, col) } }
    .filterNotNull()


private enum class Direction { NORTH, SOUTH, WEST, EAST }

private val preferences = listOf(
    listOf(NORTH, SOUTH, WEST, EAST),
    listOf(SOUTH, WEST, EAST, NORTH),
    listOf(WEST, EAST, NORTH, SOUTH),
    listOf(EAST, NORTH, SOUTH, WEST),
)

private tailrec fun positionsAfterTurns(
    elfPositions: IntArray,
    remainingRounds: Int,
    preferenceIndex: Int = 0,
    elfPositionSet: Set<Point2D> = elfPositions.toSet()
): Pair<IntArray, Int> {
    if (remainingRounds == 0) return elfPositions to 0

    val seen = HashSet<Point2D>(elfPositions.size * 2, 0.75f)
    val clashes = HashSet<Point2D>(elfPositions.size / 4)

    var needsToMove = 0
    val elvesNeedingToMove = Array(elfPositions.size) { index ->
        if (!anyNearbySquareContainsElf(elfPositions[index], elfPositionSet)) null else {
            needsToMove++
            val current = elfPositions[index]
            preferences[preferenceIndex].firstOrNull { direction ->
                when (direction) {
                    NORTH -> allAboveMatches(current.row, current.col) { it !in elfPositionSet }
                    SOUTH -> allBelowMatches(current.row, current.col) { it !in elfPositionSet }
                    WEST -> allWestOfMatches(current.row, current.col) { it !in elfPositionSet }
                    EAST -> allEastOfMatches(current.row, current.col) { it !in elfPositionSet }
                }
            }?.let {
                when (it) {
                    NORTH -> current.copy(row = current.row - 1)
                    SOUTH -> current.copy(row = current.row + 1)
                    WEST -> current.copy(col = current.col - 1)
                    EAST -> current.copy(col = current.col + 1)
                }
            }?.also {
                if (!seen.add(it)) clashes.add(it)
            }
        }
    }
    if (needsToMove == 0) return elfPositions to remainingRounds
    seen.removeAll(clashes)

    elvesNeedingToMove.forEachIndexed { index, it ->
        if (it != null && it !in clashes) elfPositions[index] = it
        else seen.add(elfPositions[index])
    }

    return positionsAfterTurns(elfPositions, remainingRounds - 1, (preferenceIndex + 1) % 4, seen)
}

private fun anyNearbySquareContainsElf(current: Point2D, elfPositions: Set<Point2D>) =
    Point2D(current.row - 1, current.col - 1) in elfPositions
            || Point2D(current.row - 1, current.col) in elfPositions
            || Point2D(current.row - 1, current.col + 1) in elfPositions
            || Point2D(current.row, current.col - 1) in elfPositions
            || Point2D(current.row, current.col + 1) in elfPositions
            || Point2D(current.row + 1, current.col - 1) in elfPositions
            || Point2D(current.row + 1, current.col) in elfPositions
            || Point2D(current.row + 1, current.col + 1) in elfPositions

private inline fun allAboveMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col - 1)) && function(Point2D(row - 1, col)) && function(Point2D(row - 1, col + 1))

private inline fun allBelowMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row + 1, col - 1)) && function(Point2D(row + 1, col)) && function(Point2D(row + 1, col + 1))

private inline fun allEastOfMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col + 1)) && function(Point2D(row, col + 1)) && function(Point2D(row + 1, col + 1))

private inline fun allWestOfMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col - 1)) && function(Point2D(row, col - 1)) && function(Point2D(row + 1, col - 1))


private typealias Point2D = Int

private val Point2D.row get():Int = this shr 8
private val Point2D.col get():Int = this.toByte().toInt()
private fun Point2D.copy(row: Int = this.row, col: Int = this.col): Point2D = Point2D(row, col)
private fun Point2D(row: Int, col: Int): Point2D = (row shl 8) or (col and 0x000000ff)
