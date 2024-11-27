package aoc2023.day13

import common.*

private val examples = loadFiles("aoc2023/day13", "example.txt").map { it.split("\n\n").map { string -> string.lines() } }
private val puzzles = loadFiles("aoc2023/day13", "input.txt").map { it.split("\n\n").map { string -> string.lines() } }

internal fun main() {
    Day13.assertCorrect()
    benchmark { part1(puzzles[0]) } // 666µs
    benchmark(100) { part2(puzzles[0]) } // 24.0ms
}

internal object Day13 : Challenge {
    override fun assertCorrect() {
        check(405, "P1 Example") { part1(examples[0]) }
        check(33122, "P1 Puzzle") { part1(puzzles[0]) }

        check(400, "P2 Example") { part2(examples[0]) }
        check(32312, "P2 Puzzle") { part2(puzzles[0]) }
    }
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
