package ec2024.day5

import common.*

private val examples = loadFilesToLines("ec2024/day5", "example.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day5", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzles[0]) } // 8.4µs
    benchmark(5) { part2(puzzles[1]) } // 330ms
    benchmark { part3(puzzles[2]) } // 47µs
}

internal object Day5 : Challenge {
    override fun assertCorrect() {
        check(2323L, "P1 Example") { part1(examples[0]) }
        check(2335L, "P1 Puzzle") { part1(puzzles[0]) }

        check(50877075L, "P2 Example") { part2(examples[1]) }
        check(20214341458880L, "P2 Puzzle") { part2(puzzles[1]) }

        check(6584L, "P3 Example") { part3(examples[2]) }
        check(3546100410031005L, "P3 Puzzle") { part3(puzzles[2]) }
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
    val (finalRound, finalGame) = playGame(game) { _, state ->
        val number = gameNumber(state)
        val count = frequencies.getOrDefault(number, 0) + 1
        frequencies[number] = count
        return@playGame count != 2024 // a more motivated person could hunt for a cycle and skip the bulk of this loop
    }
    return finalRound.toLong() * gameNumber(finalGame)
}

private fun part3(input: List<String>): Long {
    val game = input.map { it.splitToInts(" ") }.transpose()
    val frequencies = mutableMapOf<Long, Int>()
    playGame(game) { _, state ->
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
    var keepGoing: Boolean
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
