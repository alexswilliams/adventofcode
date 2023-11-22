package aoc2022.day14

import common.*
import java.awt.Color.*
import java.awt.image.*
import java.io.*
import javax.imageio.*
import kotlin.test.*

private val exampleInput = "aoc2022/day14/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day14/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 24
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 93

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 757

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 24943
}

private fun part1(input: List<String>): Int {
    val grid = parseInputToMap(input).toMutableMap()

    val xMin = grid.keys.minOf { it.first }
    val xMax = grid.keys.maxOf { it.first }
    val yMax = grid.keys.maxOf { it.second }

    val grainStart = 500 to 0
    val bounds = Triple(xMin, xMax, yMax)
    var grains = 0
    while (true) {
        val endPosition = simulateGrain(grainStart, grid, bounds)
        grid[endPosition] = 'o'
        if (endPosition.first < xMin || endPosition.first > xMax || endPosition.second > yMax) break
//        printGrid(grid, "part1", grains)
        grains++
    }
    return grains
}

private fun part2(input: List<String>): Int {
    val grid = parseInputToMap(input).toMutableMap()

    val yMax = grid.keys.maxOf { it.second + 2 }
    val xMin = grid.keys.minOf { it.first } - (yMax + 3)
    val xMax = grid.keys.maxOf { it.first } + (yMax + 3)

    (xMin..xMax).forEach { x -> grid[x to yMax] = '~' }

    val grainStart = 500 to 0
    val bounds = Triple(xMin, xMax, yMax)
    var grains = 0
    while (true) {
        grains++
        val endPosition = simulateGrain(grainStart, grid, bounds)
        grid[endPosition] = 'o'
//        printGrid(grid, "part2", grains)
        if (endPosition == grainStart) break
    }
    return grains
}

private fun parseInputToMap(input: List<String>) = input
    .map {
        it.split(" -> ")
            .map { xy ->
                xy.split(',').let { (x, y) -> x.toInt() to y.toInt() }
            }
    }.flatMap { points ->
        points.zipWithNext().flatMap { (prev, xy) ->
            (symmetricRange(prev.first, xy.first)).flatMap { x ->
                (symmetricRange(prev.second, xy.second)).map { y ->
                    (x to y) to '#'
                }
            }
        }
    }.toMap()

private fun simulateGrain(start: Pos, voxels: MutableMap<Pos, Char>, bounds: Triple<Int, Int, Int>): Pos {
    var (x, y) = start
    val (xMin, xMax, yMax) = bounds
    while (true) {
        when {
            x < xMin || x > xMax || y > yMax -> return (x to y) // out of bounds
            (x to y) in voxels -> return x to y // position blocked
            (x to y + 1) !in voxels -> y++ // descend
            (x - 1 to y + 1) !in voxels -> x--.also { y++ } // descend left
            (x + 1 to y + 1) !in voxels -> x++.also { y++ } // descend right
            else -> return x to y // come to rest
        }
    }
}

private typealias Pos = Pair<Int, Int>

private fun symmetricRange(a: Int, b: Int): IntProgression = if (a < b) a..b else a downTo b

// Just used to generate some pretty pictures
private fun printGrid(grid: Map<Pos, Char>, part: String, grain: Int = 0) {
    val xMin = grid.keys.minOf { it.first }
    val xMax = grid.keys.maxOf { it.first }
    val yMax = grid.keys.maxOf { it.second }
    val img = BufferedImage(xMax - xMin + 1, yMax + 1, BufferedImage.TYPE_INT_RGB)
    (0..yMax).flatMap { y ->
        (xMin..xMax).flatMap { x ->
            when (grid.getOrDefault(x to y, '.')) {
                'o' -> orange
                '#' -> green
                '~' -> gray
                else -> black
            }.let { listOf(it.red, it.green, it.blue) }
        }
    }.toIntArray().also { img.raster.setPixels(0, 0, xMax - xMin + 1, yMax + 1, it) }
    ImageIO.write(img, "PNG", File("/tmp/aoc/day14/$part-${grain.toString().padStart(5, '0')}.png"))
}