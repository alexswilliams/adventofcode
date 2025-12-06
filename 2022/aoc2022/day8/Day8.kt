package aoc2022.day8

import common.*


private val example = loadFilesToLines("aoc2022/day8", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day8", "input.txt").single()

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzle) } // 12.0ms
    benchmark { part2(puzzle) } // 7.6ms
}

internal object Day8 : Challenge {
    override fun assertCorrect() {
        check(21, "P1 Example") { part1(example) }
        check(1676, "P1 Puzzle") { part1(puzzle) }

        check(8, "P2 Example") { part2(example) }
        check(313200, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: List<String>) = numberOfTreesAroundTheEdge(input.asGrid()) + visibleInnerSquares(input.asGrid())
private fun part2(input: List<String>) = beautyScores(input.asGrid())


private fun numberOfTreesAroundTheEdge(it: Grid) = it.size * 2 + (it.first().size - 2) * 2
private fun visibleInnerSquares(rowMajorGrid: Grid): Int {
    val colMajorGrid = rowMajorGrid.transpose()
    return cartesianProductOf(1 until rowMajorGrid.lastIndex, 1 until rowMajorGrid.first().lastIndex)
        .count { (row, col) ->
            treesOnEachAxisOrdered(row, col, rowMajorGrid, colMajorGrid).any { it.max() < rowMajorGrid[row][col] }
        }
}

private fun beautyScores(rowMajorGrid: Grid): Int {
    val colMajorGrid = rowMajorGrid.transpose()
    return cartesianProductOf(0..rowMajorGrid.lastIndex, 0..rowMajorGrid.first().lastIndex)
        .maxOf { (row, col) ->
            treesOnEachAxisOrdered(row, col, rowMajorGrid, colMajorGrid)
                .map { treeList -> treeList.takeUntilIncludingItemThatBreaksCondition { it >= rowMajorGrid[row][col] }.size }
                .product()
        }
}

private fun treesOnEachAxisOrdered(row: Int, col: Int, rowMajorGrid: Grid, colMajorGrid: Grid) = listOf(
    colMajorGrid[col].take(row).asReversed(), // above
    colMajorGrid[col].drop(row + 1), // below
    rowMajorGrid[row].take(col).asReversed(), // left
    rowMajorGrid[row].drop(col + 1) // right
)


typealias Grid = List<List<Char>>

private fun List<String>.asGrid(): Grid = this.map { it.toList() }
