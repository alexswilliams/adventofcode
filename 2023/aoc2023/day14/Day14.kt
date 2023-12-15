package aoc2023.day14

import common.Location
import common.benchmark
import common.by
import common.col
import common.fromClasspathFileToLines
import common.locationsOfEach
import common.row
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
    benchmark(10) { part2(puzzleInput) } // 183ms
}

private fun part1(lines: List<String>): Int {
    val (movable, fixed) = lines.locationsOfEach('O', '#')
    val afterMove = tilt(movable, fixed.groupBy { it.col() })
    return afterMove.sumOf { rock -> lines.size - rock.row() }.toInt()
}

private fun part2(lines: List<String>): Int {
    val (movable, fixed) = lines.locationsOfEach('O', '#')
    val fixedByCol = fixedRotations(fixed, lines.size)

    val (loopStarts, loopStartsAgain) = findLoop(movable, lines.size, fixedByCol)
    val spinsConsumedByLooping = ((1_000_000_000 - loopStarts) / (loopStartsAgain - loopStarts)) * (loopStartsAgain - loopStarts)
    val remainingSpins = 1_000_000_000 - loopStarts - spinsConsumedByLooping

    val rocksBeforeStartOfLoop = (1..loopStarts).fold(movable) { acc, _ -> spinCycle(lines.size, acc, fixedByCol) }
    val rocksAfterEndOfLoop = (1..remainingSpins).fold(rocksBeforeStartOfLoop) { acc, _ -> spinCycle(lines.size, acc, fixedByCol) }
    return rocksAfterEndOfLoop.sumOf { rock -> lines.size - rock.row() }.toInt()
}

private fun findLoop(movable: List<Location>, size: Int, fixedByCol: List<Map<Long, List<Location>>>): Pair<Int, Int> {
    val seenBefore = mutableMapOf(movable.toSet() to 0)
    var rocks = movable
    var i = 0
    do {
        rocks = spinCycle(size, rocks, fixedByCol)
        val loopStart = seenBefore.put(rocks.toSet(), ++i)
        if (loopStart != null) return loopStart to i
    } while (true)
}

private fun fixedRotations(fixed: List<Location>, size: Int): List<Map<Long, List<Location>>> {
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

private fun spinCycle(size: Int, movable: List<Location>, fixedByCol: List<Map<Long, List<Location>>>): List<Location> =
    rotate(tilt(rotate(tilt(rotate(tilt(rotate(tilt(movable, fixedByCol[0]), size), fixedByCol[1]), size), fixedByCol[2]), size), fixedByCol[3]), size)

private fun rotate(afterTiltNorth: List<Location>, size: Int) =
    afterTiltNorth.map { it.col() by (size - 1 - it.row()) }

private fun tilt(movable: List<Location>, fixedByCol: Map<Long, List<Location>>): List<Location> {
    val movableByCol = movable.groupBy { it.col() }.mapValues { (_, b) -> b.toLongArray().also { it.sort() } }
    movableByCol.forEach { (col, movableRocksInColumn) ->
        movableRocksInColumn.forEachIndexed { index: Int, rock: Location ->
            val nearestMovableRockToNorth = movableRocksInColumn.maxOf { if (it.row() < rock.row()) it.row() else -1 }
            val nearestFixedRockToNorth = fixedByCol[col]?.maxOf { if (it.row() < rock.row()) it.row() else -1 } ?: -1
            movableRocksInColumn[index] = (max(nearestFixedRockToNorth, nearestMovableRockToNorth) + 1) by rock.col()
        }
    }
    return movableByCol.entries.flatMap { it.value.asIterable() }
}