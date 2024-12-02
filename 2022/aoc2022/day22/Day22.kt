package aoc2022.day22

import common.*
import kotlin.test.*

private val exampleInput = loadFilesToGrids("aoc2022/day22", "example.txt").single()
private val puzzleInput = loadFilesToGrids("aoc2022/day22", "input.txt").single()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 6032
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 5031

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 27436, took 870µs

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput, 4, exampleBoundaryTransitions))
    part2(puzzleInput, 50, puzzleBoundaryTransitions).also { println("Part 2: $it") }  // 15426, took 830µs
}

// Example cube plan:
//           0,2
// 1,0  1,1  1,2
//           2,2  2,3
private val exampleBoundaryTransitions = listOf(
    CubeEdge(Point2DAndDirection(0, 2, Facing.LEFT), Point2DAndDirection(1, 1, Facing.DOWN), indicesReversed = false),
    CubeEdge(Point2DAndDirection(0, 2, Facing.UP), Point2DAndDirection(1, 0, Facing.DOWN), indicesReversed = true),
    CubeEdge(Point2DAndDirection(0, 2, Facing.RIGHT), Point2DAndDirection(2, 3, Facing.LEFT), indicesReversed = true),
    CubeEdge(Point2DAndDirection(1, 0, Facing.LEFT), Point2DAndDirection(2, 3, Facing.UP), indicesReversed = true),
    CubeEdge(Point2DAndDirection(1, 0, Facing.DOWN), Point2DAndDirection(2, 2, Facing.UP), indicesReversed = true),
    CubeEdge(Point2DAndDirection(1, 1, Facing.DOWN), Point2DAndDirection(2, 2, Facing.RIGHT), indicesReversed = true),
    CubeEdge(Point2DAndDirection(1, 2, Facing.RIGHT), Point2DAndDirection(2, 3, Facing.DOWN), indicesReversed = true),
)

// Puzzle cube plan:
//      0,1  0,2
//      1,1
// 2,0  2,1
// 3,0
private val puzzleBoundaryTransitions = listOf(
    CubeEdge(Point2DAndDirection(0, 1, Facing.LEFT), Point2DAndDirection(2, 0, Facing.RIGHT), indicesReversed = true),
    CubeEdge(Point2DAndDirection(0, 1, Facing.UP), Point2DAndDirection(3, 0, Facing.RIGHT), indicesReversed = false),
    CubeEdge(Point2DAndDirection(0, 2, Facing.UP), Point2DAndDirection(3, 0, Facing.UP), indicesReversed = false),
    CubeEdge(Point2DAndDirection(0, 2, Facing.RIGHT), Point2DAndDirection(2, 1, Facing.LEFT), indicesReversed = true),
    CubeEdge(Point2DAndDirection(0, 2, Facing.DOWN), Point2DAndDirection(1, 1, Facing.LEFT), indicesReversed = false),
    CubeEdge(Point2DAndDirection(1, 1, Facing.LEFT), Point2DAndDirection(2, 0, Facing.DOWN), indicesReversed = false),
    CubeEdge(Point2DAndDirection(2, 1, Facing.DOWN), Point2DAndDirection(3, 0, Facing.LEFT), indicesReversed = false),
)

private data class Point2DAndDirection(val row: Int, val col: Int, val facing: Facing)
private data class CubeEdge(val before: Point2DAndDirection, val after: Point2DAndDirection, val indicesReversed: Boolean)


