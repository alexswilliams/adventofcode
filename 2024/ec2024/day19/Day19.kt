package ec2024.day19

import common.*

private val examples = loadFilesToLines("ec2024/day19", "example1.txt", "example2.txt")
private val puzzles = loadFilesToLines("ec2024/day19", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day19.assertCorrect()
    benchmark { part1(puzzles[0]) } // 8.3µs
    benchmark { part2(puzzles[1]) } // 1.7ms
//    benchmark(1) { part3(puzzles[2]) }
}

internal object Day19 : Challenge {
    override fun assertCorrect() {
        check("WIN", "P1 Example") { part1(examples[0]) }
        check("3122475823549152", "P1 Puzzle") { part1(puzzles[0]) }

        check("VICTORY", "P2 Example") { part2(examples[1]) }
        check("1925785457415943", "P2 Puzzle") { part2(puzzles[1]) }

        error("More coffee needed")
//        check("", "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String {
    val operations = input.first().cyclicIterator()
    val grid = input.drop(2).asArrayOfCharArrays()
    val rotationPoints = cartesianProductOf(1..grid.lastIndex - 1, 1..grid[0].lastIndex - 1)

    rotationPoints.forEach { center -> grid.rotateSquareAround(center, if (operations.next() == 'L') 1 else -1) }

    val solutionRow = grid.indexOfFirst { '>' in it && '<' in it }
    return grid[solutionRow].concatToString(grid[solutionRow].indexOf('>') + 1, grid[solutionRow].indexOf('<'))
}

private fun part2(input: List<String>): String {
    val operations = input.first().cyclicIterator()
    val grid = input.drop(2).asArrayOfCharArrays()
    val rotationPoints = cartesianProductOf(1..grid.lastIndex - 1, 1..grid[0].lastIndex - 1)

    repeat(26 * 3 + 22) { // in the example, the full grid cycles every 26*n+22 iterations; the full example seems not to (in a practical amount of time to brute force)
        operations.reset()
        rotationPoints.forEach { center -> grid.rotateSquareAround(center, if (operations.next() == 'L') 1 else -1) }
    }

    val solutionRow = grid.indexOfFirst { '>' in it && '<' in it }
    return grid[solutionRow].concatToString(grid[solutionRow].indexOf('>') + 1, grid[solutionRow].indexOf('<'))
}

private fun part3(input: List<String>): String {
    val operations = input.first().cyclicIterator()
    val grid = input.drop(2).asArrayOfCharArrays()
    val rotationPoints = cartesianProductOf(1..grid.lastIndex - 1, 1..grid[0].lastIndex - 1).toTypedArray()

    repeat(1048576000) { i ->
        if (i % 10000 == 0) println((i * 100.0) / 1048576000.0) // could be a loooong time.
        operations.reset()
        rotationPoints.forEach { center -> grid.rotateSquareAround(center, if (operations.next() == 'L') 1 else -1) }
        checkForSolution(grid, i)
    }

    val solutionRow = grid.indexOfFirst { '>' in it && '<' in it }
    return grid[solutionRow].concatToString(grid[solutionRow].indexOf('>') + 1, grid[solutionRow].indexOf('<'))
}


private fun checkForSolution(grid: Grid, i: Int) {
    val solutionRow = grid.indexOfFirst { '>' in it && '<' in it }
    if (solutionRow != -1 &&
        grid[solutionRow].indexOf('>') + 1 < grid[solutionRow].indexOf('<') &&
        grid[solutionRow].concatToString(grid[solutionRow].indexOf('>') + 1, grid[solutionRow].indexOf('<')).all { it.isDigit() }
    ) {
        println("Found >...< at iteration $i")
        grid.forEach { chars -> println(chars.concatToString()) }
        println()
    }
}

private fun Grid.rotateSquareAround(center: Pair<Int, Int>, amountCw: Int) {
    val (row, col) = center
    val original = charArrayOf(
        this[row - 1][col - 1], this[row - 1][col], this[row - 1][col + 1],
        this[row][col + 1],
        this[row + 1][col + 1], this[row + 1][col], this[row + 1][col - 1],
        this[row][col - 1],
    )
    this[row - 1][col - 1] = original[(8 + amountCw) and 7]
    this[row - 1][col] = original[1 + amountCw]
    this[row - 1][col + 1] = original[2 + amountCw]
    this[row][col + 1] = original[3 + amountCw]
    this[row + 1][col + 1] = original[4 + amountCw]
    this[row + 1][col] = original[5 + amountCw]
    this[row + 1][col - 1] = original[6 + amountCw]
    this[row][col - 1] = original[(7 + amountCw) and 7]
}
