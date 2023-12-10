package aoc2023.day10

import common.benchmark
import common.fromClasspathFile
import common.linesAsCharArrays
import java.io.File
import kotlin.test.assertEquals


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
    benchmark { part1(puzzleInput) } // 135µs
    benchmark { part2(puzzleInput) } // 2.44ms
}

private fun part1(input: List<CharArray>): Int {
    val (startLocation, shapeOfS) = findStart(input)
    return pathLength(startLocation, input, shapeOfS) / 2
}

private fun part2(input: List<CharArray>): Int {
    val (startLocation, shapeOfS) = findStart(input)
    val loop = pathLocations(startLocation, input, shapeOfS)
    return input.mapIndexed { rowIndex, row ->
        var totalInsideForRow = 0
        var insidePath = false
        var col = 0
        while (col <= row.lastIndex) {
            val char = row[col]
            val shape = if (char == 'S') shapeOfS else char
            when {
                (shape == '|') && (rowIndex by col) in loop -> insidePath = !insidePath
                (shape == 'F' || shape == 'L') && (rowIndex by col) in loop -> {
                    // a run of edge pieces will follow - ╔═╝ and ╚═╗ will flip 'insidePath' but ╔═╗ and ╚═╝ won't
                    var runEndShape: Char
                    do {
                        val charInRun = row[++col]
                        runEndShape = if (charInRun == 'S') shapeOfS else charInRun
                    } while (runEndShape == '-')
                    if ((shape == 'F' && runEndShape == 'J') || (shape == 'L' && runEndShape == '7')) insidePath = !insidePath
                }
                // The puzzle guarantees that any 'J','7','-' here will not be on the main loop, so can be considered junk
                insidePath -> totalInsideForRow++
            }
            col++
        }
        totalInsideForRow
    }.sum()
}

private fun findStart(input: List<CharArray>): Pair<Location, Char> {
    val startRow = input.indexOfFirst { 'S' in it }
    val startCol = input[startRow].indexOf('S')
    val startLocation = startRow by startCol
    val shapeOfS = determineShapeOfS(input, startLocation)
    return startLocation to shapeOfS
}

private fun pathLocations(startLocation: Location, grid: List<CharArray>, shapeOfS: Char): Set<Location> {
    val path = sortedSetOf(startLocation)
    tailrec fun walkPath(location: Location, exclude: Location = startLocation): Set<Location> {
        path.add(location)
        return if (location == startLocation) path else walkPath(grid.findValidMoveFrom(location, shapeOfS, exclude), location)
    }
    return walkPath(grid.findValidMoveFrom(startLocation, shapeOfS, -1 by -1))
}

private fun pathLength(startLocation: Location, grid: List<CharArray>, shapeOfS: Char): Int {
    tailrec fun walkPath(location: Location, exclude: Location = startLocation, length: Int = 1): Int =
        if (location == startLocation) length else walkPath(grid.findValidMoveFrom(location, shapeOfS, exclude), location, length + 1)
    return walkPath(grid.findValidMoveFrom(startLocation, shapeOfS, -1 by -1))
}

private fun determineShapeOfS(grid: List<CharArray>, locationOfS: Location): Char {
    val row = locationOfS.row()
    val col = locationOfS.col()
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

private fun List<CharArray>.findValidMoveFrom(location: Location, shapeOfS: Char, excluding: Location): Location {
    val row = location.row()
    val col = location.col()
    val exclRow = excluding.row()
    val exclCol = excluding.col()
    val pipeAtLocation = this[row][col]
    val pipeShape = if (pipeAtLocation == 'S') shapeOfS else pipeAtLocation
    return when (pipeShape) {
        '|' -> if (exclRow == row + 1 && exclCol == col) (row - 1) by col else (row + 1) by col
        '-' -> if (exclRow == row && exclCol == col + 1) row by (col - 1) else row by (col + 1)
        'J' -> if (exclRow == row - 1 && exclCol == col) row by (col - 1) else (row - 1) by col
        'L' -> if (exclRow == row - 1 && exclCol == col) row by (col + 1) else (row - 1) by col
        'F' -> if (exclRow == row + 1 && exclCol == col) row by (col + 1) else (row + 1) by col
        '7' -> if (exclRow == row + 1 && exclCol == col) row by (col - 1) else (row + 1) by col
        else -> throw Exception("Strayed off pipe network considering $location")
    }
}

private typealias Location = Int

private infix fun Int.by(col: Int): Location = (this shl 16) or col
private fun Location.row() = this shr 16
private fun Location.col() = this and 0xffff


// Just for fun, but a search in a text editor for █ was the fastest way to get the puzzle answer!
private fun renderMap(input: List<CharArray>, filename: String) {
    val startRow = input.indexOfFirst { 'S' in it }
    val startCol = input[startRow].indexOf('S')
    val shapeOfS = determineShapeOfS(input, startRow by startCol)
    val steps = pathLocations(startRow by startCol, input, shapeOfS)
    File(filename).writeText(
        rerender(input).toMutableList()
            .also { grid ->
                steps.forEach { location ->
                    grid[location.row()][location.col()] = emboldenPath(grid[location.row()][location.col()])
                }
                grid.forEachIndexed { rowIndex, row ->
                    var insidePath = false
                    var col = 0
                    while (col <= row.lastIndex) {
                        val c = if (row[col] == 'S') emboldenPath(shapeOfS) else row[col]
                        if (c == '║') insidePath = !insidePath
                        else if (c == '╔' || c == '╚') {
                            var endChar: Char
                            do {
                                col++
                                endChar = if (row[col] == 'S') emboldenPath(shapeOfS) else row[col]
                            } while (endChar == '═')
                            if ((c == '╔' && endChar == '╝') || (c == '╚' && endChar == '╗')) insidePath = !insidePath
                        } else if (insidePath) {
                            grid[rowIndex][col] = '█'
                        }
                        col++
                    }
                }
            }
            .joinToString("\n") { it.joinToString("") }
    )
}

private fun rerender(s: List<CharArray>) = s.map { line ->
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
}

private fun emboldenPath(c: Char) = when (c) {
    '|', '│' -> '║'
    '-', '─' -> '═'
    'F', '┌' -> '╔'
    '7', '┐' -> '╗'
    'J', '┘' -> '╝'
    'L', '└' -> '╚'
    else -> c
}
