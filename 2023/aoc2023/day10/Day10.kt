package aoc2023.day10

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
//    benchmark { part1(puzzleInput) } // 900µs
//    benchmark { part2(puzzleInput) }
}

private fun part1(grid: List<CharArray>): Int {
    val startRow = grid.indexOfFirst { 'S' in it }
    val startCol = grid[startRow].indexOf('S')
    val steps = walkPath(
        startRow to startCol,
        grid,
        determineShapeOfS(grid, startRow to startCol)
    )
    return steps.size / 2
}

private fun part2(input: List<CharArray>): Int {
    val startRow = input.indexOfFirst { 'S' in it }
    val startCol = input[startRow].indexOf('S')
    val shapeOfS = determineShapeOfS(input, startRow to startCol)
    val steps = walkPath(startRow to startCol, input, shapeOfS)

    var countInside = 0
    input.forEachIndexed { rowIndex, row ->
        var insidePath = false
        var col = 0
        while (col <= row.lastIndex) {
            val char = row[col]
            val shape = if (char == 'S') shapeOfS else char
            when {
                shape == '|' && (rowIndex to col) in steps -> insidePath = !insidePath
                (shape == 'F' || shape == 'L') && (rowIndex to col) in steps -> {
                    var runEndShape: Char
                    do {
                        val charInRun = row[++col]
                        runEndShape = if (charInRun == 'S') shapeOfS else charInRun
                    } while (runEndShape == '-')
                    if ((shape == 'F' && runEndShape == 'J') || (shape == 'L' && runEndShape == '7')) insidePath = !insidePath
                }
                insidePath -> countInside++
            }
            col++
        }
    }
    return countInside
}

private fun walkPath(startLocation: Pair<Int, Int>, grid: List<CharArray>, shapeOfS: Char): List<Pair<Int, Int>> {
    var lastLocation = startLocation
    var thisLocation = grid.findValidMovesFrom(startLocation, shapeOfS).first()
    val steps = mutableListOf(startLocation, thisLocation)
    while (thisLocation != startLocation) {
        val nextLocation = grid.findValidMovesFrom(thisLocation, shapeOfS).minus(lastLocation).single()
        steps.add(nextLocation)
        lastLocation = thisLocation
        thisLocation = nextLocation
    }
    return steps
}

private fun determineShapeOfS(grid: List<CharArray>, locationOfS: Pair<Int, Int>): Char {
    val (row, col) = locationOfS
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

private fun List<CharArray>.findValidMovesFrom(location: Pair<Int, Int>, shapeOfS: Char): List<Pair<Int, Int>> {
    val (row, col) = location
    val pipeAtLocation = this[row][col]
    val pipeShape = if (pipeAtLocation == 'S') shapeOfS else pipeAtLocation
    return when (pipeShape) {
        '|' -> listOf((row - 1) to col, (row + 1) to col)
        '-' -> listOf(row to (col - 1), row to (col + 1))
        'J' -> listOf(row to (col - 1), (row - 1) to col)
        'L' -> listOf(row to (col + 1), (row - 1) to col)
        'F' -> listOf(row to (col + 1), (row + 1) to col)
        '7' -> listOf(row to (col - 1), (row + 1) to col)
        else -> throw Exception("Strayed off pipe network considering $location")
    }
}


private fun renderMap(input: List<CharArray>, filename: String) {
    val startRow = input.indexOfFirst { 'S' in it }
    val startCol = input[startRow].indexOf('S')
    val shapeOfS = determineShapeOfS(input, startRow to startCol)
    val steps = walkPath(startRow to startCol, input, shapeOfS)
    File(filename).writeText(
        rerender(input).toMutableList()
            .also { grid ->
                steps.forEach { (row, col) ->
                    grid[row][col] = emboldenPath(grid[row][col])
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
