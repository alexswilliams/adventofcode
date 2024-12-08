package aoc2024.day8

import common.*

private val examples = loadFilesToGrids("aoc2024/day8", "example1.txt", "example2.txt")
private val puzzle = loadFilesToGrids("aoc2024/day8", "input.txt").single()

internal fun main() {
    Day8.assertCorrect()
    benchmark(10000) { part1(puzzle) } // 18µs
    benchmark(10000) { part2(puzzle) } // 34µs
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
        findReflectingAntennae(grid).forEach { nodes ->
            nodes.forEach { pos ->
                nodes.forEach { otherPos ->
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
        findReflectingAntennae(grid).forEach { nodes ->
            nodes.forEachIndexed { n, pos ->
                for (m in (n + 1)..nodes.lastIndex) {
                    val otherPos = nodes[m]
                    val vDist = pos.row() - otherPos.row()
                    val hDist = pos.col() - otherPos.col()
                    add(pos)
                    add(otherPos)
                    addAntiNodes(pos, -vDist, -hDist, grid)
                    addAntiNodes(otherPos, vDist, hDist, grid)
                }
            }
        }
    }.size


private fun MutableSet<Location1616>.addAntiNodes(antenna: Location1616, vDist: Int, hDist: Int, grid: Grid) {
    var i = 1
    var antiNode = (antenna.row() - vDist * i) by16 (antenna.col() - hDist * i)
    while (antiNode isWithin grid) {
        add(antiNode)
        i++
        antiNode = (antenna.row() - vDist * i) by16 (antenna.col() - hDist * i)
    }
}

private fun findReflectingAntennae(grid: Grid): Sequence<List<Location1616>> {
    val byChar = mutableMapOf<Char, MutableList<Location1616>>()
    grid.forEachIndexed { rowNum, row -> row.forEachIndexed { colNum, c -> if (c != '.') byChar.getOrPut(c) { mutableListOf() }.add(rowNum by16 colNum) } }
    return byChar.values.asSequence().filterNot { it.size == 1 }
}
