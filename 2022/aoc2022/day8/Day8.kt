package aoc2022.day8

import common.*
import kotlin.test.*


private val exampleInput = "aoc2022/day8/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day8/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_ANSWER = 21
private const val PART_2_EXPECTED_ANSWER = 8

fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1: " + part1(puzzleInput)) // 1676

    assertEquals(PART_2_EXPECTED_ANSWER, part2(exampleInput))
    println("Part 2: " + part2(puzzleInput)) // 313200
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
                .map { treeList -> treeList.takeUntilIncludingItemThatBreaksCondition { it >= rowMajorGrid[row][col] }.count() }
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
