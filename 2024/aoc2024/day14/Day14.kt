package aoc2024.day14

import common.*

private val example = loadFilesToLines("aoc2024/day14", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day14", "input.txt").single()

internal fun main() {
    Day14.assertCorrect()
    benchmark { part1(puzzle, 103, 101) } // 308µs
    benchmark(10) { part2(puzzle, 103, 101) } // 196ms :(
}

internal object Day14 : Challenge {
    override fun assertCorrect() {
        check(12, "P1 Example") { part1(example, 7, 11) }
        check(219150360, "P1 Puzzle") { part1(puzzle, 103, 101) }

        check(5, "P2 Example") { part2(example, 7, 11) }
        check(8053, "P2 Puzzle") { part2(puzzle, 103, 101) }
    }
}


private fun part1(input: List<String>, height: Int, width: Int): Int {
    val robots = input.map { it.matchingAsIntList(Regex("p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)"))!! }
    return safetyFactor(robots, height, width, 100)
}

private fun part2(input: List<String>, height: Int, width: Int): Int {
    val robots = input.map { it.matchingAsIntList(Regex("p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)"))!! }
    var i = 1
    var minSeconds = 0 to Int.MAX_VALUE
    while (true) {
        val sf = safetyFactor(robots, height, width, i)
        if (sf < minSeconds.second) minSeconds = i to sf
        if (minSeconds.first + 10_000 < i) return minSeconds.first
        i++
    }
}


private fun safetyFactor(robots: List<List<Int>>, height: Int, width: Int, seconds: Int): Int {
    val quadrants = intArrayOf(0, 0, 0, 0)
    robots.forEach { (x, y, vx, vy) ->
        val pos = (y + vy * seconds % height + height) % height by16 (x + (vx * seconds % width) + width) % width
        when {
            pos.row() < height / 2 && pos.col() < width / 2 -> quadrants[0]++
            pos.row() > height / 2 && pos.col() < width / 2 -> quadrants[1]++
            pos.row() < height / 2 && pos.col() > width / 2 -> quadrants[2]++
            pos.row() > height / 2 && pos.col() > width / 2 -> quadrants[3]++
        }
    }
    return quadrants.product()
}
