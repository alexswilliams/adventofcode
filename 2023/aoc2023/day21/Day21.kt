package aoc2023.day21

import common.*

private val example = loadFilesToGrids("aoc2023/day21", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2023/day21", "input.txt").single()

internal fun main() {
    Day21.assertCorrect()
    benchmark { part1(puzzle) } // 5.8ms
    benchmark { part2(puzzle) }
}

internal object Day21 : Challenge {
    override fun assertCorrect() {
        check(16, "P1 Example") { part1(example, steps = 6) }
        check(3598, "P1 Puzzle") { part1(puzzle) }

        check(16, "P2 Example") { part2(example, steps = 6) }
        check(50, "P2 Example") { part2(example, steps = 10) }
        check(1594, "P2 Example") { part2(example, steps = 50) }
        check(6536, "P2 Example") { part2(example, steps = 100) }
        check(167004, "P2 Example") { part2(example, steps = 500) }
        check(668697, "P2 Example") { part2(example, steps = 1000) }
        check(16733044, "P2 Example") { part2(example, steps = 5000) }
        check(0, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid, steps: Int = 64): Int {
    return (1..steps).fold(setOf(grid.locationOf('S'))) { locations, _ ->
        buildSet {
            for (location in locations) {
                location.plusRow().also { if (it.isWithin(grid) && grid.at(it) != '#') add(it) }
                location.minusRow().also { if (it.isWithin(grid) && grid.at(it) != '#') add(it) }
                location.plusCol().also { if (it.isWithin(grid) && grid.at(it) != '#') add(it) }
                location.minusCol().also { if (it.isWithin(grid) && grid.at(it) != '#') add(it) }
            }
        }
    }.size
}

private fun part2(grid: Grid, steps: Int = 26501365): Long = TODO()
