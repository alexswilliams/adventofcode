package day23

import common.*
import day23.Directions.*
import kotlin.test.*

private val exampleInput = "day23/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day23/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 110
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 20

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 3849, took 24.29ms

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 995, took 3.16s
}

private fun part1(input: List<String>): Int {
    val initialElfPositions = parseInput(input)
    val (after10Rounds, _) = positionsAfterTurns(initialElfPositions, 10)

    val width = after10Rounds.maxOf { it.col } - after10Rounds.minOf { it.col } + 1
    val height = after10Rounds.maxOf { it.row } - after10Rounds.minOf { it.row } + 1
    return width * height - after10Rounds.size
}

private fun part2(input: List<String>): Int {
    val initialElfPositions = parseInput(input)
    val (_, turnsNeeded) = positionsAfterTurns(initialElfPositions, Int.MAX_VALUE)
    return Int.MAX_VALUE - turnsNeeded + 1
}


private fun parseInput(input: List<String>): List<Point2D> = input
    .flatMapIndexed { row, values -> values.mapIndexed { col, char -> if (char == '.') null else Point2D(row, col) } }
    .filterNotNull()


private enum class Directions { NORTH, SOUTH, WEST, EAST }

private val preferences = listOf(
    listOf(NORTH, SOUTH, WEST, EAST),
    listOf(SOUTH, WEST, EAST, NORTH),
    listOf(WEST, EAST, NORTH, SOUTH),
    listOf(EAST, NORTH, SOUTH, WEST),
)

private tailrec fun positionsAfterTurns(
    elfPositions: List<Point2D>,
    remainingRounds: Int,
    preferenceIndex: Int = 0,
): Pair<List<Point2D>, Int> {
    if (remainingRounds == 0) return elfPositions to 0
    val elfPositionSet = elfPositions.toSet()

    var needsToMove = 0
    val elvesNeedingToMove = Array(elfPositions.size) { index ->
        if (anyNearbySquareContainsElf(elfPositions[index], elfPositionSet)) elfPositions[index].also { needsToMove++ }
        else null
    }
    if (needsToMove == 0) return elfPositions to remainingRounds

    val elfMovementProposals = elvesNeedingToMove.withIndex().filter { it.value != null }
        .map { elf ->
            val current = elf.value!!
            elf.index to preferences[preferenceIndex].firstOrNull { direction ->
                when (direction) {
                    NORTH -> allAboveMatches(current.row, current.col) { it !in elfPositionSet }
                    SOUTH -> allBelowMatches(current.row, current.col) { it !in elfPositionSet }
                    WEST -> allWestOfMatches(current.row, current.col) { it !in elfPositionSet }
                    EAST -> allEastOfMatches(current.row, current.col) { it !in elfPositionSet }
                }
            }
        }.filter { it.second != null }
        .map { (index, direction) ->
            val current = elfPositions[index]
            index to when (direction) {
                NORTH -> current.copy(row = current.row - 1)
                SOUTH -> current.copy(row = current.row + 1)
                WEST -> current.copy(col = current.col - 1)
                EAST -> current.copy(col = current.col + 1)
                null -> throw Exception("Should be unreachable")
            }
        }.groupBy { it.second }
        .filterValues { it.size == 1 }
        .values
        .flatten()

    val newPositions = ArrayList(elfPositions)
    elfMovementProposals.forEach { (index, newPoint) -> newPositions[index] = newPoint }
    return positionsAfterTurns(newPositions, remainingRounds - 1, (preferenceIndex + 1) % 4)
}

private fun anyNearbySquareContainsElf(current: Point2D, elfPositions: Set<Point2D>) =
    (anyAboveMatches(current.row, current.col) { it in elfPositions }
            || anyBelowMatches(current.row, current.col) { it in elfPositions }
            || anyEastOfMatches(current.row, current.col) { it in elfPositions }
            || anyWestOfMatches(current.row, current.col) { it in elfPositions })

private inline fun allAboveMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col - 1)) && function(Point2D(row - 1, col)) && function(Point2D(row - 1, col + 1))

private inline fun allBelowMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row + 1, col - 1)) && function(Point2D(row + 1, col)) && function(Point2D(row + 1, col + 1))

private inline fun allEastOfMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col + 1)) && function(Point2D(row, col + 1)) && function(Point2D(row + 1, col + 1))

private inline fun allWestOfMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col - 1)) && function(Point2D(row, col - 1)) && function(Point2D(row + 1, col - 1))

private inline fun anyAboveMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col - 1)) || function(Point2D(row - 1, col)) || function(Point2D(row - 1, col + 1))

private inline fun anyBelowMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row + 1, col - 1)) || function(Point2D(row + 1, col)) || function(Point2D(row + 1, col + 1))

private inline fun anyEastOfMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col + 1)) || function(Point2D(row, col + 1)) || function(Point2D(row + 1, col + 1))

private inline fun anyWestOfMatches(row: Int, col: Int, crossinline function: (Point2D) -> Boolean): Boolean =
    function(Point2D(row - 1, col - 1)) || function(Point2D(row, col - 1)) || function(Point2D(row + 1, col - 1))


typealias Point2D = Long

val Point2D.row get():Int = (this shr 32).toInt()
val Point2D.col get():Int = this.toInt()
fun Point2D.copy(row: Int = this.row, col: Int = this.col): Point2D = Point2D(row, col)
fun Point2D(row: Int, col: Int): Point2D = (row.toLong() shl 32) or (col.toLong() and 0xffffffff)