private fun part1(input: Grid): Int {
    val grid = input.copyOfRange(0, input.size - 2)
    val instructions = parseInstructions(input.last())

    tailrec fun positionAfterFollowing(row: Int, col: Int, facing: Facing, remainingInstructions: List<String>): Int {
        val nextInstruction = remainingInstructions.firstOrNull() ?: return (row + 1) * 1000 + (col + 1) * 4 + facing.number
        val steps = when (nextInstruction[0]) {
            'L' -> return positionAfterFollowing(row, col, facing.afterLeftTurn(), remainingInstructions.tail())
            'R' -> return positionAfterFollowing(row, col, facing.afterRightTurn(), remainingInstructions.tail())
            else -> nextInstruction.toInt()
        }
        var newRow = row
        var newCol = col
        repeat(steps) {
            var nextColToTest = newCol
            var nextRowToTest = newRow
            when (facing) {
                Facing.RIGHT -> nextColToTest = if (grid.outOfBounds(row, newCol + 1)) grid.findLeftEdge(row, newCol) else newCol + 1
                Facing.LEFT -> nextColToTest = if (grid.outOfBounds(row, newCol - 1)) grid.findRightEdge(row, newCol) else newCol - 1
                Facing.DOWN -> nextRowToTest = if (grid.outOfBounds(newRow + 1, col)) grid.findTopEdge(newRow, col) else newRow + 1
                Facing.UP -> nextRowToTest = if (grid.outOfBounds(newRow - 1, col)) grid.findBottomEdge(newRow, col) else newRow - 1
            }
            if (grid[nextRowToTest][nextColToTest] == '#')
                return positionAfterFollowing(newRow, newCol, facing, remainingInstructions.tail())
            newRow = nextRowToTest
            newCol = nextColToTest
        }
        return positionAfterFollowing(newRow, newCol, facing, remainingInstructions.tail())
    }
    return positionAfterFollowing(0, grid[0].indexOfFirst { it != ' ' }, Facing.RIGHT, instructions)
}

private fun part2(input: Grid, faceSize: Int, boundaryTransitions: List<CubeEdge>): Int {
    val grid = input.copyOfRange(0, input.size - 2)
    val instructions = parseInstructions(input.last())


    val transitionSites: Map<Point2DAndDirection, Point2DAndDirection> = boundaryTransitions.flatMap {
        listOf(
            it,
            it.copy(
                before = it.after.copy(facing = it.after.facing.opposite()),
                after = it.before.copy(facing = it.before.facing.opposite())
            )
        )
    }.flatMap { (before, after, indicesReversed) ->
        val beforeIndexes = cartesianProductOf(
            when (before.facing) {
                Facing.LEFT, Facing.RIGHT -> before.row * faceSize until (before.row + 1) * faceSize
                Facing.UP -> before.row * faceSize..before.row * faceSize
                Facing.DOWN -> (before.row + 1) * faceSize - 1 until (before.row + 1) * faceSize
            },
            when (before.facing) {
                Facing.LEFT -> before.col * faceSize..before.col * faceSize
                Facing.RIGHT -> (before.col + 1) * faceSize - 1 until (before.col + 1) * faceSize
                Facing.UP, Facing.DOWN -> before.col * faceSize until (before.col + 1) * faceSize
            }
        ).map { (r, c) -> Point2DAndDirection(r, c, before.facing) }
        val afterIndexes = cartesianProductOf(
            when (after.facing) {
                Facing.LEFT, Facing.RIGHT -> after.row * faceSize until (after.row + 1) * faceSize
                Facing.UP -> (after.row + 1) * faceSize - 1 until (after.row + 1) * faceSize
                Facing.DOWN -> after.row * faceSize..after.row * faceSize
            },
            when (after.facing) {
                Facing.LEFT -> (after.col + 1) * faceSize - 1 until (after.col + 1) * faceSize
                Facing.RIGHT -> after.col * faceSize..after.col * faceSize
                Facing.UP, Facing.DOWN -> after.col * faceSize until (after.col + 1) * faceSize
            }
        ).map { (r, c) -> Point2DAndDirection(r, c, after.facing) }

        beforeIndexes.zip(if (indicesReversed) afterIndexes.asReversed() else afterIndexes)
    }.toMap()

    tailrec fun positionAfterFollowing(row: Int, col: Int, facing: Facing, remainingInstructions: List<String>): Int {
        val nextInstruction = remainingInstructions.firstOrNull() ?: return (row + 1) * 1000 + (col + 1) * 4 + facing.number
        val steps = when (nextInstruction[0]) {
            'L' -> return positionAfterFollowing(row, col, facing.afterLeftTurn(), remainingInstructions.tail())
            'R' -> return positionAfterFollowing(row, col, facing.afterRightTurn(), remainingInstructions.tail())
            else -> nextInstruction.toInt()
        }
        var newRow = row
        var newCol = col
        var newFacing = facing
        repeat(steps) {
            var nextColToTest = newCol
            var nextRowToTest = newRow
            var facingToTest = newFacing
            val transitionTarget = transitionSites[Point2DAndDirection(newRow, newCol, newFacing)]
            if (transitionTarget == null) {
                when (newFacing) {
                    Facing.RIGHT -> nextColToTest = newCol + 1
                    Facing.LEFT -> nextColToTest = newCol - 1
                    Facing.DOWN -> nextRowToTest = newRow + 1
                    Facing.UP -> nextRowToTest = newRow - 1
                }
            } else {
                nextRowToTest = transitionTarget.row
                nextColToTest = transitionTarget.col
                facingToTest = transitionTarget.facing
            }
            if (grid[nextRowToTest][nextColToTest] == '#') {
                return positionAfterFollowing(newRow, newCol, newFacing, remainingInstructions.tail())
            }
            newRow = nextRowToTest
            newCol = nextColToTest
            newFacing = facingToTest
        }
        return positionAfterFollowing(newRow, newCol, newFacing, remainingInstructions.tail())
    }
    return positionAfterFollowing(0, grid[0].indexOfFirst { it != ' ' }, Facing.RIGHT, instructions)
}


