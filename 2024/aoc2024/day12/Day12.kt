package aoc2024.day12

import common.*

private val examples = loadFilesToGrids("aoc2024/day12", "example1.txt", "example2.txt", "example3.txt", "example4.txt", "example5.txt")
private val puzzle = loadFilesToGrids("aoc2024/day12", "input.txt").single()

internal fun main() {
    Day12.assertCorrect()
    benchmark(10) { part1(puzzle) } // 13.9ms
    benchmark(10) { part2(puzzle) } // 12.8ms
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
    val borders = (grid.allLocationsWhere(grid::hasBorderBeneath) +
            grid.allLocationsWhere(grid::hasBorderAbove) +
            grid.allLocationsWhere(grid::hasBorderLeft) +
            grid.allLocationsWhere(grid::hasBorderRight)).groupBy { grid.at(it) }

    return findShapes(grid)
        .sumOf { (letter, pieces) -> pieces.size * borders[letter]!!.count { it in pieces } }
}

private fun part2(grid: Grid): Int {
    val bordersBeneath = grid.allLocationsWhere(grid::hasBorderBeneath).groupBy { grid.at(it) }.mapValues { foldToSides(it.value) }
    val bordersAbove = grid.allLocationsWhere(grid::hasBorderAbove).groupBy { grid.at(it) }.mapValues { foldToSides(it.value) }
    val bordersLeft = grid.allLocationsWhere(grid::hasBorderLeft).groupBy { grid.at(it) }.mapValues { foldToSides(it.value, flip = true) }
    val bordersRight = grid.allLocationsWhere(grid::hasBorderRight).groupBy { grid.at(it) }.mapValues { foldToSides(it.value, flip = true) }

    return findShapes(grid)
        .sumOf { (letter, pieces) ->
            val perimeter = bordersBeneath[letter]!!.count { it in pieces } +
                    bordersAbove[letter]!!.count { it in pieces } +
                    bordersLeft[letter]!!.count { it in pieces } +
                    bordersRight[letter]!!.count { it in pieces }
            pieces.size * perimeter
        }
}


private fun Grid.hasBorderRight(row: Int, col: Int, char: Char) = col == this[0].lastIndex || char != this[row][col + 1]
private fun Grid.hasBorderLeft(row: Int, col: Int, char: Char) = col == 0 || char != this[row][col - 1]
private fun Grid.hasBorderAbove(row: Int, col: Int, char: Char) = row == 0 || char != this[row - 1][col]
private fun Grid.hasBorderBeneath(row: Int, col: Int, char: Char) = row == lastIndex || char != this[row + 1][col]

private fun foldToSides(borders: List<Location1616>, flip: Boolean = false): List<Location1616> = buildList {
    borders.sortedBy { if (flip) it.flip() else it }
        .fold(-1) { last, pos: Int ->
            val current = if (flip) pos.flip() else pos
            if (current.row() != last.row() || current.col() != last.col() + 1)
                add(pos)
            current
        }
}

private fun findShapes(grid: Grid): List<Pair<Char, Collection<Location1616>>> = buildList {
    val unvisited = mutableSetOf<Location1616>().apply { addAll(grid.allLocations()) }
    while (unvisited.isNotEmpty()) {
        val start = unvisited.first()
        val shape = findShapeStartingAt(start, grid)
        add(grid.at(start) to shape)
        unvisited.removeAll(shape)
    }
}

private fun findShapeStartingAt(start: Location1616, grid: Grid, thisLetter: Char = grid.at(start)): Set<Location1616> {
    val shape = mutableSetOf(start)
    val work = ArrayDeque<Location1616>().apply { add(start) }
    while (true) {
        val current = work.removeFirstOrNull() ?: return shape
        for (n in neighboursOf(current, grid) { it == thisLetter }) {
            if (n == -1) continue
            if (n in shape) continue
            shape.add(n)
            work.add(n)
        }
    }
}
