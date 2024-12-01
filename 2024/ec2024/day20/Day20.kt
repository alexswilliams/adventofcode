package ec2024.day20

import common.*

private val examples = loadFilesToGrids("ec2024/day20", "example1.txt", "example2A.txt", "example2B.txt", "example2C.txt", "example3.txt")
private val puzzles = loadFilesToGrids("ec2024/day20", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day20.assertCorrect()
    benchmark { part1(puzzles[0]) } // 1.4ms
//    benchmark(1) { part2(puzzles[1]) } // 46s :(
//    benchmark(100) { part3(puzzles[2]) }
}

internal object Day20 : Challenge {
    override fun assertCorrect() {
        check(1045, "P1 Example") { part1(examples[0]) }
        check(1031, "P1 Puzzle") { part1(puzzles[0]) }

//        check(24, "P2 Example A") { part2(examples[1]) }
//        check(78, "P2 Example B") { part2(examples[2]) }
//        check(206, "P2 Example C") { part2(examples[3]) }
//        check(562, "P2 Puzzle") { part2(puzzles[1]) }

//        check(768790, "P3 Example") { part3(examples[4]) }
//        check(768791, "P3 Puzzle") { part3(puzzles[2]) } // solved by hand
    }
}


private fun part1(grid: Grid): Int {
    fillDeadEnds(grid)
    val start = grid.location16Of('S')

    data class Work(val position: Location1616, val previous: Location1616, val timeLeft: Int, val height: Int)

    val work = ArrayDeque(listOf(Work(start, -1, 100, 1000)))
    val neighbours = IntArray(4)
    val bestHeightAt = mutableMapOf<Location1616, Int>()
    var bestAfterTimeLimit = 0
    while (true) {
        val u = work.removeFirstOrNull() ?: return bestAfterTimeLimit
        val bestHeightSoFar = bestHeightAt.getOrDefault(u.position, Int.MIN_VALUE)
        if (bestHeightSoFar >= u.height) continue
        bestHeightAt[u.position] = u.height
        if (u.timeLeft == 0) {
            if (bestAfterTimeLimit < u.height) bestAfterTimeLimit = u.height
            continue
        }
        for (n in neighboursOf(u.position, grid, '#', neighbours)) {
            if (n == -1) continue
            if (n == u.previous) continue
            val op = grid[n.row()][n.col()]
            val newHeight = when (op) {
                '+' -> u.height + 1
                '-' -> u.height - 2
                else -> u.height - 1
            }
            work.addLast(Work(n, u.position, u.timeLeft - 1, newHeight))
        }
    }
}

private fun part2(grid: Grid): Int {
    fillDeadEnds(grid)
    val start = grid.location16Of('S')

    data class Work(val position: Location1616, val previous: Location1616, val timeElapsed: Int, val height: Int, val collected: Int)

    val work = ArrayDeque(listOf(Work(start, -1, 0, 10_000, 0)))
    val neighbours = IntArray(4)
    fun pack(position: Location1616, height: Int, collected: Int): Long = (position.toLong() shl 32) or (height.toLong() shl 3) or collected.toLong()
    val visited = mutableSetOf(pack(start, 10_000, 0))
    while (true) {
        val u = work.removeFirstOrNull() ?: throw Error("Explored entire search space without returning to start")
        if (u.position == start && u.height >= 10_000 && u.collected == 7) return u.timeElapsed
        for (n in neighboursOf(u.position, grid, '#', neighbours)) {
            if (n == -1) continue
            if (n == u.previous) continue
            val op = grid[n.row()][n.col()]
            val newHeight = when (op) {
                '+' -> u.height + 1
                '-' -> u.height - 2
                else -> u.height - 1
            }
            val newCollected = when (op) {
                'A' -> u.collected or 1
                'B' -> u.collected or 2
                'C' -> u.collected or 4
                else -> u.collected
            }
            if (newCollected != 0 && newCollected != 1 && newCollected != 3 && newCollected != 7) continue
            if (visited.add(pack(n, newHeight, newCollected)))
                work.addLast(Work(n, u.position, u.timeElapsed + 1, newHeight, newCollected))
        }
    }
}

private fun part3(grid: Grid): Int = 0
