package aoc2022.day18

import common.*


private val example = loadFilesToLines("aoc2022/day18", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day18", "input.txt").single()

internal fun main() {
    Day18.assertCorrect()
    benchmark { part1(puzzle) } // 867.9µs
    benchmark { part2(puzzle) } // 4.1ms
}

internal object Day18 : Challenge {
    override fun assertCorrect() {
        check(64, "P1 Example") { part1(example) }
        check(4504, "P1 Puzzle") { part1(puzzle) }

        check(58, "P2 Example") { part2(example) }
        check(2556, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: List<String>): Int {
    val allPoints = input.map { it.split(',').let { (x, y, z) -> Point3D(x.toInt(), y.toInt(), z.toInt()) } }

    val allPointsAsInt = allPoints.map { xyzAsInt(it.x, it.y, it.z) }.toSet()
    var openFaces = 0
    for ((x, y, z) in allPoints) {
        if (xyzAsInt(x, y, z - 1) !in allPointsAsInt) openFaces++
        if (xyzAsInt(x, y, z + 1) !in allPointsAsInt) openFaces++
        if (xyzAsInt(x, y - 1, z) !in allPointsAsInt) openFaces++
        if (xyzAsInt(x, y + 1, z) !in allPointsAsInt) openFaces++
        if (xyzAsInt(x - 1, y, z) !in allPointsAsInt) openFaces++
        if (xyzAsInt(x + 1, y, z) !in allPointsAsInt) openFaces++
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
    for ((x, y, z) in allPoints) {
        if (xyzAsInt(x, y, z - 1) in steam) openFaces++
        if (xyzAsInt(x, y, z + 1) in steam) openFaces++
        if (xyzAsInt(x, y - 1, z) in steam) openFaces++
        if (xyzAsInt(x, y + 1, z) in steam) openFaces++
        if (xyzAsInt(x - 1, y, z) in steam) openFaces++
        if (xyzAsInt(x + 1, y, z) in steam) openFaces++
    }
    return openFaces
}

private fun axisRanges(allPoints: List<Point3D>) = Triple(
    allPoints.minOf { it.x }..allPoints.maxOf { it.x },
    allPoints.minOf { it.y }..allPoints.maxOf { it.y },
    allPoints.minOf { it.z }..allPoints.maxOf { it.z })


private data class Point3D(val x: Int, val y: Int, val z: Int)

private fun xyzAsInt(x: Int, y: Int, z: Int) = ((x and 0xff) shl 16) or ((y and 0xff) shl 8) or (z and 0xff)