private fun parseInstructions(chars: CharArray): List<String> {
    var i = 0
    val outputs = ArrayList<String>(chars.size)
    do {
        parseInstruction(i, chars)?.also { outputs.add(it); i += it.length }
    } while (i <= chars.lastIndex)
    return outputs
}

private fun parseInstruction(instructionIndex: Int, instructions: CharArray): String? {
    if (instructionIndex > instructions.lastIndex) return null
    if (instructions[instructionIndex].isLetter()) return instructions[instructionIndex].toString()
    var lastNumber = instructionIndex + 1
    while (lastNumber <= instructions.lastIndex && instructions[lastNumber].isDigit()) lastNumber++
    return instructions.concatToString(instructionIndex, lastNumber)
}


private fun Grid.outOfBounds(row: Int, col: Int) =
    row < 0 || col < 0 || row > lastIndex || col > this[row].lastIndex || this[row][col] == ' '

private fun Grid.findRowEdge(row: Int, col: Int, step: Int): Int {
    var newCol = col
    while (!outOfBounds(row, newCol)) newCol += step
    return newCol - step
}

private fun Grid.findColEdge(row: Int, col: Int, step: Int): Int {
    var newRow = row
    while (!outOfBounds(newRow, col)) newRow += step
    return newRow - step
}

private fun Grid.findLeftEdge(row: Int, col: Int) = findRowEdge(row, col, -1)
private fun Grid.findRightEdge(row: Int, col: Int) = findRowEdge(row, col, 1)
private fun Grid.findTopEdge(row: Int, col: Int) = findColEdge(row, col, -1)
private fun Grid.findBottomEdge(row: Int, col: Int) = findColEdge(row, col, 1)

private enum class Facing(val number: Int, private val afterLeftTurn: Int, private val afterRightTurn: Int, private val opposite: Int) {
    RIGHT(0, 3, 1, 2),
    DOWN(1, 0, 2, 3),
    LEFT(2, 1, 3, 0),
    UP(3, 2, 0, 1);

    fun afterLeftTurn() = fromInt(this.afterLeftTurn)
    fun afterRightTurn() = fromInt(this.afterRightTurn)
    fun opposite() = fromInt(this.opposite)
    private fun fromInt(it: Int): Facing = when (it) {
        0 -> RIGHT
        1 -> DOWN
        2 -> LEFT
        3 -> UP
        else -> throw Exception("Unknown facing value: $it")
    }
}
