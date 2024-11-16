package ec2024.day10

import common.*
import kotlin.test.assertEquals

private const val rootFolder = "ec2024/day10"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day10.assertPart1Correct()
    Day10.assertPart2Correct()
    Day10.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 32µs
    benchmark { part2(puzzle2Input) } // 1.23ms
    benchmark { part3(puzzle3Input) } //
}

internal object Day10 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals("PTBVRCZHFLJWGMNS", it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals("FHMTJRGZSLKWCXVD", it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(1851, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(195008, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(3889, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(0, it) }
    }
}


private fun part1(input: List<String>): String {
    return squareToRuneWord(input)
}

private fun squareToRuneWord(input: List<String>): String {
    val h = input.subList(2, 6).map { it.filterNot { ch -> ch == '.' }.toSet() }
    val v = input.transposeToStrings().subList(2, 6).map { it.filterNot { ch -> ch == '.' }.toSet() }
    return (0..3).joinToString("") { row -> (0..3).map { col -> (h[row] intersect v[col]).singleOrNull() ?: '.' }.joinToString("") }
}

private fun String.basePower() = this.foldIndexed(0) { index, acc, ch -> acc + (ch - 'A' + 1) * (index + 1) }


private fun part2(input: List<String>): Int {
    val squares = input.chunked(9) { chunk -> chunk.dropLastWhile { string -> string.isBlank() } }.flatMap { strings -> strings.splitOnSpaces().transpose() }
    val words = squares.map { grid -> squareToRuneWord(grid).basePower() }
    return words.sum()
}

private fun part3(input: List<String>): Int {
    val grid = input.map { it -> it.toCharArray() }.toTypedArray()
    val squareTopLefts = cartesianProductOf(
        IntProgression.fromClosedRange(0, grid.lastIndex - 2, 6),
        IntProgression.fromClosedRange(0, grid[0].lastIndex - 2, 6)
    )
    val innerCoords = cartesianProductOf(listOf(2, 3, 4, 5), listOf(2, 3, 4, 5))
    val rowCoords = cartesianProductOf(listOf(2, 3, 4, 5), listOf(0, 1, 6, 7))
    val colCoords = cartesianProductOf(listOf(0, 1, 6, 7), listOf(2, 3, 4, 5))

    println("Unsolved Grid")
    printGrid(grid)

    // Fill in what's easily derivable, leave '.' behind if there wasn't a unique letter for that position
    squareTopLefts.forEach { (row, col) ->
        val word = squareToRuneWord(input.subList(row, row + 8).map { line -> line.substring(col, col + 8) }).toCharArray()
        word.copyInto(grid[row + 2], col + 2, 0, 4)
        word.copyInto(grid[row + 3], col + 2, 4, 8)
        word.copyInto(grid[row + 4], col + 2, 8, 12)
        word.copyInto(grid[row + 5], col + 2, 12, 16)
    }

    println("After naive round")
    printGrid(grid)

    val unsolvedCells = grid.indices.flatMap { row -> grid[0].indices.filter { col -> grid[row][col] == '.' }.map { col -> row to col } }.toMutableList()
    var cellsRemaining: Int
    do {
        println("Unsolved cell locations: ${unsolvedCells.size}")
        unsolvedCells.forEach { (row, col) ->
            val localRow = row % 6
            val localCol = col % 6
            val innerRows = innerCoords.groupBy({ (r, _) -> r }) { (r, c) -> grid[row - localRow + r][col - localCol + c] }
            val innerCols = innerCoords.groupBy({ (_, c) -> c }) { (r, c) -> grid[row - localRow + r][col - localCol + c] }
            val rows = rowCoords.groupBy({ (r, _) -> r }) { (r, c) -> grid[row - localRow + r][col - localCol + c] }
            val cols = colCoords.groupBy({ (_, c) -> c }) { (r, c) -> grid[row - localRow + r][col - localCol + c] }

            println(innerRows)
            println(innerCols)
            println("$row,$col -> $localRow,$localCol")
            println("rows[localRow] ${rows[localRow]}")
            println("innerRows[localRow] ${innerRows[localRow]}")
            println("cols[localCol] ${cols[localCol]}")
            println("innerCols[localCol] ${innerCols[localCol]}")
            println("cols[localCol] - innerCols[localCol] ${cols[localCol]!!.subtract(innerCols[localCol]!!)}")
            println("rows[localRow] - innerRows[localRow] ${rows[localRow]!!.subtract(innerRows[localRow]!!)}")

            val singleCol = cols[localCol]!!.subtract(innerCols[localCol]!!.plus('?')).singleOrNull()
            if (singleCol != null) {
                val markInRow = rows[localRow]!!.indexOf('?')
                println("Found COL $singleCol for $row, $col and ? ${markInRow + if (markInRow <= 1) 0 else 4}")
                grid[row][col] = singleCol
                if (markInRow >= 0)
                    grid[row][col - localCol + markInRow + if (markInRow <= 1) 0 else 4] = singleCol
            }
            val singleRow = rows[localRow]!!.subtract(innerRows[localRow]!!.plus('?')).singleOrNull()
            if (singleRow != null) {
                val markInCol = cols[localCol]!!.indexOf('?')
                println("Found ROW $singleRow for $row, $col and ? ${markInCol + if (markInCol <= 1) 0 else 4}")
                grid[row][col] = singleRow
                if (markInCol >= 0)
                    grid[row - localRow + markInCol + if (markInCol <= 1) 0 else 4][col] = singleRow
            }
        }
        cellsRemaining = unsolvedCells.size
        unsolvedCells.removeAll { (row, col) -> grid[row][col] != '.' }
    } while (cellsRemaining != unsolvedCells.size)

    println("After Iterating Solutions")
    printGrid(grid)

    return squareTopLefts.map { (row, col) ->
        innerCoords.groupBy({ (r, _) -> r }) { (r, c) -> grid[row + r][col + c] }.map { (_, line) -> line.joinToString("") }.joinToString("")
    }.also { println(it) }
        .filterNot { '.' in it }.sumOf { it.basePower() }
}

private fun printGrid(arrays: Array<CharArray>) {
    arrays.forEach { line ->
        println(line.concatToString())
    }
    println()
}

