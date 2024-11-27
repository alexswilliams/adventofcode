package aoc2023.day2

import common.*

private val examples = loadFilesToLines("aoc2023/day2", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day2", "input.txt")

internal fun main() {
    Day2.assertCorrect()
    benchmark { part1(puzzles[0]) } // 49µs
    benchmark { part2(puzzles[0]) } // 37µs
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check(8, "P1 Example") { part1(examples[0]) }
        check(2204, "P1 Puzzle") { part1(puzzles[0]) }

        check(2286, "P2 Example") { part2(examples[0]) }
        check(71036, "P2 Puzzle") { part2(puzzles[0]) }
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
