package aoc2022.day9

import aoc2022.day9.Coordinate.Companion.listOfOrigins
import common.*
import kotlinx.collections.immutable.*
import kotlin.math.*

private val example = loadFilesToLines("aoc2022/day9", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("aoc2022/day9", "input.txt").single()

internal fun main() {
    Day9.assertCorrect()
    benchmark { part1(puzzle) } // 1.0ms
    benchmark { part2(puzzle) } // 1.4ms
}

internal object Day9 : Challenge {
    override fun assertCorrect() {
        check(13, "P1 Example") { part1(example[0]) }
        check(5883, "P1 Puzzle") { part1(puzzle) }

        check(1, "P2 Example") { part2(example[0]) }
        check(36, "P2 Example") { part2(example[1]) }
        check(2367, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: List<String>) = tailPositionCount(input, 2)
private fun part2(input: List<String>) = tailPositionCount(input, 10)

private fun tailPositionCount(input: List<String>, numberOfKnots: Int) = input.splitOnSpaces()
    .fold(State(numberOfKnots)) { state, (direction, amount) -> state.move(direction.single(), amount.toInt()) }
    .visitedTailCoordinates.distinct().size

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

