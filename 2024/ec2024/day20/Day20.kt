package ec2024.day20

import common.*

private val examples = loadFilesToGrids("ec2024/day20", "example1.txt", "example2A.txt", "example2B.txt", "example2C.txt", "example3.txt")
private val puzzles = loadFilesToGrids("ec2024/day20", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day20.assertCorrect()
    benchmark { part1(puzzles[0]) } // 1.4ms
    benchmark { part2(puzzles[1]) }
    benchmark(100) { part3(puzzles[2]) }
}

internal object Day20 : Challenge {
    override fun assertCorrect() {
        check(1045, "P1 Example") { part1(examples[0]) }
        check(1031, "P1 Puzzle") { part1(puzzles[0]) }

        check(24, "P2 Example A") { part2(examples[1]) }
        check(78, "P2 Example B") { part2(examples[2]) }
        check(206, "P2 Example C") { part2(examples[3]) }
        check(0, "P2 Puzzle") { part2(puzzles[1]) }

        check(0, "P3 Example") { part3(examples[4]) }
        check(0, "P3 Puzzle") { part3(puzzles[2]) }
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

private fun part2(input: Grid): Int = 0

private fun part3(input: Grid): Int = 0
