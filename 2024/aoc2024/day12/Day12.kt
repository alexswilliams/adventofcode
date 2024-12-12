package aoc2024.day12

import common.*

private val examples = loadFilesToGrids("aoc2024/day12", "example1.txt", "example2.txt", "example3.txt", "example4.txt", "example5.txt")
private val puzzle = loadFilesToGrids("aoc2024/day12", "input.txt").single()

internal fun main() {
    Day12.assertCorrect()
    benchmark(10) { part1(puzzle) } // 29.3ms
    benchmark(10) { part2(puzzle) } // 48.6ms
}

internal object Day12 : Challenge {
    override fun assertCorrect() {
        check(140, "P1 Example 1") { part1(examples[0]) }
        check(772, "P1 Example 2") { part1(examples[1]) }
        check(1930, "P1 Example 3") { part1(examples[2]) }
        check(1424006, "P1 Puzzle") { part1(puzzle) }

        check(80, "P2 Example 1") { part2(examples[0]) }
        check(436, "P2 Example 2") { part2(examples[1]) }
        check(236, "P2 Example 3") { part2(examples[3]) }
        check(368, "P2 Example 4") { part2(examples[4]) }
        check(858684, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int {
    val bordersBeneath = grid.mapCartesianNotNull { row, col, char -> if (row == grid.lastIndex || char != grid[row + 1][col]) char to (row by16 col) else null }.groupBy({ it.first }) { it.second }
    val bordersAbove = grid.mapCartesianNotNull { row, col, char -> if (row == 0 || char != grid[row - 1][col]) char to (row by16 col) else null }.groupBy({ it.first }) { it.second }
    val bordersLeft = grid.mapCartesianNotNull { row, col, char -> if (col == 0 || char != grid[row][col - 1]) char to (row by16 col) else null }.groupBy({ it.first }) { it.second }
    val bordersRight = grid.mapCartesianNotNull { row, col, char -> if (col == grid[0].lastIndex || char != grid[row][col + 1]) char to (row by16 col) else null }.groupBy({ it.first }) { it.second }


    val shapes = mutableListOf<Pair<Char, Collection<Location1616>>>()
    val unvisited = mutableSetOf<Location1616>().apply { addAll(grid.mapCartesianNotNull { row, col, _ -> row by16 col }) }
    while (unvisited.isNotEmpty()) {
        val start = unvisited.first()
        val thisLetter = grid.at(start)
        val shape = mutableListOf(start)
        val work = ArrayDeque(listOf(start))
        while (work.isNotEmpty()) {
            val current = work.removeFirst()
            unvisited.remove(current)
            for (n in neighboursOf(current, grid) { ch -> ch == thisLetter }) {
                if (n == -1) continue
                if (n in shape) continue
                shape.add(n)
                unvisited.remove(n)
                work.add(n)
            }
        }
        shapes.add(thisLetter to shape)
    }

    return shapes.sumOf { (letter, pieces) ->
        val perimeter = bordersBeneath[letter]!!.count { it in pieces } +
                bordersAbove[letter]!!.count { it in pieces } +
                bordersLeft[letter]!!.count { it in pieces } +
                bordersRight[letter]!!.count { it in pieces }
        val area = pieces.size
        area * perimeter
    }
}


private fun part2(grid: Grid): Int {
    val bordersBeneath = grid.mapCartesianNotNull { row, col, char -> if (row == grid.lastIndex || char != grid[row + 1][col]) char to (row by16 col) else null }.groupBy({ it.first }) { it.second }
    val bordersAbove = grid.mapCartesianNotNull { row, col, char -> if (row == 0 || char != grid[row - 1][col]) char to (row by16 col) else null }.groupBy({ it.first }) { it.second }
    val bordersLeft = grid.mapCartesianNotNull { row, col, char -> if (col == 0 || char != grid[row][col - 1]) char to (row by16 col) else null }.groupBy({ it.first }) { it.second }
    val bordersRight = grid.mapCartesianNotNull { row, col, char -> if (col == grid[0].lastIndex || char != grid[row][col + 1]) char to (row by16 col) else null }.groupBy({ it.first }) { it.second }

    val shapes = mutableListOf<Pair<Char, Collection<Location1616>>>()
    val unvisited = mutableSetOf<Location1616>().apply { addAll(grid.mapCartesianNotNull { row, col, _ -> row by16 col }) }
    while (unvisited.isNotEmpty()) {
        val start = unvisited.first()
        val thisLetter = grid.at(start)
        val shape = mutableListOf(start)
        val work = ArrayDeque(listOf(start))
        while (work.isNotEmpty()) {
            val current = work.removeFirst()
            unvisited.remove(current)
            for (n in neighboursOf(current, grid) { ch -> ch == thisLetter }) {
                if (n == -1) continue
                if (n in shape) continue
                shape.add(n)
                unvisited.remove(n)
                work.add(n)
            }
        }
        shapes.add(thisLetter to shape)
    }

    return shapes.sumOf { (letter, pieces) ->
        val perimeter = startsOfBorders(bordersBeneath[letter]!!.filter { it in pieces }).size +
                startsOfBorders(bordersAbove[letter]!!.filter { it in pieces }).size +
                startsOfBordersCols(bordersLeft[letter]!!.filter { it in pieces }).size +
                startsOfBordersCols(bordersRight[letter]!!.filter { it in pieces }).size
        val area = pieces.size
        area * perimeter
    }
}

private fun startsOfBorders(borders: List<Location1616>): List<Location1616> {
    val bordersForShape = borders.sorted()
    var last = -1
    val result = arrayListOf<Location1616>()
    bordersForShape.forEach { pos: Int ->
        if (pos.row() != last.row() || pos.col() != last.col() + 1)
            result.add(pos)
        last = pos
    }
    return result
}


private fun startsOfBordersCols(borders: List<Location1616>): List<Location1616> {
    val bordersForShape = borders.sortedWith(compareBy<Location1616> { it.col() }.thenBy { it.row() })
    var last = -1
    val result = arrayListOf<Location1616>()
    bordersForShape.forEach { pos: Int ->
        if (pos.col() != last.col() || pos.row() != last.row() + 1)
            result.add(pos)
        last = pos
    }
    return result
}




















