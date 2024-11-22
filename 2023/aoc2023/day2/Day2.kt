package aoc2023.day2

import common.*
import kotlin.test.*

private val exampleInput = "aoc2023/day2/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day2/input.txt".fromClasspathFileToLines()

internal fun main() {
    Day2.assertPart1Correct()
    Day2.assertPart2Correct()
    benchmark { part1(puzzleInput) } // 49µs
    benchmark { part2(puzzleInput) } // 37µs
}

internal object Day2 : TwoPartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(8, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(2204, it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(2286, it) }
        part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(71036, it) }
    }
}

private data class Round(val red: Int, val green: Int, val blue: Int)
private data class Game(val id: Int, val rounds: List<Round>)

private fun parseGame(gameLine: String): Game {
    val idxId = gameLine.indexOf(' ') + 1
    return Game(
        id = gameLine.toIntFromIndex(idxId),
        rounds = gameLine.splitMappingRanges(
            delimiter = "; ",
            startAt = 2 + gameLine.indexOf(':', startIndex = idxId + 1),
            transform = ::parseRound
        )
    )
}

private fun parseRound(round: String, startAt: Int, endAt: Int): Round {
    var red = 0
    var green = 0
    var blue = 0
    var idxNumber = startAt
    do {
        when (round[round.indexOf(' ', idxNumber + 1) + 1]) {
            'r' -> red = round.toIntFromIndex(idxNumber)
            'g' -> green = round.toIntFromIndex(idxNumber)
            'b' -> blue = round.toIntFromIndex(idxNumber)
        }
        idxNumber = round.indexOf(',', idxNumber + 5) + 2
    } while (idxNumber != 1 && idxNumber < endAt)
    return Round(red, green, blue)
}

private fun part1(input: List<String>) =
    input.map { line -> parseGame(line) }
        .filter { game -> game.rounds.all { it.red <= 12 && it.green <= 13 && it.blue <= 14 } }
        .sumOf { game -> game.id }

private fun part2(input: List<String>) =
    input.sumOf { game ->
        parseGame(game).rounds
            .reduce { maxSoFar: Round, round: Round ->
                Round(
                    red = round.red.coerceAtLeast(maxSoFar.red),
                    green = round.green.coerceAtLeast(maxSoFar.green),
                    blue = round.blue.coerceAtLeast(maxSoFar.blue),
                )
            }
            .let { it.red * it.green * it.blue }
    }
