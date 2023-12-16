package aoc2023.day14

import common.*
import kotlin.math.max
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day14/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day14/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(136, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(108935, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(64, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(100876, it) }
    benchmark { part1(puzzleInput) } // 275µs
    benchmark(10) { part2(puzzleInput) } // 160ms
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
    val (loopStartsAt, loopLength, rocksAtEndOfCycle) = findLoop(movableRocks, lines.size, allFixedRockGrids)
    return (1..(1_000_000_000 - loopStartsAt) % loopLength)
        .fold(rocksAtEndOfCycle) { grid, _ -> spinCycle(lines.size, grid, allFixedRockGrids) }
        .sumOf { rock -> lines.size - rock.row() }
}

private data class Loop(val loopStarts: Int, val loopLength: Int, val repeatedState: Rocks)

private fun findLoop(movableRocks: Rocks, size: Int, allFixedRockGrids: List<LocationsByColumn>): Loop {
    val seenBefore = mutableMapOf(movableRocks.toSet() to 0)
    var rocks = movableRocks
    var i = 0
    while (true) {
        rocks = spinCycle(size, rocks, allFixedRockGrids)
        val loopStart = seenBefore.put(rocks.toSet(), ++i)
        if (loopStart != null) return Loop(loopStart, i - loopStart, rocks)
    }
}

private fun precalculateRotationsOfFixedRocks(fixed: Rocks, size: Int): List<LocationsByColumn> {
    val fixedWestIsUp = rotate(fixed, size)
    val fixedSouthIsUp = rotate(fixedWestIsUp, size)
    val fixedEastIsUp = rotate(fixedSouthIsUp, size)
    return listOf(
        fixed.groupBy { it.col() },
        fixedWestIsUp.groupBy { it.col() },
        fixedSouthIsUp.groupBy { it.col() },
        fixedEastIsUp.groupBy { it.col() },
    )
}

private fun spinCycle(size: Int, movable: Rocks, fixed: List<LocationsByColumn>): Rocks =
    rotate(tilt(rotate(tilt(rotate(tilt(rotate(tilt(movable, fixed[0]), size), fixed[1]), size), fixed[2]), size), fixed[3]), size)

private fun rotate(afterTiltNorth: Rocks, size: Int) =
    afterTiltNorth.map { it.col() by (size - 1 - it.row()) }

@Suppress("UNCHECKED_CAST")
private fun tilt(movable: Rocks, fixedRocksByColumn: LocationsByColumn): Rocks {
    val movableByCol = movable.groupBy { it.col() } as Map<Long, MutableList<Long>>
    movableByCol.forEach { (col, movableRocksInColumn) ->
        movableRocksInColumn.apply { sort() }.forEachIndexed { index: Int, rock: Location ->
            val nearestMovableRockToNorth = movableRocksInColumn.maxOfBefore(index) { if (it.row() < rock.row()) it.row() else -1 }
            val nearestFixedRockToNorth = fixedRocksByColumn[col]?.maxOf { if (it.row() < rock.row()) it.row() else -1 } ?: -1
            movableRocksInColumn[index] = (max(nearestFixedRockToNorth, nearestMovableRockToNorth) + 1) by rock.col()
        }
    }
    return movableByCol.entries.flatMap { it.value.asIterable() }
}

