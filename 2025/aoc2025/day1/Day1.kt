package aoc2025.day1

import common.*

private val example = loadFilesToLines("aoc2025/day1", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day1", "input.txt").single()

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzle) } // 80.9µs
    benchmark { part2(puzzle) } // 59.2µs
}

internal object Day1 : Challenge {
    override fun assertCorrect() {
        check(3, "P1 Example") { part1(example) }
        check(1168, "P1 Puzzle") { part1(puzzle) }

        check(6, "P2 Example") { part2(example) }
        check(7199, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int = input
    .runningFold(50) { pos, move -> (pos + sign(move) * move.toIntFromIndex(1)).mod(100) }
    .count { it == 0 }

private fun part2(input: List<String>): Int {
    tailrec fun countZeroPasses(pos: Int, index: Int = 0, zerosSoFar: Int = 0): Int {
        if (index > input.lastIndex) return zerosSoFar
        val amount = input[index].toIntFromIndex(1)
        val fullRotations = amount / 100
        val remainingClicks = (amount % 100) * sign(input[index])
        return countZeroPasses(
            pos = (pos + remainingClicks).mod(100),
            index = index + 1,
            zerosSoFar = zerosSoFar +
                    fullRotations +
                    if (pos == 0 || pos + remainingClicks in 1..99) 0 else 1
        )
    }
    return countZeroPasses(50)
}

private fun sign(move: String) =
    if (move[0] == 'L') -1 else 1
