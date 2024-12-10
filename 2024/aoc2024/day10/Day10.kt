package aoc2024.day10

import common.*

private val examples = loadFilesToGrids("aoc2024/day10", "example1.txt", "example2.txt", "example3.txt", "example4.txt", "example5.txt", "example6.txt")
private val puzzle = loadFilesToGrids("aoc2024/day10", "input.txt").single()

internal fun main() {
    Day10.assertCorrect()
    benchmark { part1(puzzle) } // 325µs
    benchmark { part2(puzzle) } // 164µs
}

internal object Day10 : Challenge {
    override fun assertCorrect() {
        check(2, "P1 Example 1") { part1(examples[0]) }
        check(4, "P1 Example 2") { part1(examples[1]) }
        check(3, "P1 Example 3") { part1(examples[2]) }
        check(36, "P1 Example 4") { part1(examples[3]) }
        check(489, "P1 Puzzle") { part1(puzzle) }

        check(3, "P2 Example 1") { part2(examples[4]) }
        check(13, "P2 Example 2") { part2(examples[1]) }
        check(227, "P2 Example 3") { part2(examples[5]) }
        check(1086, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int {
    val starts = grid.allLocationOf('0')
    val tops = grid.allLocationOf('9')

    val reachableByStart = Array(grid.height) { Array(grid.width) { BooleanArray(starts.size) } }
    starts.forEachIndexed { index, pos -> reachableByStart[pos.row()][pos.col()][index] = true }
    val work = ArrayDeque(starts)
    val neighbours = IntArray(4)

    while (true) {
        val pos = work.removeFirstOrNull() ?: return tops.sumOf { reachableByStart[it.row()][it.col()].count { b -> b } }
        val startsSeenBefore = reachableByStart[pos.row()][pos.col()]
        for (n in neighboursOf(pos, grid, neighbours)) {
            if (n == -1) continue
            if (grid.at(n) != grid.at(pos) + 1) continue
            startsSeenBefore.forEachIndexed { index, b -> reachableByStart[n.row()][n.col()][index] = reachableByStart[n.row()][n.col()][index] or b }
            work.addLast(n)
        }
    }
}


private fun part2(grid: Grid): Int {
    val starts = grid.allLocationOf('0')
    val tops = grid.allLocationOf('9')

    val reachableByStart = Array(grid.height) { IntArray(grid.width) }
    starts.forEach { pos -> reachableByStart[pos.row()][pos.col()] = 1 }
    val work = ArrayDeque(starts)
    val neighbours = IntArray(4)

    while (true) {
        val pos = work.removeFirstOrNull() ?: return tops.sumOf { reachableByStart[it.row()][it.col()] }
        for (n in neighboursOf(pos, grid, neighbours)) {
            if (n == -1) continue
            if (grid.at(n) != grid.at(pos) + 1) continue
            reachableByStart[n.row()][n.col()] += 1
            work.addLast(n)
        }
    }
}
