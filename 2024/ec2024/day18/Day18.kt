package ec2024.day18

import common.*

private val examples = loadFilesToGrids("ec2024/day18", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToGrids("ec2024/day18", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day18.assertCorrect()
    benchmark { part1(puzzles[0]) } // 17µs
    benchmark { part2(puzzles[1]) } // 369µs
    benchmark(1) { part3(puzzles[2]) } // 2.7s :(
}

internal object Day18 : Challenge {
    override fun assertCorrect() {
        check(11, "P1 Example") { part1(examples[0]) }
        check(129, "P1 Puzzle") { part1(puzzles[0]) }

        check(21, "P2 Example") { part2(examples[1]) }
        check(1289, "P2 Puzzle") { part2(puzzles[1]) }

        check(12, "P3 Example") { part3(examples[2]) }
        check(239760, "P3 Puzzle") { part3(puzzles[2]) } // a smaller answer of 239743 is possible if you can dig the well directly under a tree
    }
}


private fun part1(input: Grid): Int =
    timeUntilAllTreesWatered(
        fillDeadEnds(input),
        listOf(input.indexOfFirst { it.first() == '.' } by16 0))

private fun part2(input: Grid): Int =
    timeUntilAllTreesWatered(
        fillDeadEnds(input),
        listOf(input.indexOfFirst { it.first() == '.' } by16 0, input.indexOfFirst { it.last() == '.' } by16 input[0].lastIndex))

private fun part3(input: Grid): Int {
    val grid = fillDeadEnds(input)
    val possibleStarts = grid.mapCartesianNotNull { row, col, char -> if (char == '.') row by16 col else null }
    return possibleStarts.minOf { start -> sumOfIndividualTimesUntilAllTreesWatered(grid, start) }
}

private fun pack(pos: Location1616, distance: Int): Long = pos.toLong() or (distance.toLong() shl 32)
private fun unpackPos(packed: Long): Location1616 = (packed and 0xffff_ffff).toInt()
private fun unpackDistance(packed: Long): Int = ((packed and 0x7fff_ffff_0000_0000) shr 32).toInt()

private fun sumOfIndividualTimesUntilAllTreesWatered(grid: Grid, start: Location1616): Int {
    val work = ArrayDeque(listOf(pack(start, 0)))
    val visited = Array(grid.size) { BooleanArray(grid[0].size) { false } }.apply { this[start.row()][start.col()] = true }
    val numberOfTrees = grid.sumOf { it.count { ch -> ch == 'P' } }
    var totalPathLengths = 0
    var treesSeen = if (grid[start.row()][start.col()] == 'P') 1 else 0

    val neighbours = IntArray(4)
    while (true) {
        val packed = work.removeFirstOrNull() ?: throw Error("Explored all squares but palm trees still remain un-irrigated")
        val u = unpackPos(packed)
        val dist = unpackDistance(packed)

        for (n in neighboursOf(u, grid, neighbours)) {
            if (n == -1) continue
            val row = n.row()
            val col = n.col()
            if (visited[row][col]) continue
            visited[row][col] = true
            if (grid[row][col] == 'P') {
                totalPathLengths += dist + 1
                if (++treesSeen == numberOfTrees) return totalPathLengths
            }
            work.addLast(pack(n, dist + 1))
        }
    }
}

private fun timeUntilAllTreesWatered(grid: Grid, starts: List<Location1616>): Int {
    val work = ArrayDeque<Long>().apply { starts.forEach { add(pack(it, 0)) } }
    val visited = Array(grid.size) { BooleanArray(grid[0].size) { false } }.apply { starts.forEach { start -> this[start.row()][start.col()] = true } }
    val numberOfTrees = grid.sumOf { it.count { ch -> ch == 'P' } }
    var treesSeen = 0
    val neighbours = IntArray(4)
    while (true) {
        val packed = work.removeFirstOrNull() ?: throw Error("Explored all squares but palm trees still remain un-irrigated")
        val u = unpackPos(packed)
        val dist = unpackDistance(packed)

        for (n in neighboursOf(u, grid, neighbours)) {
            if (n == -1) continue
            val row = n.row()
            val col = n.col()
            if (visited[row][col]) continue
            visited[row][col] = true
            if (grid[row][col] == 'P') {
                treesSeen++
                if (treesSeen == numberOfTrees) return dist + 1
            }
            work.addLast(pack(n, dist + 1))
        }
    }
}

private fun neighboursOf(u: Location1616, grid: Grid, result: IntArray = IntArray(4)): IntArray {
    val row = u.row()
    val col = u.col()
    result[0] = if (row == 0 || grid[row - 1][col] == '#') -1 else u.minusRow()
    result[1] = if (row == grid.lastIndex || grid[row + 1][col] == '#') -1 else u.plusRow()
    result[2] = if (col == 0 || grid[row][col - 1] == '#') -1 else u.minusCol()
    result[3] = if (col == grid[0].lastIndex || grid[row][col + 1] == '#') -1 else u.plusCol()
    return result
}

