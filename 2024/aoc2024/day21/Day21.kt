package aoc2024.day21

import common.*

private val example = loadFilesToLines("aoc2024/day21", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day21", "input.txt").single()

internal fun main() {
    Day21.assertCorrect()
    benchmark(10) { part1(puzzle) } // 20.9ms
    benchmark(10) { part2(puzzle) }
}

internal object Day21 : Challenge {
    override fun assertCorrect() {
        check(126384, "P1 Example") { part1(example) }
        check(248108, "P1 Puzzle") { part1(puzzle) }

        check(0, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val paths = cartesianProductOf(dirPad.keys, dirPad.keys).associateWith { (prev, next) -> dirPad.waysBetween(prev, next) } +
            cartesianProductOf(numPad.keys, numPad.keys).associateWith { (prev, next) -> numPad.waysBetween(prev, next) }
    return input.sumOf { code ->
        code.toIntFromIndex(0) * "A$code".zipWithNext().sumOf { (prev, next) -> fewestKeyPushes2LevelAway(paths, "$prev$next") }
    }
}

private fun part2(input: List<String>): Int {
    error("Need to think about this")
}


private fun fewestKeyPushes2LevelAway(paths: Map<Pair<Char, Char>, List<List<Char>>>, code: String): Int =
    code.zipWithNext()
        .map { paths[it]!! }
        .reduce { acc: List<List<Char>>, options: List<List<Char>> ->
            options.flatMap { option -> acc.map { it + option } }
        }
        .asSequence()
        .map { path -> (listOf('A') + path).zipWithNext() }
        .flatMap { previousLevel ->
            previousLevel.map { paths[it]!! }.reduce { acc, options ->
                options.flatMap { option -> acc.map { it + option } }
            }
        }
        .minOf { path ->
            (listOf('A') + path).asSequence()
                .zipWithNext()
                .sumOf { paths[it]!!.minOf { path -> path.size } }
        }


private fun Map<Char, List<Pair<Char, Char>>>.waysBetween(start: Char, target: Char): List<List<Char>> {
    fun waysBetweenInner(start: Char, seenSoFar: List<Char>): List<List<Char>> {
        if (start == target) return listOf(listOf('A'))
        val seenForNext = seenSoFar.plus(start)
        return this[start]!!.mapNotNull { (move, symbolReached) ->
            if (symbolReached !in seenSoFar) waysBetweenInner(symbolReached, seenForNext).map { it + move }
            else null
        }.flatten()
    }
    return waysBetweenInner(start, listOf()).map { it.reversed() }
}

private val dirPad = mapOf(
    'A' to listOf('<' to '^', 'v' to '>'),
    '^' to listOf('v' to 'v', '>' to 'A'),
    '<' to listOf('>' to 'v'),
    '>' to listOf('<' to 'v', '^' to 'A'),
    'v' to listOf('<' to '<', '>' to '>', '^' to '^'),
)

private val numPad = mapOf(
    'A' to listOf('<' to '0', '^' to '3'),
    '0' to listOf('^' to '2', '>' to 'A'),
    '1' to listOf('^' to '4', '>' to '2'),
    '2' to listOf('^' to '5', '<' to '1', 'v' to '0', '>' to '3'),
    '3' to listOf('^' to '6', '<' to '2', 'v' to 'A'),
    '4' to listOf('^' to '7', 'v' to '1', '>' to '5'),
    '5' to listOf('^' to '8', '<' to '4', 'v' to '2', '>' to '6'),
    '6' to listOf('^' to '9', '<' to '5', 'v' to '3'),
    '7' to listOf('v' to '4', '>' to '8'),
    '8' to listOf('<' to '7', 'v' to '5', '>' to '9'),
    '9' to listOf('<' to '8', 'v' to '6'),
)


