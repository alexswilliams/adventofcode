package aoc2022.day5

import common.*

private val example = loadFilesToLines("aoc2022/day5", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day5", "input.txt").single()

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzle) } // 769.1µs
    benchmark { part2(puzzle) } // 168.7µs
}

internal object Day5 : Challenge {
    override fun assertCorrect() {
        check("CMZ", "P1 Example") { part1(example) }
        check("SHQWSRBDL", "P1 Puzzle") { part1(puzzle) }

        check("MCD", "P2 Example") { part2(example) }
        check("CDTQZHBRS", "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: List<String>) = solveForBlocksPickedUpAtOnce(input, picksUpSingleBlocks = true)
private fun part2(input: List<String>) = solveForBlocksPickedUpAtOnce(input, picksUpSingleBlocks = false)


private fun solveForBlocksPickedUpAtOnce(input: List<String>, picksUpSingleBlocks: Boolean): String {
    tailrec fun <T> makeMove(state: List<List<T>>, move: Move, blocksToPickUpAtOnce: Int): List<List<T>> =
        if (move.count == 0)
            state
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
