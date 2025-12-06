package aoc2025.day4

import common.*

private val example = loadFilesToGrids("aoc2025/day4", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2025/day4", "input.txt").single()

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzle) } // 175.6µs
    benchmark { part2(puzzle) } // 148.7µs
}

internal object Day4 : Challenge {
    override fun assertCorrect() {
        check(13, "P1 Example") { part1(example) }
        check(1460, "P1 Puzzle") { part1(puzzle) }

        check(43, "P2 Example") { part2(example) }
        check(9243, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int = grid.allLocationOf('@').count { rollCanBeRemoved(it, grid) }

private fun part2(grid: Grid): Int {
    var rollCount = 0
    while (true) {
        val removableRolls = grid.allLocationOf('@').filter { rollCanBeRemoved(it, grid) }
        if (removableRolls.isEmpty()) return rollCount
        // it's faster to use the actual grid as the cache of rolls and to re-scan them each iteration, than it is to store them in a set, due to the hash overhead
        removableRolls.forEach { grid.set(it, '.') }
        rollCount += removableRolls.size
    }
}

private fun rollCanBeRemoved(position: Location1616, grid: Grid): Boolean = sequenceOf(
    position.minusRow().minusCol(),
    position.minusRow(),
    position.minusRow().plusCol(),
    position.minusCol(),
    position.plusCol(),
    position.plusRow().minusCol(),
    position.plusRow(),
    position.plusRow().plusCol()
).count { it.isWithin(grid) && grid.at(it) == '@' } < 4
