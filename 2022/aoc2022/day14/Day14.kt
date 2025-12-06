package aoc2022.day14

import common.*
import java.awt.Color.*
import java.awt.image.*
import java.io.*
import javax.imageio.*


private val example = loadFilesToLines("aoc2022/day14", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day14", "input.txt").single()

internal fun main() {
    Day14.assertCorrect()
    benchmark { part1(puzzle) } // 1.8ms
    benchmark(10) { part2(puzzle) } // 161.3ms
}

internal object Day14 : Challenge {
    override fun assertCorrect() {
        check(24, "P1 Example") { part1(example) }
        check(757, "P1 Puzzle") { part1(puzzle) }

        check(93, "P2 Example") { part2(example) }
        check(24943, "P2 Puzzle") { part2(puzzle) }
    }
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
        if (endPosition.first !in xMin..xMax || endPosition.second > yMax) break
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
            x !in xMin..xMax || y > yMax -> return (x to y) // out of bounds
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
@Suppress("unused")
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