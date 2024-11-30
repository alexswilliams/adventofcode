package ec2024.day19

import common.*

private val examples = loadFilesToLines("ec2024/day19", "example1.txt", "example2.txt")
private val puzzles = loadFilesToLines("ec2024/day19", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day19.assertCorrect()
    benchmark { part1(puzzles[0]) } // 5.4µs
    benchmark { part2(puzzles[1]) } // 57µs
    benchmark { part3(puzzles[2]) } // 870µs
}

internal object Day19 : Challenge {
    override fun assertCorrect() {
        check("WIN", "P1 Example") { part1(examples[0]) }
        check("3122475823549152", "P1 Puzzle") { part1(puzzles[0]) }

        check("VICTORY", "P2 Example") { part2(examples[1]) }
        check("1925785457415943", "P2 Puzzle") { part2(puzzles[1]) }

        check("7625713577112228", "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String = decryptAfterRounds(input, 1)
private fun part2(input: List<String>): String = decryptAfterRounds(input, 100)
private fun part3(input: List<String>): String = decryptAfterRounds(input, 1048576000)


private fun decryptAfterRounds(input: List<String>, rounds: Int): String {
    val operations = input.first().cyclicIterator()
    val grid = input.drop(2).asArrayOfCharArrays()

    val transforms = mutableListOf<Array<IntArray>>()
    var lastTransform = buildTransformationMatrix(operations, grid.size, grid[0].size)
    var target = rounds
    var lowest = target.takeLowestOneBit()
    var current = 1
    while (lowest > 0) {
        if (lowest == current) {
            transforms.add(lastTransform)
            target -= lowest
            lowest = target.takeLowestOneBit()
        }
        lastTransform = lastTransform.applyTransformation(lastTransform)
        current = current shl 1
    }

    return findSolutionString(transforms.fold(grid) { lastGrid, transform -> lastGrid.applyTransformation(transform) })
}

private fun buildTransformationMatrix(operations: Iterator<Char>, rows: Int, cols: Int) =
    Array(rows) { row -> IntArray(cols) { col -> row by16 col } }.also {
        val rotationPoints = cartesianProductOf(1..rows - 2, 1..cols - 2).toTypedArray()
        for (center in rotationPoints) it.rotateSquareAround(center, if (operations.next() == 'L') 1 else -1)
    }

private fun Array<IntArray>.rotateSquareAround(center: Pair<Int, Int>, amountCw: Int) {
    val (row, col) = center
    val original = intArrayOf(
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

private fun Array<IntArray>.applyTransformation(transformation: Array<IntArray>): Array<IntArray> =
    Array(this.size) { row ->
        IntArray(this[0].size) { col ->
            val cell = transformation[row][col]
            this[cell.row()][cell.col()]
        }
    }

private fun Grid.applyTransformation(transformation: Array<IntArray>): Grid =
    Array(this.size) { row ->
        CharArray(this[0].size) { col ->
            val cell = transformation[row][col]
            this[cell.row()][cell.col()]
        }
    }

private fun findSolutionString(grid: Grid): String {
    val solutionRow = grid.indexOfFirst { '>' in it && '<' in it }
    return grid[solutionRow].concatToString(grid[solutionRow].indexOf('>') + 1, grid[solutionRow].indexOf('<'))
}
