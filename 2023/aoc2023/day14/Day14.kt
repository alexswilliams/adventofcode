package aoc2023.day14

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("aoc2023/day14", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day14", "input.txt")

internal fun main() {
    Day14.assertCorrect()
    benchmark { part1(puzzles[0]) } // 275µs
    benchmark(10) { part2(puzzles[0]) } // 126ms
}

internal object Day14 : Challenge {
    override fun assertCorrect() {
        check(136, "P1 Example") { part1(examples[0]) }
        check(108935, "P1 Puzzle") { part1(puzzles[0]) }

        check(64, "P2 Example") { part2(examples[0]) }
        check(100876, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private typealias Rocks = List<Location>
private typealias LocationsByColumn = Map<Long, Rocks>

private fun part1(lines: List<String>): Long {
    val (movableRocks, fixedRocks) = lines.locationsOfEach('O', '#')
    return tilt(movableRocks, fixedRocks.groupBy { it.col() })
        .sumOf { rock -> lines.size - rock.row() }
}

private fun part2(lines: List<String>): Long {
    val (movableRocks, fixedRocks) = lines.locationsOfEach('O', '#')
    val allFixedRockGrids = precalculateRotationsOfFixedRocks(fixedRocks, lines.size)
    val (loopStartIndex, loopLength, movableRocksAtEndOfCycle) = findLoop(movableRocks, lines.size, allFixedRockGrids)
    return (1..(1_000_000_000 - loopStartIndex) % loopLength)
        .fold(movableRocksAtEndOfCycle) { grid, _ -> spinCycle(lines.size, grid, allFixedRockGrids) }
        .sumOf { rock -> lines.size - rock.row() }
}

private data class Loop(val loopStartIndex: Int, val loopLength: Int, val repeatedState: Rocks)

private fun findLoop(movableRocks: Rocks, gridSize: Int, allFixedRockGrids: List<LocationsByColumn>): Loop {
    fun hashRocks(rocks: Rocks): Long = rocks.reduce { acc, it -> acc * 23 + it } // This is a gamble - not guaranteed to be unique over long sequences
    val seenBefore = mutableMapOf(hashRocks(movableRocks) to 0)
    var rocks = movableRocks
    var i = 0
    while (true) {
        rocks = spinCycle(gridSize, rocks, allFixedRockGrids)
        val loopStart = seenBefore.put(hashRocks(rocks), ++i)
        if (loopStart != null) return Loop(loopStart, i - loopStart, rocks)
    }
}

private fun precalculateRotationsOfFixedRocks(fixedRocksNorthIsUp: Rocks, gridSize: Int): List<LocationsByColumn> {
    val fixedRocksWestIsUp = rotate(fixedRocksNorthIsUp, gridSize)
    val fixedRocksSouthIsUp = rotate(fixedRocksWestIsUp, gridSize)
    val fixedRocksEastIsUp = rotate(fixedRocksSouthIsUp, gridSize)
    return listOf(
        fixedRocksNorthIsUp.groupBy { it.col() },
        fixedRocksWestIsUp.groupBy { it.col() },
        fixedRocksSouthIsUp.groupBy { it.col() },
        fixedRocksEastIsUp.groupBy { it.col() },
    )
}

private fun spinCycle(size: Int, movableRocks: Rocks, fixed: List<LocationsByColumn>): Rocks =
    rotate(tilt(rotate(tilt(rotate(tilt(rotate(tilt(movableRocks, fixed[0]), size), fixed[1]), size), fixed[2]), size), fixed[3]), size)

private fun rotate(rocks: Rocks, size: Int) = rocks.map { it.col() by (size - 1 - it.row()) }

@Suppress("UNCHECKED_CAST")
private fun tilt(movable: Rocks, fixedRocksByColumn: LocationsByColumn): Rocks {
    val movableByCol = movable.groupBy { it.col() } as Map<Long, MutableList<Location>>
    movableByCol.forEach { (col, movableRocksInColumn) ->
        movableRocksInColumn.apply { sort() }.forEachIndexed { index: Int, rock: Location ->
            val nearestMovableRockToNorth = movableRocksInColumn.maxOfBefore(index) { if (it.row() < rock.row()) it.row() else -1 }
            val nearestFixedRockToNorth = fixedRocksByColumn[col]?.maxOf { if (it.row() < rock.row()) it.row() else -1 } ?: -1
            movableRocksInColumn[index] = (max(nearestFixedRockToNorth, nearestMovableRockToNorth) + 1) by rock.col()
        }
    }
    return movableByCol.entries.flatMap { it.value.asIterable() }
}

