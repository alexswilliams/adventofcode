package day9

import common.*
import day9.Coordinate.Companion.listOfOrigins
import kotlinx.collections.immutable.*
import kotlin.math.*
import kotlin.test.*

private val exampleInput = "day9/example.txt".fromClasspathFileToLines()
private val example2Input = "day9/example2.txt".fromClasspathFileToLines()
private val puzzleInput = "day9/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_ANSWER = 13
private const val PART_2_EXPECTED_ANSWER_1 = 1
private const val PART_2_EXPECTED_ANSWER_2 = 36

fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1: " + part1(puzzleInput)) // 5883

    assertEquals(PART_2_EXPECTED_ANSWER_1, part2(exampleInput))
    assertEquals(PART_2_EXPECTED_ANSWER_2, part2(example2Input))
    println("Part 2: " + part2(puzzleInput)) // 2367
}

private fun part1(input: List<String>) = tailPositionCount(input, 2)
private fun part2(input: List<String>) = tailPositionCount(input, 10)

private fun tailPositionCount(input: List<String>, numberOfKnots: Int) = input.splitOnSpaces()
    .fold(State(numberOfKnots)) { state, (direction, amount) -> state.move(direction.single(), amount.toInt()) }
    .visitedTailCoordinates.distinct().count()

private tailrec fun State.move(direction: Char, amount: Int): State =
    if (amount == 0) this.copy(visitedTailCoordinates = visitedTailCoordinates.add(knots.last()))
    else State(
        knots.tail().runningFold(knots.first().movedToward(direction)) { previousKnot, knot -> knot.movedToward(previousKnot) },
        visitedTailCoordinates.add(knots.last())
    ).move(direction, amount - 1)


private data class State(val knots: List<Coordinate>, val visitedTailCoordinates: PersistentList<Coordinate>) {
    constructor(numberOfKnots: Int) : this(listOfOrigins(numberOfKnots), persistentListOf())
}

private data class Coordinate(val row: Int, val col: Int) {
    fun movedToward(direction: Char) = when (direction) {
        'U' -> Coordinate(row + 1, col)
        'D' -> Coordinate(row - 1, col)
        'L' -> Coordinate(row, col - 1)
        'R' -> Coordinate(row, col + 1)
        else -> throw Exception("Unknown direction $this")
    }

    fun movedToward(target: Coordinate) =
        if ((row - target.row).absoluteValue <= 1 && (col - target.col).absoluteValue <= 1) this
        else Coordinate(
            row + target.row.compareTo(row).sign,
            col + target.col.compareTo(col).sign,
        )

    companion object {
        private val ORIGIN = Coordinate(0, 0)
        fun listOfOrigins(times: Int) = List(times) { ORIGIN }
    }
}

