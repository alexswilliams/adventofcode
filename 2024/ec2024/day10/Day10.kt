package ec2024.day10

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import common.*

private val examples = loadFilesToLines("ec2024/day10", "example.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day10", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day10.assertCorrect()
    benchmark { part1(puzzles[0]) } // 21µs
    benchmark { part2(puzzles[1]) } // 610µs
    benchmark(100) { part3(puzzles[2]) } // 3.2ms
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


private fun part1(input: List<String>): String =
    squareToRuneWord(input.asArrayOfCharArrays()).joinToString("")

private fun part2(input: List<String>): Int =
    input.chunked(9) { chunk -> chunk.dropLastWhile { it.isBlank() } }
        .flatMap { it.splitOnSpaces().transpose() }
        .map { it.asArrayOfCharArrays() }
        .sumOf { squareToRuneWord(it).basePower }


private fun squareToRuneWord(square: Array<CharArray>) = CharArray(16) { i ->
    val rowCodex = arrayOf(square[i / 4 + 2][0], square[i / 4 + 2][1], square[i / 4 + 2][6], square[i / 4 + 2][7])
    val colCodex = listOf(square[0][i % 4 + 2], square[1][i % 4 + 2], square[6][i % 4 + 2], square[7][i % 4 + 2])
    (rowCodex intersect colCodex).singleOrNull() ?: '.'
}

private val CharArray.basePower: Int
    get() = this.foldIndexed(0) { index, acc, ch -> acc + (ch - 'A' + 1) * (index + 1) }


private fun part3(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
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


    var unsolvedCells = squareTopLefts.flatMapTo(ArrayList()) { (r, c) -> unsolvedCoordsForSquare(r, c, grid) }
    while (unsolvedCells.isNotEmpty()) {
//        printGrid(grid)
//        println(unsolvedCells)

        unsolvedCells.forEach { (r, c) ->
            val localRow = r % 6
            val localCol = c % 6
            val topR = r - localRow
            val leftC = c - localCol
            val square = grid.subGrid(topR, leftC, 8, 8)

            val rowCodex = arrayOf(square[localRow][0], square[localRow][1], square[localRow][6], square[localRow][7])
            val colCodex = arrayOf(square[0][localCol], square[1][localCol], square[6][localCol], square[7][localCol])
            val rowWord = arrayOf(square[localRow][2], square[localRow][3], square[localRow][4], square[localRow][5])
            val colWord = arrayOf(square[2][localCol], square[3][localCol], square[4][localCol], square[5][localCol])
//            println("Unsolved cell: $r,$c")
//            println("  Row codex: ${rowCodex.joinToString(",")}, Row Word: ${rowWord.joinToString(",")}")
//            println("  Col codex: ${colCodex.joinToString(",")}, Col Word: ${colWord.joinToString(",")}")

            when {
                rowWord.count { it == '.' } == 1 && '?' !in rowCodex -> {
                    val missingLetter = rowCodex.subtract(rowWord.toSet()).single()
                    grid[r][c] = missingLetter
//                    println("  - replacing $r,$c with $missingLetter because row contained no ?")
                    if (missingLetter !in colCodex && colCodex.count { it == '?' } == 1) {
                        grid[codexToSquare(colCodex.indexOf('?')) + topR][c] = missingLetter
//                        println("  - replacing ? at ${codexToSquare(colCodex.indexOf('?')) + topR},$c with $missingLetter")
                    }
                }
                colWord.count { it == '.' } == 1 && '?' !in colCodex -> {
                    val missingLetter = colCodex.subtract(colWord.toSet()).single()
                    grid[r][c] = missingLetter
//                    println("  - replacing $r,$c with $missingLetter because column contained no ?")
                    if (missingLetter !in rowCodex && rowCodex.count { it == '?' } == 1) {
                        grid[r][codexToSquare(rowCodex.indexOf('?')) + leftC] = missingLetter
//                        println("  - replacing ? at $r,${codexToSquare(rowCodex.indexOf('?')) + leftC} with $missingLetter")
                    }
                }
                ('?' !in colCodex && '?' !in rowCodex) -> {
                    val unusedRowCodex = rowCodex.subtract(rowWord.toSet())
                    val unusedColCodex = colCodex.subtract(colWord.toSet())
                    val missingLetter = (unusedRowCodex intersect unusedColCodex).singleOrNull()
                    if (missingLetter != null) {
                        grid[r][c] = missingLetter
//                        println("  - replacing $r,$c with $missingLetter because neither row nor column contained ?")
                    }
                }
            }

        }

        squareTopLefts.removeAll(squareTopLefts.filter { isUnsolvable(it, grid) })
        unsolvedCells = squareTopLefts.flatMapTo(ArrayList()) { (r, c) -> unsolvedCoordsForSquare(r, c, grid) }
    }

    return squareTopLefts.map { (topR, leftC) ->
        CharArray(16) { i ->
            grid[topR + i / 4 + 2][leftC + i % 4 + 2]
        }
    }.filterNot { chars -> '.' in chars }.sumOf { chars -> chars.basePower }
}

fun codexToSquare(i: Int) = when (i) {
    0, 1 -> i
    2, 3 -> i + 4
    else -> throw Error()
}

private fun unsolvedCoordsForSquare(r: Int, c: Int, grid: Array<CharArray>): List<Pair<Int, Int>> =
    innerCoords.map { (ir, ic) -> (r + ir) to (c + ic) }.filter { (ir, ic) -> grid[ir][ic] == '.' }


private fun isUnsolvable(topLeft: Pair<Int, Int>, grid: Array<CharArray>): Boolean {
    val (squareRow, squareCol) = topLeft
    val top = topCoords.map { (r, c) -> grid[r + squareRow][c + squareCol] }
    val bottom = bottomCoords.map { (r, c) -> grid[r + squareRow][c + squareCol] }
    val left = leftCoords.map { (r, c) -> grid[r + squareRow][c + squareCol] }
    val right = rightCoords.map { (r, c) -> grid[r + squareRow][c + squareCol] }
    if ((top.filterNot { it == '?' } intersect bottom).isNotEmpty()) return true
    if ((left.filterNot { it == '?' } intersect right).isNotEmpty()) return true
    if (top.partition { it == '?' }.let { (q, l) -> l.distinct().size + q.size } != 8) return true
    if (bottom.partition { it == '?' }.let { (q, l) -> l.distinct().size + q.size } != 8) return true
    if (left.partition { it == '?' }.let { (q, l) -> l.distinct().size + q.size } != 8) return true
    if (right.partition { it == '?' }.let { (q, l) -> l.distinct().size + q.size } != 8) return true
    return false
}

private val topCoords = cartesianProductOf(listOf(0, 1), listOf(2, 3, 4, 5))
private val bottomCoords = cartesianProductOf(listOf(6, 7), listOf(2, 3, 4, 5))
private val leftCoords = cartesianProductOf(listOf(2, 3, 4, 5), listOf(0, 1))
private val rightCoords = cartesianProductOf(listOf(2, 3, 4, 5), listOf(6, 7))
private val innerCoords = cartesianProductOf(listOf(2, 3, 4, 5), listOf(2, 3, 4, 5))

private fun printGrid(arrays: Array<CharArray>, offset: Int = 0) {
    arrays.forEachIndexed { lineNo, line ->
        println(
            " ".repeat(offset) +
                    line.concatToString().chunked(2).mapIndexed { index, string ->
                        if (string == "**") "  "
                        else if (string == "??") brightRed(string)
                        else if (string[0] == '?') (brightRed(string[0].toString()) + string[1].toString())
                        else if (string[1] == '?') (string[0].toString() + brightRed(string[1].toString()))
                        else if (index % 3 == 0 || lineNo % 6 <= 1) string
                        else if (string == "..") bold(brightCyan(string))
                        else if (string[0] == '.') (bold(brightCyan(string[0].toString())) + brightCyan(string[1].toString()))
                        else if (string[1] == '.') (brightCyan(string[0].toString()) + bold(brightCyan(string[1].toString())))
                        else brightCyan(string)
                    }.joinToString("")
        )
    }
}
