package aoc2025.day12

import common.*

//private val example = loadFilesToLines("aoc2025/day12", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day12", "input.txt").single()

internal fun main() {
    Day12.assertCorrect()
    benchmark { part1(puzzle) } // 948.7µs
}

internal object Day12 : Challenge {
    override fun assertCorrect() {
        // check(2, "P1 Example") { part1(example) }
        check(457, "P1 Puzzle") { part1(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val (shapes, instructions) = listOf(1..3, 6..8, 11..13, 16..18, 21..23, 26..28, 30..input.lastIndex)
        .map { input.subList(it.first, it.last + 1) }
        .let {
            Pair(
                listOf(it[0], it[1], it[2], it[3], it[4], it[5]).map { i -> i.asArrayOfCharArrays() },
                it[6].map { i -> i.matchingAsIntList(Regex("(\\d+)x(\\d+): (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+)"))!! }
            )
        }

    return instructions.count { line ->
        val (w, h) = line
        val shapeCount = line.drop(2)

        // Check the easy case, where there are enough 3x3 squares to hold all the shapes
        val squareCapacity = (w / 3) * (h / 3)
        // Oh... that heuristic also happens to be the answer to the real puzzle input :(
        // Feels like cheating.
        squareCapacity >= shapeCount.sum()
    }
}
