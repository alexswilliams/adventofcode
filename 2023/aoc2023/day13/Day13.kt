package aoc2023.day13

import common.benchmark
import common.fromClasspathFile
import common.transposeToStrings
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day13/example.txt".fromClasspathFile()
private val puzzleInput = "aoc2023/day13/input.txt".fromClasspathFile()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(405, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(33122, it) }
//    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(400, it) }
//    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(0, it) }
    benchmark { part1(puzzleInput) } // 2.35ms
//    benchmark { part2(puzzleInput) } // 268µs
}

private fun part1(input: String): Int {
    val grids = input.split("\n\n").map { it.lines() }
    val allColumnMirrors = grids.mapNotNull { grid -> findMirrorColumns(grid).singleOrNull() }
    val allRowMirrors = grids.mapNotNull { grid -> findMirrorColumns(grid.transposeToStrings()).singleOrNull() }
    return allRowMirrors.sum() * 100 + allColumnMirrors.sum()
}

private fun findMirrorColumns(grid: List<String>) =
    grid.map { line -> (1..line.lastIndex).filter { line[it] == line[it - 1] }.toSet() }
        .reduce { viable, it -> viable.intersect(it) }
        .filter { splitBefore ->
            grid.all { line ->
                val stringBeforePlusExtra = line.substring(0, splitBefore)
                val stringAfterPlusExtra = line.substring(splitBefore)

                stringBeforePlusExtra.takeLast(stringAfterPlusExtra.length) == stringAfterPlusExtra.take(stringBeforePlusExtra.length).reversed()
            }
        }

private fun part2(input: String) = 0
