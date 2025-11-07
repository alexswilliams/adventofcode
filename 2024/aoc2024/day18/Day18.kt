package aoc2024.day18

import common.*

private val example = loadFilesToLines("aoc2024/day18", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day18", "input.txt").single()

internal fun main() {
    Day18.assertCorrect()
    benchmark { part1(puzzle, 71, 1024) } //305µs
    benchmark(10) { part2(puzzle, 71, 1024) } // 221.4ms :(
}

internal object Day18 : Challenge {
    override fun assertCorrect() {
        check(22, "P1 Example") { part1(example, 7, 12) }
        check(364, "P1 Puzzle") { part1(puzzle, 71, 1024) }

        check("6,1", "P2 Example") { part2(example, 7, 12) }
        check("52,28", "P2 Puzzle") { part2(puzzle, 71, 1024) }
    }
}


private fun part1(input: List<String>, gridSize: Int, bytesToFall: Int): Int =
    aStarLengthOrMinusOne(populateGrid(input, gridSize, bytesToFall).second)

private fun part2(input: List<String>, gridSize: Int, startBytesToFall: Int): String {
    val (positions, grid) = populateGrid(input, gridSize, startBytesToFall)
    positions.drop(startBytesToFall).forEach { nextByte ->
        grid.set(nextByte, '#')
        if (aStarLengthOrMinusOne(grid) == -1) return "${nextByte.col()},${nextByte.row()}"
    }
    error("Path was not blocked")
}

private fun populateGrid(input: List<String>, gridSize: Int, startBytesToFall: Int): Pair<List<Location1616>, Array<CharArray>> {
    val positions = input.map { line -> line.mapIntPair(',') { first, second -> second by16 first } }
    val grid = Array(gridSize) { CharArray(gridSize) { '.' } }
    positions.subList(0, startBytesToFall).forEach { grid[it.row()][it.col()] = '#' }
    return Pair(positions, grid)
}

private fun aStarLengthOrMinusOne(grid: Array<CharArray>): Int {
    val target = grid.lastIndex
    val visited = Array(grid.height) { IntArray(grid.width) { Int.MAX_VALUE } }.apply { this.set(0 by16 0, 0) }
    val work = TreeQueue((0 by16 0) to 0) { (target - it.row()) + (target - it.col()) }

    val neighbours = IntArray(4)
    while (true) {
        val u = work.poll() ?: return -1
        val weight = visited.at(u)
        if (u == target by16 target) return weight
        for (n in neighboursOf(u, grid, '#', neighbours)) {
            if (n == -1) continue
            val oldWeight = visited.at(n)
            if (weight + 1 < oldWeight) {
                work.offerOrReposition(n, oldWeight, weight + 1)
                visited.set(n, weight + 1)
            }
        }
    }
}
