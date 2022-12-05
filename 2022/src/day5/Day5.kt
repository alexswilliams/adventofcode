package day5

import common.*
import kotlin.test.*

private val exampleInput = "day5/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day5/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_ANSWER = "CMZ"
private const val PART_2_EXPECTED_ANSWER = "MCD"

fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1: " + part1(puzzleInput)) // SHQWSRBDL

    assertEquals(PART_2_EXPECTED_ANSWER, part2(exampleInput))
    println("Part 2: " + part2(puzzleInput)) // CDTQZHBRS
}

private fun part1(input: List<String>) = solveForBlocksPickedUpAtOnce(input, picksUpSingleBlocks = true)
private fun part2(input: List<String>) = solveForBlocksPickedUpAtOnce(input, picksUpSingleBlocks = false)


private fun solveForBlocksPickedUpAtOnce(input: List<String>, picksUpSingleBlocks: Boolean): String {
    tailrec fun <T> makeMove(state: List<List<T>>, move: Move, blocksToPickUpAtOnce: Int): List<List<T>> =
        if (move.count == 0)
            state;
        else
            makeMove(
                state.mapIndexed { index, column ->
                    when (index) {
                        move.source -> column.drop(blocksToPickUpAtOnce)
                        move.target -> state[move.source].take(blocksToPickUpAtOnce) + column
                        else -> column
                    }
                },
                move.copy(count = move.count - blocksToPickUpAtOnce),
                blocksToPickUpAtOnce
            )

    return parseMoveList(input)
        .fold(parseInitialState(input)) { state, move -> makeMove(state, move, if (picksUpSingleBlocks) 1 else move.count) }
        .joinToString("") { it.first() }
}


private fun parseInitialState(input: List<String>) = input
    .takeWhile { '[' in it }
    .map { line -> line.chunked(4).map { column -> column.trim('[', ']', ' ') } }
    .transpose()
    .map { it.filterNotBlank() }

private fun parseMoveList(input: List<String>) = input
    .mapMatching("""move (\d+) from (\d+) to (\d+)""".toRegex())
    .map { (count, source, target) -> Move(count.toInt(), source.toInt() - 1, target.toInt() - 1) }

private data class Move(val count: Int, val source: Int, val target: Int)
