package aoc2022.day2

import aoc2022.common.*
import aoc2022.day2.Move.*
import aoc2022.day2.Outcome.*
import kotlin.test.*

private val exampleInput = "aoc2022/day2/example.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_ANSWER = 15
private const val PART_2_EXPECTED_ANSWER = 12

private val puzzleInput = "aoc2022/day2/input.txt".fromClasspathFileToLines()

fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1:" + part1(puzzleInput)) // 11150

    assertEquals(PART_2_EXPECTED_ANSWER, part2(exampleInput))
    println("Part 2:" + part2(puzzleInput)) // 8295
}

private fun part1(input: List<String>) = input
    .map { Move.from(it[0]) to Move.from(it[2]) }
    .sumOf { (challenge, response) -> response.points + myOutcomeFromPlaying(challenge, response).points }

private fun part2(input: List<String>) = input
    .map { Move.from(it[0]) to Outcome.from(it[2]) }
    .sumOf { (challenge, requiredOutcome) -> myMoveForRequiredOutcome(challenge, requiredOutcome).points + requiredOutcome.points }


private enum class Outcome(val points: Int) {
    Win(6), Lose(0), Draw(3);

    companion object {
        fun from(input: Char) = mapOf('X' to Lose, 'Y' to Draw, 'Z' to Win)[input]!!
    }
}

private enum class Move(val points: Int) {
    Rock(1), Paper(2), Scissors(3);

    companion object {
        fun from(input: Char) = when (input) {
            'A', 'X' -> Rock
            'B', 'Y' -> Paper
            'C', 'Z' -> Scissors
            else -> throw Exception("Invalid input $input")
        }
    }
}

private fun myOutcomeFromPlaying(challenge: Move, response: Move): Outcome = when (challenge) {
    Rock -> mapOf(Rock to Draw, Paper to Win, Scissors to Lose)[response]!!
    Paper -> mapOf(Rock to Lose, Paper to Draw, Scissors to Win)[response]!!
    Scissors -> mapOf(Rock to Win, Paper to Lose, Scissors to Draw)[response]!!
}

private fun myMoveForRequiredOutcome(challenge: Move, requiredOutcome: Outcome): Move = when (challenge) {
    Rock -> mapOf(Win to Paper, Lose to Scissors, Draw to Rock)[requiredOutcome]!!
    Paper -> mapOf(Win to Scissors, Lose to Rock, Draw to Paper)[requiredOutcome]!!
    Scissors -> mapOf(Win to Rock, Lose to Paper, Draw to Scissors)[requiredOutcome]!!
}
