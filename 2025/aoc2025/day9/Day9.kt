package aoc2025.day9

import common.*
import java.util.*
import kotlin.math.*

private val example = loadFilesToLines("aoc2025/day9", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day9", "input.txt").single()
private val tim = loadFilesToLines("aoc2025/day9", "tim.txt").single()

internal fun main() {
    Day9.assertCorrect()
//    benchmark { part1(puzzle) } // 852.6µs
//    benchmark(100) { part2(puzzle) } // 56.3ms
}

internal object Day9 : Challenge {
    override fun assertCorrect() {
        check(50, "P1 Example") { part1(example) }
        check(4764078684, "P1 Puzzle") { part1(puzzle) }

        check(24, "P2 Example") { part2(example) }
        check(1652344888, "P2 Puzzle") { part2(puzzle) }

        check(1470616992, "Tim's P2 Puzzle") { part2(tim) }
    }
}


private fun part1(input: List<String>): Long = input
    .map { it.splitToLongs(",").let { (col, row) -> row by col } }
    .let { locations -> locations.mapPairwise { loc1, loc2 -> areaBoundedBy(loc1, loc2) } }
    .max()

private fun part2(input: List<String>): Long {
    val coordinates = input.map { it.splitToLongs(",").let { (col, row) -> row by col } }
    val compressedCoordinates = compressUniverse(coordinates)

    // Observations:
    //  - The edge seems to walk clockwise, which defines the inside vs the outside.
    //  - Every consecutive pair forms its own valid rectangle that can never go out of bounds as it's a straight line on the edge, so
    //    they don't need checking in the main pairwise comparison (which is why the inner loop starts at offset 2 rather than offset 1).
    //  - These 1-height rectangles are almost certainly not the largest viable area, so they can be excluded (see commented-out line below)
    val outOfBoundsTiles = boundingOutsideShapeClockwise(compressedCoordinates)
    val pairsOfIndicesToTest =
        // compressedCoordinates.indices.zipWithNext() +
        compressedCoordinates.mapPairwiseIndexed(offset = 2) { idx1, idx2, loc1, loc2 ->
            val rows = loc1 rowsTo loc2
            val relevantOutOfBoundTiles = outOfBoundsTiles.subSet(rows.first by16 0, rows.last + 1 by16 0)
            val cols = loc1 colsTo loc2
            if (relevantOutOfBoundTiles.none { it.col() in cols }) idx1 to idx2 else null
        }.filterNotNull()

    return pairsOfIndicesToTest
        .map { (c1, c2) -> coordinates[c1] to coordinates[c2] }
        .maxOf { (loc1, loc2) -> areaBoundedBy(loc1, loc2) }
}


private fun areaBoundedBy(loc1: Location, loc2: Location): Long =
    ((loc1.row() - loc2.row()).absoluteValue + 1L) * ((loc1.col() - loc2.col()).absoluteValue + 1L)

private fun compressUniverse(coordinates: List<Location>): List<Location1616> {
    // The idea: to determine if a rectangle overlaps something out-of-bounds, you just need to test the edges
    // rather than the internal space.  So, if any part of the grid is not being used for an edge, remove it.
    // The return value is a list of coordinates where all rows and columns from 1 upwards are used by at least one vertex.
    // (Analysing the input showed that none of the concave elements butt up against each other on the way out,
    // i.e. there are no collapsed caves, so there's no need to leave precautionary gaps in the grid to allow for this.)
    val columnMapping = compactionMapping(coordinates.mapTo(TreeSet()) { it.col() })
    val rowMapping = compactionMapping(coordinates.mapTo(TreeSet()) { it.row() })
    return coordinates.map { rowMapping[it.row()]!! by16 columnMapping[it.col()]!! }
}

private fun compactionMapping(uniqueElementsInOrder: SortedSet<Long>): Map<Long, Long> =
    buildMap {
        uniqueElementsInOrder.forEachIndexed { index, original -> this[original] = index + 1L }
    }

private enum class Direction { UP, DOWN, LEFT, RIGHT }

private fun boundingOutsideShapeClockwise(compressedCoordinates: List<Location1616>): NavigableSet<Location1616> {
    val outOfBoundsTiles = TreeSet<Location1616>()
    val edgeTiles = TreeSet<Location1616>()
    compressedCoordinates.plusElement(compressedCoordinates[0])
        .zipWithNext()
        .forEach { (c1, c2) ->
            val direction = when {
                c1.row() == c2.row() && c1.col() < c2.col() -> Direction.RIGHT
                c1.row() == c2.row() -> Direction.LEFT
                c1.row() < c2.row() -> Direction.DOWN
                else -> Direction.UP
            }
            val edgeWalkerTransform = when (direction) {
                Direction.RIGHT -> Location1616::plusSingleCol
                Direction.LEFT -> Location1616::minusSingleCol
                Direction.DOWN -> Location1616::plusSingleRow
                Direction.UP -> Location1616::minusSingleRow
            }
            val outsideLineTransform = when (direction) {
                Direction.RIGHT -> Location1616::minusSingleRow
                Direction.LEFT -> Location1616::plusSingleRow
                Direction.DOWN -> Location1616::plusSingleCol
                Direction.UP -> Location1616::minusSingleCol
            }

            var pointOnEdge = c1
            while (pointOnEdge != c2) {
                edgeTiles.add(pointOnEdge)
                outOfBoundsTiles.add(outsideLineTransform(pointOnEdge))
                pointOnEdge = edgeWalkerTransform(pointOnEdge)
            }
            outOfBoundsTiles.add(outsideLineTransform(c2))
        }
    outOfBoundsTiles.removeAll(edgeTiles)
//    render(edgeTiles = edgeTiles, outOfBoundsTiles = outOfBoundsTiles, compressedCoordinates = compressedCoordinates)
    return outOfBoundsTiles
}

@Suppress("unused")
private fun render(
    edgeTiles: Collection<Location1616>,
    outOfBoundsTiles: Collection<Location1616>,
    compressedCoordinates: List<Location1616>
) {
    val height = compressedCoordinates.maxOf { it.row() }
    val width = compressedCoordinates.maxOf { it.col() }
    Array(height + 2) { CharArray(width + 2) { ' ' } }
        .apply { edgeTiles.forEach { lng -> this.set(lng, 'X') } }
//        .apply { outOfBoundsTiles.forEach { lng -> this.set(lng, '*') } }
        .apply { compressedCoordinates.forEachIndexed { index, lng -> this.set(lng, (index % 10).digitToChar()) } }
        .also { println(it.render()) }
}

