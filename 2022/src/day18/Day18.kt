package day18

import common.*
import kotlin.test.*
import kotlin.time.*

private val exampleInput = "day18/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day18/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 64
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 58

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 4504, took 2.8ms

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 2556, took 8.7ms

    repeat(20) { part1(puzzleInput) }
    println(measureTime { repeat(100) { part1(puzzleInput) } }.div(100))
    repeat(20) { part2(puzzleInput) }
    println(measureTime { repeat(100) { part2(puzzleInput) } }.div(100))
}

private fun part1(input: List<String>): Int {
    val allPoints = input.map { it.split(',').let { (x, y, z) -> Point3D(x.toInt(), y.toInt(), z.toInt()) } }

    val allPointsAsInt = allPoints.map { xyzAsInt(it.x, it.y, it.z) }.toSet()
    var openFaces = 0
    for (point in allPoints) {
        if (xyzAsInt(point.x, point.y, point.z - 1) !in allPointsAsInt) openFaces++
        if (xyzAsInt(point.x, point.y, point.z + 1) !in allPointsAsInt) openFaces++
        if (xyzAsInt(point.x, point.y - 1, point.z) !in allPointsAsInt) openFaces++
        if (xyzAsInt(point.x, point.y + 1, point.z) !in allPointsAsInt) openFaces++
        if (xyzAsInt(point.x - 1, point.y, point.z) !in allPointsAsInt) openFaces++
        if (xyzAsInt(point.x + 1, point.y, point.z) !in allPointsAsInt) openFaces++
    }
    return openFaces
}

private fun part2(input: List<String>): Int {
    val allPoints = input.map { it.split(',').let { (x, y, z) -> Point3D(x.toInt(), y.toInt(), z.toInt()) } }

    // Create bounding box
    val (xRange, yRange, zRange) = axisRanges(allPoints)
    val steam = mutableSetOf<Int>()
    for (x in xRange.first - 1..xRange.last + 1) {
        for (y in yRange.first - 1..yRange.last + 1) {
            steam.add(xyzAsInt(x, y, zRange.first - 1))
            steam.add(xyzAsInt(x, y, zRange.last + 1))
        }
    }
    for (x in xRange.first - 1..xRange.last + 1) {
        for (z in zRange.first - 1..zRange.last + 1) {
            steam.add(xyzAsInt(x, yRange.first - 1, z))
            steam.add(xyzAsInt(x, yRange.last + 1, z))
        }
    }
    for (y in yRange.first - 1..yRange.last + 1) {
        for (z in zRange.first - 1..zRange.last + 1) {
            steam.add(xyzAsInt(xRange.first - 1, y, z))
            steam.add(xyzAsInt(xRange.last + 1, y, z))
        }
    }

    // Expand steam until steam coverage stops changing
    val allPointsAsInt = allPoints.map { xyzAsInt(it.x, it.y, it.z) }.toSet()
    var beforeSize: Int
    do {
        beforeSize = steam.size
        for (x in xRange) {
            for (y in yRange) {
                for (z in zRange) {
                    val thisPoint = xyzAsInt(x, y, z)
                    if (thisPoint in steam || thisPoint in allPointsAsInt) continue
                    if (xyzAsInt(x, y, z - 1) in steam || xyzAsInt(x, y, z + 1) in steam
                        || xyzAsInt(x, y - 1, z) in steam || xyzAsInt(x, y + 1, z) in steam
                        || xyzAsInt(x - 1, y, z) in steam || xyzAsInt(x + 1, y, z) in steam
                    ) steam.add(thisPoint)
                }
            }
        }
    } while (beforeSize != steam.size)

    // Test to see what's touching just steam
    var openFaces = 0
    for (point in allPoints) {
        if (xyzAsInt(point.x, point.y, point.z - 1) in steam) openFaces++
        if (xyzAsInt(point.x, point.y, point.z + 1) in steam) openFaces++
        if (xyzAsInt(point.x, point.y - 1, point.z) in steam) openFaces++
        if (xyzAsInt(point.x, point.y + 1, point.z) in steam) openFaces++
        if (xyzAsInt(point.x - 1, point.y, point.z) in steam) openFaces++
        if (xyzAsInt(point.x + 1, point.y, point.z) in steam) openFaces++
    }
    return openFaces
}

private fun axisRanges(allPoints: List<Point3D>) = Triple(
    allPoints.minOf { it.x }..allPoints.maxOf { it.x },
    allPoints.minOf { it.y }..allPoints.maxOf { it.y },
    allPoints.minOf { it.z }..allPoints.maxOf { it.z })


private data class Point3D(val x: Int, val y: Int, val z: Int)

private fun xyzAsInt(x: Int, y: Int, z: Int) = ((x and 0xff) shl 16) or ((y and 0xff) shl 8) or (z and 0xff)
