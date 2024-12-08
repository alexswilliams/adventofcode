package aoc2024.day8

import common.*

private val examples = loadFilesToGrids("aoc2024/day8", "example1.txt", "example2.txt")
private val puzzle = loadFilesToGrids("aoc2024/day8", "input.txt").single()

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzle) } // 52µs
    benchmark(10) { part2(puzzle) } // 302µs
}

internal object Day8 : Challenge {
    override fun assertCorrect() {
        check(14, "P1 Example") { part1(examples[0]) }
        check(295, "P1 Puzzle") { part1(puzzle) }

        check(34, "P2 Example") { part2(examples[0]) }
        check(9, "P2 Example") { part2(examples[1]) }
        check(1034, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(grid: Grid): Int =
    buildSet {
        findReflectingAntennae(grid).forEach { (_, nodes) ->
            nodes.forEach { (_, pos) ->
                nodes.forEach { (_, otherPos) ->
                    if (pos != otherPos) {
                        val antiNode = (2 * otherPos.row() - pos.row()) by16 (2 * otherPos.col() - pos.col())
                        if (antiNode isWithin grid) add(antiNode)
                    }
                }
            }
        }
    }.size

private fun part2(grid: Grid): Int =
    buildSet {
        findReflectingAntennae(grid).forEach { (_, nodes) ->
            nodes.forEach { (_, pos) ->
                nodes.forEach { (_, otherPos) ->
                    if (pos != otherPos) {
                        add(otherPos)
                        val hDist = pos.col() - otherPos.col()
                        val vDist = pos.row() - otherPos.row()
                        var i = 1
                        var antiNode = (otherPos.row() - vDist * i) by16 (otherPos.col() - hDist * i)
                        while (antiNode isWithin grid) {
                            add(antiNode)
                            i++
                            antiNode = (otherPos.row() - vDist * i) by16 (otherPos.col() - hDist * i)
                        }
                    }
                }
            }
        }
    }.size


private fun findReflectingAntennae(grid: Grid) = grid
    .mapCartesianNotNull { row, col, char -> if (char != '.') char to (row by16 col) else null }
    .groupBy { it.first }.entries
    .asSequence()
    .filterNot { it.value.size == 1 }
