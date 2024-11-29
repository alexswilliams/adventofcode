package ec2024.day10

import common.*

private val examples = loadFilesToGrids("ec2024/day10", "example.txt", "example3.txt")
private val puzzles = loadFilesToGrids("ec2024/day10", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day10.assertCorrect()
    benchmark { part1(puzzles[0]) } // 19µs
    benchmark { part2(puzzles[1]) } // 656.9µs
    benchmark { part3(puzzles[2]) } // 1.4ms
}

internal object Day10 : Challenge {
    override fun assertCorrect() {
        check("PTBVRCZHFLJWGMNS", "P1 Example") { part1(examples[0]) }
        check("FHMTJRGZSLKWCXVD", "P1 Puzzle") { part1(puzzles[0]) }

        check(1851, "P2 Example") { part2(examples[0]) }
        check(195008, "P2 Puzzle") { part2(puzzles[1]) }

        check(3889, "P3 Example") { part3(examples[1]) }
        check(212096, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: Grid): String =
    squareToRuneWord(input).joinToString("")

private fun part2(input: Grid): Int =
    input.asSequence().chunked(9) { chunk -> chunk.dropLastWhile { it.isEmpty() } }
        .flatMap { it.splitArrayOnSpaces().transpose() }
        .map { it.asArrayOfCharArrays() }
        .sumOf { squareToRuneWord(it).basePower }


private fun squareToRuneWord(square: Grid) = CharArray(16) { i ->
    val rowCodex = arrayOf(square[i / 4 + 2][0], square[i / 4 + 2][1], square[i / 4 + 2][6], square[i / 4 + 2][7])
    val colCodex = arrayOf(square[0][i % 4 + 2], square[1][i % 4 + 2], square[6][i % 4 + 2], square[7][i % 4 + 2])
    (rowCodex intersect colCodex.asIterable()).singleOrNull() ?: '.'
}

private val CharArray.basePower: Int
    get() = this.foldIndexed(0) { index, acc, ch -> acc + (ch - 'A' + 1) * (index + 1) }


private fun part3(grid: Grid): Int {
    val squareTopLefts = cartesianProductOf(
        IntProgression.fromClosedRange(0, grid.lastIndex - 2, 6),
        IntProgression.fromClosedRange(0, grid[0].lastIndex - 2, 6)
    ).filterNot { topLeft -> isUnsolvable(topLeft, grid) }.toMutableList()

    // Fill in what's easily derivable, leave '.' behind if there wasn't a unique letter for that position
    squareTopLefts.forEach { (row, col) ->
        squareToRuneWord(grid.subGrid(row, col, 8, 8)).apply {
            copyInto(grid[row + 2], col + 2, 0, 4)
            copyInto(grid[row + 3], col + 2, 4, 8)
            copyInto(grid[row + 4], col + 2, 8, 12)
            copyInto(grid[row + 5], col + 2, 12, 16)
        }
    }

    val unsolvedCells = squareTopLefts.flatMap { (r, c) -> allUnsolvedCoordsInSquare(r, c, grid) }.toMutableList()
    while (unsolvedCells.isNotEmpty()) {
        val solvedCells = mutableListOf<Pair<Int, Int>>()
        unsolvedCells.forEach { cell ->
            val (r, c) = cell
            val localRow = r % 6
            val localCol = c % 6

            val rowCodex = arrayOf(grid[r][c - localCol + 0], grid[r][c - localCol + 1], grid[r][c - localCol + 6], grid[r][c - localCol + 7])
            val colCodex = arrayOf(grid[r - localRow + 0][c], grid[r - localRow + 1][c], grid[r - localRow + 6][c], grid[r - localRow + 7][c])
            val rowWord = arrayOf(grid[r][c - localCol + 2], grid[r][c - localCol + 3], grid[r][c - localCol + 4], grid[r][c - localCol + 5])
            val colWord = arrayOf(grid[r - localRow + 2][c], grid[r - localRow + 3][c], grid[r - localRow + 4][c], grid[r - localRow + 5][c])

            when {
                '?' !in rowCodex && rowWord.count { it == '.' } == 1 -> {
                    val missingLetter = rowCodex.subtract(rowWord.asIterable()).single()
                    grid[r][c] = missingLetter
                    solvedCells.add(cell)
                    if (missingLetter !in colCodex && colCodex.count { it == '?' } == 1) {
                        grid[codexToSquare(colCodex.indexOf('?')) + (r - localRow)][c] = missingLetter
                    }
                }
                '?' !in colCodex && colWord.count { it == '.' } == 1 -> {
                    val missingLetter = colCodex.subtract(colWord.asIterable()).single()
                    grid[r][c] = missingLetter
                    solvedCells.add(cell)
                    if (missingLetter !in rowCodex && rowCodex.count { it == '?' } == 1) {
                        grid[r][codexToSquare(rowCodex.indexOf('?')) + (c - localCol)] = missingLetter
                    }
                }
                ('?' !in colCodex && '?' !in rowCodex) -> {
                    grid[r][c] = (rowCodex intersect colCodex.asIterable()).single()
                    solvedCells.add(cell)
                }
            }
        }

        unsolvedCells.removeAll(solvedCells)
        // In theory this step might be necessary for a really complex grid, but the puzzle input seems to avoid it.
        // squareTopLefts.removeAll(squareTopLefts.filter { isUnsolvable(it, grid) })
        // unsolvedCells = squareTopLefts.flatMapTo(ArrayList()) { (r, c) -> unsolvedCoordsForSquare(r, c, grid) }
    }

    return squareTopLefts
        .map { (topR, leftC) -> CharArray(16) { i -> grid[topR + i / 4 + 2][leftC + i % 4 + 2] } }
        .filterNot { chars -> '.' in chars }
        .sumOf { chars -> chars.basePower }
}

fun codexToSquare(i: Int) = when (i) {
    0, 1 -> i
    2, 3 -> i + 4
    else -> throw Error()
}

private fun allUnsolvedCoordsInSquare(r: Int, c: Int, grid: Grid): List<Pair<Int, Int>> =
    innerCoords.map { (ir, ic) -> (r + ir) to (c + ic) }.filter { (ir, ic) -> grid[ir][ic] == '.' }


private fun List<Char>.uniqueLettersPlusQuestionMarks() =
    partition { it == '?' }.let { (q, l) -> l.distinct().size + q.size }

private fun isUnsolvable(topLeft: Pair<Int, Int>, grid: Grid): Boolean {
    val (squareRow, squareCol) = topLeft
    val top = topCoords.map { (r, c) -> grid[r + squareRow][c + squareCol] }
    val bottom = bottomCoords.map { (r, c) -> grid[r + squareRow][c + squareCol] }
    val left = leftCoords.map { (r, c) -> grid[r + squareRow][c + squareCol] }
    val right = rightCoords.map { (r, c) -> grid[r + squareRow][c + squareCol] }
    return when {
        (top.filterNot { it == '?' } intersect bottom).isNotEmpty() -> true
        (left.filterNot { it == '?' } intersect right).isNotEmpty() -> true
        top.uniqueLettersPlusQuestionMarks() != 8 -> true
        bottom.uniqueLettersPlusQuestionMarks() != 8 -> true
        left.uniqueLettersPlusQuestionMarks() != 8 -> true
        right.uniqueLettersPlusQuestionMarks() != 8 -> true
        else -> false
    }
}

private val topCoords = cartesianProductOf(listOf(0, 1), listOf(2, 3, 4, 5))
private val bottomCoords = cartesianProductOf(listOf(6, 7), listOf(2, 3, 4, 5))
private val leftCoords = cartesianProductOf(listOf(2, 3, 4, 5), listOf(0, 1))
private val rightCoords = cartesianProductOf(listOf(2, 3, 4, 5), listOf(6, 7))
private val innerCoords = cartesianProductOf(listOf(2, 3, 4, 5), listOf(2, 3, 4, 5))
