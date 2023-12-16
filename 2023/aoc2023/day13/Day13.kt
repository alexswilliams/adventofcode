package aoc2023.day13

import common.*
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day13/example.txt".fromClasspathFile().split("\n\n").map { it.lines() }
private val puzzleInput = "aoc2023/day13/input.txt".fromClasspathFile().split("\n\n").map { it.lines() }

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(405, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(33122, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(400, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(32312, it) }
    benchmark { part1(puzzleInput) } // 666µs
    benchmark(100) { part2(puzzleInput) } // 24.0ms
}

private fun part1(grids: List<List<String>>): Int {
    val allColumnMirrors = grids.mapNotNull { grid -> findMirrorColumns(grid).singleOrNull() }
    val allRowMirrors = grids.mapNotNull { grid -> findMirrorColumns(grid.transposeToStrings()).singleOrNull() }
    return allRowMirrors.sum() * 100 + allColumnMirrors.sum()
}

private fun part2(inputs: List<List<String>>): Int {
    val grids = inputs.map { grid ->
        val existingVertical = findMirrorColumns(grid).singleOrNull()
        val existingHorizontal = findMirrorColumns(grid.transposeToStrings()).singleOrNull()
        Triple(correctSmudge(grid, existingVertical, existingHorizontal), existingVertical, existingHorizontal)
    }
    val allColumnMirrors = grids.mapNotNull { (grid, v, _) -> findMirrorColumns(grid, v).singleOrNull() }
    val allRowMirrors = grids.mapNotNull { (grid, _, h) -> findMirrorColumns(grid.transposeToStrings(), h).singleOrNull() }
    return allRowMirrors.sum() * 100 + allColumnMirrors.sum()
}

private fun findMirrorColumns(grid: List<String>, ignore: Int? = null) =
    grid.asSequence()
        .map { line -> (1..line.lastIndex).filter { it != ignore && line[it] == line[it - 1] } }
        .intersect()
        .filter { splitBefore ->
            grid.all { line ->
                if (splitBefore * 2 < line.length) {
                    val before = line.substring(0, splitBefore)
                    val after = line.substring(splitBefore, splitBefore * 2)
                    before == after.reversed()
                } else {
                    val after = line.substring(splitBefore, line.length)
                    val before = line.substring(splitBefore * 2 - line.length, splitBefore)
                    before == after.reversed()
                }
            }
        }

private fun correctSmudge(grid: List<String>, ignoreVertical: Int?, ignoreHorizontal: Int?): List<String> {
    val rows = grid.size
    val cols = grid.first().length
    return allCoordinates(rows, cols)
        .map { l ->
            val existing = grid[l.rowInt()]
            val new = existing.replaceRange(l.colInt(), l.colInt() + 1, if (existing[l.colInt()] == '#') "." else "#")
            List(rows) { if (it == l.rowInt()) new else grid[it] }
        }
        .first { candidate ->
            findMirrorColumns(candidate, ignoreVertical).size == 1 || findMirrorColumns(candidate.transposeToStrings(), ignoreHorizontal).size == 1
        }
}

private fun allCoordinates(rows: Int, cols: Int): Sequence<Location> {
    return sequence {
        for (row in 0..<rows) {
            for (col in 0..<cols) {
                yield(row by col)
            }
        }
    }
}
