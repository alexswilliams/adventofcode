package aoc2023.day10

import common.Location
import common.benchmark
import common.by
import common.colInt
import common.fromClasspathFile
import common.linesAsCharArrays
import common.minusCol
import common.minusRow
import common.plusCol
import common.plusRow
import common.rowInt
import common.sumOfIndexed
import java.io.File
import kotlin.test.assertEquals


// You know it'll be an awful day when they give you FIVE different example inputs...
private val exampleInput1 = "aoc2023/day10/example1.txt".fromClasspathFile().linesAsCharArrays()
private val exampleInput2 = "aoc2023/day10/example2.txt".fromClasspathFile().linesAsCharArrays()
private val exampleInput3 = "aoc2023/day10/example3.txt".fromClasspathFile().linesAsCharArrays()
private val exampleInput4 = "aoc2023/day10/example4.txt".fromClasspathFile().linesAsCharArrays()
private val exampleInput5 = "aoc2023/day10/example5.txt".fromClasspathFile().linesAsCharArrays()
private val puzzleInput = "aoc2023/day10/input.txt".fromClasspathFile().linesAsCharArrays()

fun main() {
    renderMap(exampleInput1, "2023/aoc2023/day10/renderedMap1.txt")
    renderMap(exampleInput2, "2023/aoc2023/day10/renderedMap2.txt")
    renderMap(exampleInput3, "2023/aoc2023/day10/renderedMap3.txt")
    renderMap(exampleInput4, "2023/aoc2023/day10/renderedMap4.txt")
    renderMap(exampleInput5, "2023/aoc2023/day10/renderedMap5.txt")
    renderMap(puzzleInput, "2023/aoc2023/day10/renderedMapInput.txt")

    part1(exampleInput1).also { println("[Example 1] Part 1: $it") }.also { assertEquals(4, it) }
    part1(exampleInput2).also { println("[Example 2] Part 1: $it") }.also { assertEquals(8, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(7107, it) }
    part2(exampleInput3).also { println("[Example 3] Part 2: $it") }.also { assertEquals(4, it) }
    part2(exampleInput4).also { println("[Example 4] Part 2: $it") }.also { assertEquals(8, it) }
    part2(exampleInput5).also { println("[Example 5] Part 2: $it") }.also { assertEquals(10, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(281, it) }
    benchmark { part1(puzzleInput) } // 49µs
    benchmark { part2(puzzleInput) } // 179µs
}

private fun part1(input: List<CharArray>): Int {
    val startLocation = findStart(input)
    val shapeOfS = determineShapeOfS(input, startLocation)
    tailrec fun walkPath(location: Location, exclude: Location = startLocation, length: Int = 1): Int =
        if (location == startLocation) length
        else walkPath(input.findValidMoveFrom(location, shapeOfS, exclude), location, length + 1)
    return walkPath(input.findValidMoveFrom(startLocation, shapeOfS, -1 by -1)) / 2
}

private fun part2(input: List<CharArray>): Int {
    val startLocation = findStart(input)
    val shapeOfS = determineShapeOfS(input, startLocation)
    val loopBitmap = pathLocations(startLocation, input, shapeOfS)
    return input.sumOfIndexed { rowIndex, row ->
        val isOnLoop = loopBitmap[rowIndex]
        var totalInsideForRow = 0
        var insidePath = false
        var col = 0
        do {
            val char = row[col]
            val shape = if (char == 'S') shapeOfS else char
            when {
                (shape == '|') && isOnLoop[col] -> insidePath = !insidePath
                (shape == 'F' || shape == 'L') && isOnLoop[col] -> {
                    // a run of edge pieces will follow - ╔═╝ and ╚═╗ will flip 'insidePath' but ╔═╗ and ╚═╝ won't
                    var runEndShape: Char
                    do {
                        val charInRun = row[++col]
                        runEndShape = if (charInRun == 'S') shapeOfS else charInRun
                    } while (runEndShape == '-')
                    if ((shape == 'F' && runEndShape == 'J') || (shape == 'L' && runEndShape == '7')) insidePath = !insidePath
                }
                // The puzzle guarantees that any ╝,╗,═ will never start a run, so cannot be on the main loop
                insidePath -> totalInsideForRow++
            }
            col++
        } while (col < row.lastIndex)
        totalInsideForRow
    }
}

private fun findStart(input: List<CharArray>): Location {
    val startRow = input.indexOfFirst { 'S' in it }
    val startCol = input[startRow].indexOf('S')
    val startLocation = startRow by startCol
    return startLocation
}

private fun pathLocations(startLocation: Location, grid: List<CharArray>, shapeOfS: Char): Array<BooleanArray> {
    val path = Array(grid.size) { BooleanArray(grid[0].size) }
    tailrec fun walkPath(location: Location, exclude: Location = startLocation): Array<BooleanArray> {
        path[location.rowInt()][location.colInt()] = true
        return if (location == startLocation) path else
            walkPath(grid.findValidMoveFrom(location, shapeOfS, exclude), location)
    }
    return walkPath(
        grid.findValidMoveFrom(startLocation, shapeOfS, -1 by -1)
    )
}

private fun determineShapeOfS(grid: List<CharArray>, start: Location): Char {
    val row = start.rowInt()
    val col = start.colInt()
    val up = if (row == 0) null else grid[row - 1][col]
    val down = if (row == grid.lastIndex) null else grid[row + 1][col]
    val left = if (col == 0) null else grid[row][col - 1]
    val right = if (col == grid[row].lastIndex) null else grid[row][col + 1]

    val possibleShapes = mutableListOf('|', '-', 'J', 'L', 'F', '7')
    if (up != '|' && up != 'F' && up != '7') possibleShapes.removeAll(listOf('|', 'J', 'L'))
    if (down != '|' && down != 'L' && down != 'J') possibleShapes.removeAll(listOf('|', 'F', '7'))
    if (left != '-' && left != 'F' && left != 'L') possibleShapes.removeAll(listOf('-', 'J', '7'))
    if (right != '-' && right != 'J' && right != '7') possibleShapes.removeAll(listOf('-', 'L', 'F'))
    return possibleShapes.single()
}

private fun List<CharArray>.findValidMoveFrom(location: Location, shapeOfS: Char, exclude: Location): Location {
    val char = this[location.rowInt()][location.colInt()]
    val shape = if (char == 'S') shapeOfS else char
    return when (shape) {
        '|' -> if (exclude == location.plusRow()) location.minusRow() else location.plusRow()
        '-' -> if (exclude == location.plusCol()) location.minusCol() else location.plusCol()
        'J' -> if (exclude == location.minusRow()) location.minusCol() else location.minusRow()
        'L' -> if (exclude == location.minusRow()) location.plusCol() else location.minusRow()
        'F' -> if (exclude == location.plusRow()) location.plusCol() else location.plusRow()
        '7' -> if (exclude == location.plusRow()) location.minusCol() else location.plusRow()
        else -> throw Exception("Strayed off pipe network considering $location")
    }
}

// Just for fun, but a search in a text editor for █ was the fastest way to get the puzzle answer!
private fun renderMap(input: List<CharArray>, filename: String) {
    val startRow = input.indexOfFirst { 'S' in it }
    val startCol = input[startRow].indexOf('S')
    val shapeOfS = determineShapeOfS(input, startRow by startCol)
    val steps = pathLocations(startRow by startCol, input, shapeOfS)
        .flatMapIndexed { rowNum, row -> row.mapIndexed { colNum, col -> if (col) rowNum by colNum else null }.filterNotNull() }
    File(filename).writeText(
        input.mapTo(arrayListOf()) { line ->
            line.mapTo(arrayListOf()) {
                when (it) {
                    '|' -> '│'
                    '-' -> '─'
                    'F' -> '┌'
                    '7' -> '┐'
                    'J' -> '┘'
                    'L' -> '└'
                    '.' -> '░'
                    else -> it
                }
            }
        }.also { grid ->
            fun emboldenPath(c: Char) = when (c) {
                '|', '│' -> '║'
                '-', '─' -> '═'
                'F', '┌' -> '╔'
                '7', '┐' -> '╗'
                'J', '┘' -> '╝'
                'L', '└' -> '╚'
                else -> c
            }
            steps.forEach { location ->
                grid[location.rowInt()][location.colInt()] = emboldenPath(grid[location.rowInt()][location.colInt()])
            }
            grid.forEachIndexed { rowIndex, row ->
                var insidePath = false
                var col = 0
                while (col <= row.lastIndex) {
                    val c = if (row[col] == 'S') emboldenPath(shapeOfS) else row[col]
                    if (c == '║') insidePath = !insidePath
                    else if (c == '╔' || c == '╚') {
                        var endChar: Char
                        do endChar = if (row[++col] == 'S') emboldenPath(shapeOfS) else row[col] while (endChar == '═')
                        if ((c == '╔' && endChar == '╝') || (c == '╚' && endChar == '╗')) insidePath = !insidePath
                    } else if (insidePath) grid[rowIndex][col] = '█'
                    col++
                }
            }
        }.joinToString("\n") { it.joinToString("") }
    )
}


