package ec2024.day5

import common.*
import kotlin.test.assertEquals

private const val rootFolder = "ec2024/day5"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example2Input = "$rootFolder/example2.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day5.assertPart1Correct()
    Day5.assertPart2Correct()
    Day5.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 8.4µs
    benchmark(5) { part2(puzzle2Input) } // 330ms
    benchmark { part3(puzzle3Input) } // 47µs
}

internal object Day5 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(2323L, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(2335L, it) }
    }

    override fun assertPart2Correct() {
        part2(example2Input).also { println("[Example] Part 2: $it") }.also { assertEquals(50877075L, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(20214341458880L, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(6584L, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(3546100410031005L, it) }
    }
}


private fun part1(input: List<String>): Long {
    val game = input.map { it.splitToInts(" ") }.transpose()
    val (_, finalGame) = playGame(game) { round, _ -> round < 10 }
    return gameNumber(finalGame)
}

private fun part2(input: List<String>): Long {
    val game = input.map { it.splitToInts(" ") }.transpose()
    val frequencies = mutableMapOf<Long, Int>()
    val (finalRound, finalGame) = playGame(game) { round, state ->
        val number = gameNumber(state)
        val count = frequencies.getOrDefault(number, 0) + 1
        frequencies[number] = count
        return@playGame count != 2024 // a more motivated person could hunt for a cycle and skip the bulk of this loop
    }
    return finalRound.toLong() * gameNumber(finalGame).toLong()
}

private fun part3(input: List<String>): Long {
    val game = input.map { it.splitToInts(" ") }.transpose()
    val frequencies = mutableMapOf<Long, Int>()
    playGame(game) { round, state ->
        val number = gameNumber(state)
        val count = frequencies.getOrDefault(number, 0) + 1
        frequencies[number] = count
        return@playGame count != 5 // trial and error
    }
    return frequencies.keys.max()
}

private fun playGame(initial: List<List<Int>>, onRoundComplete: (round: Int, state: List<List<Int>>) -> Boolean): Pair<Int, List<List<Int>>> {
    var game = initial.map { it.toMutableList() }
    var round = 0
    var keepGoing = true
    do {
        game = playRound(game, ++round)
        keepGoing = onRoundComplete(round, game)
    } while (keepGoing)
    return round to game
}

private fun playRound(game: List<MutableList<Int>>, round: Int): List<MutableList<Int>> {
    val playerNumber = game[(round - 1) % 4][0]
    game[(round - 1) % 4].removeAt(0)
    game[round % 4].add(insertionIndex(playerNumber, game[round % 4].size), playerNumber)
    return game
}

private fun insertionIndex(steps: Int, lengthOfNextColumn: Int) =
    ((steps - 1) % lengthOfNextColumn).let {
        if (((steps - 1) / lengthOfNextColumn) % 2 == 0) it else lengthOfNextColumn - it
    }

private fun gameNumber(game: List<List<Int>>): Long = when (game[0][0]) {
    in 1000..9999 -> game[0][0] * 10000_0000_0000L + game[1][0] * 10000_0000L + game[2][0] * 10000L + game[3][0]
    in 10..99 -> game[0][0] * 100_00_00L + game[1][0] * 100_00L + game[2][0] * 100L + game[3][0]
    in 1..9 -> game[0][0] * 1000L + game[1][0] * 100L + game[2][0] * 10L + game[3][0]
    else -> throw Error()
}
