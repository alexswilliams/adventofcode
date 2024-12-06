package aoc2023.day17

import common.*
import kotlin.math.*


private val examples = loadFilesToGrids("aoc2023/day17", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToGrids("aoc2023/day17", "input.txt")

internal fun main() {
    Day17.assertCorrect()
    benchmark(50) { part1(puzzles[0]) } // 34ms
    benchmark(50) { part2(puzzles[0]) } // 80ms
}

internal object Day17 : Challenge {
    override fun assertCorrect() {
        check(102, "P1 Example 1") { part1(examples[0]) }
        check(9, "P1 Example 3") { part1(examples[2]) } // from a reddit comment
        check(684, "P1 Puzzle") { part1(puzzles[0]) }

        check(94, "P2 Example 1") { part2(examples[0]) }
        check(71, "P2 Example 2") { part2(examples[1]) }
        check(40, "P2 Example 3") { part2(examples[2]) }
        check(822, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private enum class Heading(val nextCell: (Location1616, Int) -> Location1616) {
    Up({ pos, amt -> pos.minusRow(amt) }),
    Down({ pos, amt -> pos.plusRow(amt) }),
    Left({ pos, amt -> pos.minusCol(amt) }),
    Right({ pos, amt -> pos.plusCol(amt) });
}

private fun part1(grid: Grid): Int = totalHeatLoss(grid.toDigitGrid(), 1, 3)
private fun part2(grid: Grid): Int = totalHeatLoss(grid.toDigitGrid(), 4, 10)


private fun totalHeatLoss(grid: DigitGrid, minStraight: Int, maxStraight: Int): Int {
    val start = 0 by16 0
    val target = (grid.height - 1) by16 (grid.width - 1)

    data class Work(val pos: Location1616, val heading: Heading)

    val work = TreeQueue<Work> { manhattanDistance(target, it.pos) }
    work.offer(Work(start, Heading.Right), 0)
    work.offer(Work(start, Heading.Down), 0)
    val shortest = hashMapOf<Work, Int>()

    val neighbours = Array((maxStraight - minStraight + 1) * 2) { Work(0, Heading.Right) }

    while (true) {
        val u = work.poll() ?: throw Error("Map explored with no route to target")
        val totalHeatLoss = shortest[u] ?: 0
        if (u.pos == target) return totalHeatLoss
        val heatOfU = grid[u.pos.row()][u.pos.col()]

        (minStraight..maxStraight).forEach {
            when (u.heading) {
                Heading.Up, Heading.Down -> {
                    neighbours[(it - minStraight) * 2] = Work(Heading.Left.nextCell(u.pos, it), Heading.Left)
                    neighbours[(it - minStraight) * 2 + 1] = Work(Heading.Right.nextCell(u.pos, it), Heading.Right)
                }

                Heading.Left, Heading.Right -> {
                    neighbours[(it - minStraight) * 2] = Work(Heading.Up.nextCell(u.pos, it), Heading.Up)
                    neighbours[(it - minStraight) * 2 + 1] = Work(Heading.Down.nextCell(u.pos, it), Heading.Down)
                }
            }
        }

        for (key in neighbours) {
            if (key.pos.row() !in grid.rowIndices || key.pos.col() !in grid.colIndices) continue
            val newDistance = totalHeatLoss + sumOfDigitsBetween(grid, key.pos, u.pos) - heatOfU
            val oldDistance = shortest[key] ?: Int.MAX_VALUE
            if (newDistance < oldDistance) {
                shortest[key] = newDistance
                work.offerOrReposition(key, oldDistance, newDistance)
            }
        }
    }
}

private fun manhattanDistance(a: Location1616, b: Location1616): Int = abs(a.row() - b.row()) + abs(a.col() - b.col())

private fun sumOfDigitsBetween(grid: DigitGrid, n: Location1616, u: Location1616): Int =
    IntProgression.fromClosedRange(n.row(), u.row(), if (u.row() < n.row()) -1 else 1)
        .sumOf { row -> IntProgression.fromClosedRange(n.col(), u.col(), if (u.col() < n.col()) -1 else 1).sumOf { col -> grid[row][col] } }


