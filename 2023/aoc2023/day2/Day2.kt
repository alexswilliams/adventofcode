package aoc2023.day2

import common.benchmark
import common.fromClasspathFileToLines
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day2/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day2/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(8, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(2204, it) }

    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(2286, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(71036, it) }

    benchmark { part2(puzzleInput) } // ~715µs
}

data class Game(val id: Int, val rounds: List<Map<String, Int>>)

private fun parseGame(gameLine: String): Game {
    val (idString, roundsString) = Regex("^Game (\\d+): (.*)$").matchEntire(gameLine)?.destructured
        ?: throw Exception("Line not in expected format: $gameLine")
    val roundsList = roundsString.split(';')
        .map { round ->
            round.trim().split(',')
                .associate { selection -> selection.trim().split(' ').let { it[1].trim() to it[0].trim().toInt() } }
        }
    return Game(idString.toInt(), roundsList)
}


private fun part1(input: List<String>) =
    input.map { game -> parseGame(game) }.filter { game ->
        game.rounds.all { round ->
            ((round["red"] ?: 0) <= 12) && ((round["green"] ?: 0) <= 13) && ((round["blue"] ?: 0) <= 14) &&
                    setOf("red", "green", "blue").containsAll(round.keys)
        }
    }.sumOf { game -> game.id }

private fun part2(input: List<String>) = input.sumOf { game ->
    parseGame(game).rounds
        .reduce() { minSoFar: Map<String, Int>, round: Map<String, Int> ->
            mapOf(
                "red" to (round["red"] ?: 0).coerceAtLeast(minSoFar["red"] ?: 0),
                "green" to (round["green"] ?: 0).coerceAtLeast(minSoFar["green"] ?: 0),
                "blue" to (round["blue"] ?: 0).coerceAtLeast(minSoFar["blue"] ?: 0)
            )
        }.let { (it["red"] ?: 0) * (it["green"] ?: 0) * (it["blue"] ?: 0) }
}
