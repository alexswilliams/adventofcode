package ec2024.day20

import common.*

private val examples = loadFilesToGrids("ec2024/day20", "example1.txt", "example2A.txt", "example2B.txt", "example2C.txt", "example3.txt")
private val puzzles = loadFilesToGrids("ec2024/day20", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day20.assertCorrect()
    benchmark { part1(puzzles[0]) } // 468µs
    benchmark(10) { part2(puzzles[1]) } // 255ms
    benchmark(100) { part3(puzzles[2]) } // 464µs
}

internal object Day20 : Challenge {
    override fun assertCorrect() {
        check(1045, "P1 Example") { part1(examples[0]) }
        check(1031, "P1 Puzzle") { part1(puzzles[0]) }

        check(24, "P2 Example A") { part2(examples[1]) }
        check(78, "P2 Example B") { part2(examples[2]) }
        check(206, "P2 Example C") { part2(examples[3]) }
        check(562, "P2 Puzzle") { part2(puzzles[1]) }

        check(768790, "P3 Example") { part3(examples[4]) }
        check(768791, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(grid: Grid): Int {
    fillDeadEnds(grid)
    val start = grid.locationOf('S')

    data class Work(val position: Location1616, val previous: Location1616, val timeLeft: Int, val height: Int)

    val work = ArrayDeque(listOf(Work(start, -1, 100, 1000)))
    val neighbours = IntArray(4)
    val bestHeightAt = Array(grid.size) { IntArray(grid[0].size) { Int.MIN_VALUE } }
    var bestAfterTimeLimit = 0
    while (true) {
        val u = work.removeFirstOrNull() ?: return bestAfterTimeLimit
        val bestHeightSoFar = bestHeightAt[u.position.row()][u.position.col()]
        if (bestHeightSoFar >= u.height) continue
        bestHeightAt[u.position.row()][u.position.col()] = u.height
        if (u.timeLeft == 0) {
            if (bestAfterTimeLimit < u.height) bestAfterTimeLimit = u.height
            continue
        }
        for (n in neighboursOf(u.position, grid, '#', neighbours)) {
            if (n == -1 || n == u.previous) continue
            val newHeight = u.height + interpretAirCurrent(grid[n.row()][n.col()])
            work.addLast(Work(n, u.position, u.timeLeft - 1, newHeight))
        }
    }
}

private fun part2(grid: Grid): Int {
    fillDeadEnds(grid)
    val start = grid.locationOf('S')
    val heightBounds = (10_000 - (grid.size + grid[0].size) * 4)..(10_000 + (grid.size + grid[0].size) * 4)

    data class Work(val position: Location1616, val previous: Location1616, val timeElapsed: Int, val collected: Int)

    val work = ArrayDeque(listOf(Work(start, -1, 0, 0)))
    val neighbours = IntArray(4)
    val bestHeightAt = Array(4) { Array(grid.size) { IntArray(grid[0].size) { Int.MIN_VALUE } } }
    bestHeightAt[0][start.row()][start.col()] = 10_000

    while (true) {
        val u = work.removeFirstOrNull() ?: throw Error("Explored entire search space without returning to start")
        val bestHeightSoFar = bestHeightAt[u.collected.countOneBits()][u.position.row()][u.position.col()]
        if (u.position == start && bestHeightSoFar >= 10_000 && u.collected == 7) return u.timeElapsed

        for (n in neighboursOf(u.position, grid, '#', neighbours)) {
            if (n == -1 || n == u.previous) continue
            val op = grid[n.row()][n.col()]
            if (op == 'B' && u.collected != 1 || op == 'C' && u.collected != 3) continue
            val newHeight = bestHeightSoFar + interpretAirCurrent(op)
            if (newHeight !in heightBounds) continue
            val newCollected = when (op) {
                'A' -> u.collected or 1
                'B' -> u.collected or 2
                'C' -> u.collected or 4
                else -> u.collected
            }
            // you could search every height, but because the target has a minimum height, the shortest solution will only ever need height adding to make it pass
            if (newHeight > bestHeightAt[newCollected.countOneBits()][n.row()][n.col()]) {
                bestHeightAt[newCollected.countOneBits()][n.row()][n.col()] = newHeight
                work.addLast(Work(n, u.position, u.timeElapsed + 1, newCollected))
            }
        }
    }
}

private fun part3(grid: Grid): Int {
    fillDeadEnds(grid)
    val start = grid.locationOf('S')

    // observation - after a short preamble, flying down one column yields the best solutions
    val lossFromColumns =
        grid[0].indices.map { col ->
            col to grid.indices.sumOf { row -> if (grid[row][col] == '#') -grid.size else interpretAirCurrent(grid[row][col]) }
        }.groupBy({ it.second }) { it.first }
    val lossFromFutureCycles = lossFromColumns.keys.max()
    val leastLossyColumns = lossFromColumns[lossFromFutureCycles]!!

    fun maximiseHeightAtBottomOfTargetColumns(start: Location1616, grid: Grid, leastLossyColumns: List<Int>): List<Int> {
        data class Work(val position: Location1616, val previous: Location1616, val height: Int)

        val work = ArrayDeque(listOf(Work(start, -1, 384_400)))
        val neighbours = IntArray(4)
        val bestHeightAt = Array(grid.size) { IntArray(grid[0].size) { Int.MIN_VALUE } }
        while (true) {
            val u = work.removeFirstOrNull() ?: return leastLossyColumns.map { bestHeightAt[grid.lastIndex][it] }
            val bestHeightSoFar = bestHeightAt[u.position.row()][u.position.col()]
            if (bestHeightSoFar >= u.height) continue
            bestHeightAt[u.position.row()][u.position.col()] = u.height
            for (n in descendingNeighboursOfInfiniteGrid(u.position, grid, '#', neighbours)) {
                if (n == -1 || n == u.previous) continue
                val newHeight = u.height + interpretAirCurrent(grid[n.row()][n.col()])
                work.addLast(Work(n, u.position, newHeight))
            }
        }
    }

    return leastLossyColumns
        .zip(maximiseHeightAtBottomOfTargetColumns(start, grid, leastLossyColumns))
        .maxOf { (col, heightAfterFirstCycle) ->
            val cyclesAdvancedOver = (heightAfterFirstCycle / -lossFromFutureCycles) - 1
            var distanceSouth = (cyclesAdvancedOver + 1) * grid.size - 1
            var height = heightAfterFirstCycle - cyclesAdvancedOver * -lossFromFutureCycles
            while (height > 0) {
                distanceSouth++
                height += interpretAirCurrent(grid[distanceSouth % grid.size][col])
            }
            distanceSouth
        }
}


private fun interpretAirCurrent(op: Char): Int = when (op) {
    '+' -> +1
    '-' -> -2
    else -> -1
}

fun descendingNeighboursOfInfiniteGrid(pos: Location1616, grid: Grid, wall: Char, output: IntArray = IntArray(4)): IntArray {
    val colMinus1 = (pos.col() - 1 + grid[0].size) % grid[0].size
    val colPlus1 = (pos.col() + 1) % grid[0].size
    val rowPlus1 = (pos.row() + 1) % grid.size
    output[0] = if (grid[pos.row()][colMinus1] != wall) pos.row() by16 colMinus1 else -1
    output[2] = if (grid[pos.row()][colPlus1] != wall) pos.row() by16 colPlus1 else -1
    output[3] = if (grid[rowPlus1][pos.col()] != wall) rowPlus1 by16 pos.col() else -1
    return output
}
