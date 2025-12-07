package aoc2025.day7

import common.*

private val example = loadFilesToGrids("aoc2025/day7", "example.txt").single()
private val puzzle = loadFilesToGrids("aoc2025/day7", "input.txt").single()

internal fun main() {
    Day7.assertCorrect()
    benchmark { part1(puzzle) } // 155.1µs
    benchmark { part2(puzzle) } // 24.4µs
}

internal object Day7 : Challenge {
    override fun assertCorrect() {
        check(21, "P1 Example") { part1(example) }
        check(1662, "P1 Puzzle") { part1(puzzle) }

        check(40, "P2 Example") { part2(example) }
        check(40941112789504, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int {
    var beams = setOf(grid[0].indexOf('S'))
    var splits = 0
    for (rowIndex in 2..grid.lastIndex step 2) { // every other row is blank, so beams just fall through - skip these rows
        val (beamsBeingSplit, beamsNotBeingSplit) = beams.partition { grid[rowIndex][it] == '^' }
        splits += beamsBeingSplit.size
        beams = buildSet {
            beamsNotBeingSplit.forEach { add(it) }
            beamsBeingSplit.forEach { index ->
                add(index - 1) // puzzle is set up so that no bounds-checking is needed
                add(index + 1)
            }
        }
    }
    return splits
}

private fun part2(grid: Grid): Long =
    (2..grid.lastIndex step 2).fold(
        LongArray(grid.width).apply { this[grid[0].indexOf('S')] = 1 }
    ) { timelinesAbove, row ->
        LongArray(grid.width).apply {
            for (index in (grid.width / 2 - row / 2)..(grid.width / 2 + row / 2)) {
                if (grid[row][index] == '^') {
                    this[index - 1] += timelinesAbove[index]
                    this[index + 1] += timelinesAbove[index]
                } else
                    this[index] += timelinesAbove[index]
            }
        }
    }.sum()
