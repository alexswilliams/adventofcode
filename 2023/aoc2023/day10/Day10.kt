package aoc2023.day10

import common.*
import java.io.*


// You know it'll be an awful day when they give you FIVE different example inputs...
private val examples = loadFilesToGrids("aoc2023/day10", "example1.txt", "example2.txt", "example3.txt", "example4.txt", "example5.txt")
private val puzzles = loadFilesToGrids("aoc2023/day10", "input.txt")

internal fun main() {
    renderMap(examples[0], "2023/aoc2023/day10/renderedMap1.txt")
    renderMap(examples[1], "2023/aoc2023/day10/renderedMap2.txt")
    renderMap(examples[2], "2023/aoc2023/day10/renderedMap3.txt")
    renderMap(examples[3], "2023/aoc2023/day10/renderedMap4.txt")
    renderMap(examples[4], "2023/aoc2023/day10/renderedMap5.txt")
    renderMap(puzzles[0], "2023/aoc2023/day10/renderedMapInput.txt")

    Day10.assertCorrect()
    benchmark { part1(puzzles[0]) } // 49µs
    benchmark { part2(puzzles[0]) } // 179µs
}

internal object Day10 : Challenge {
    override fun assertCorrect() {
        check(4, "P1 Example 1") { part1(examples[0]) }
        check(8, "P1 Example 2") { part1(examples[1]) }
        check(7107, "P1 Puzzle") { part1(puzzles[0]) }

        check(4, "P2 Example 3") { part2(examples[2]) }
        check(8, "P2 Example 4") { part2(examples[3]) }
        check(10, "P2 Example 5") { part2(examples[4]) }
        check(281, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private fun part1(grid: Grid): Int {
    val startLocation = findStart(grid)
    val shapeOfS = determineShapeOfS(grid, startLocation)
    tailrec fun walkPath(location: Location, exclude: Location = startLocation, length: Int = 1): Int =
        if (location == startLocation) length
        else walkPath(grid.findValidMoveFrom(location, shapeOfS, exclude), location, length + 1)
    return walkPath(grid.findValidMoveFrom(startLocation, shapeOfS, -1 by -1)) / 2
}

private fun part2(grid: Grid): Int {
    val startLocation = findStart(grid)
    val shapeOfS = determineShapeOfS(grid, startLocation)
    val loopBitmap = pathLocations(startLocation, grid, shapeOfS)
    return grid.asIterable().sumOfIndexed { rowIndex, row ->
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

private fun findStart(input: Grid): Location {
    val startRow = input.indexOfFirst { 'S' in it }
    val startCol = input[startRow].indexOf('S')
    val startLocation = startRow by startCol
    return startLocation
}

private fun pathLocations(startLocation: Location, grid: Grid, shapeOfS: Char): Array<BooleanArray> {
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

private fun determineShapeOfS(grid: Grid, start: Location): Char {
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

private fun Grid.findValidMoveFrom(location: Location, shapeOfS: Char, exclude: Location): Location {
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
private fun renderMap(grid: Grid, filename: String) {
    val startRow = grid.indexOfFirst { 'S' in it }
    val startCol = grid[startRow].indexOf('S')
    val shapeOfS = determineShapeOfS(grid, startRow by startCol)
    val steps = pathLocations(startRow by startCol, grid, shapeOfS)
        .flatMapIndexed { rowNum, row -> row.mapIndexed { colNum, col -> if (col) rowNum by colNum else null }.filterNotNull() }
    File(filename).writeText(
        grid.mapTo(arrayListOf()) { line ->
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


