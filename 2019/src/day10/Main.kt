package day10

import common.*
import kotlin.math.*
import kotlin.test.*

private data class XY(val x: Int, val y: Int) {
    fun gradientTo(other: XY) = -(other.x - x).toDouble() / (other.y - y).toDouble()
}

private typealias Box = Pair<XY, XY>


fun main() {
    runTests()

    val asteroidMap = "day10/input.txt".fromClasspathFileToLines()

    val best = bestPosition(asteroidMap.toAsteroids())
    println("Part 1: best position = ${best.first}, visible = ${best.second}")
    assertEquals(230, best.second)

    val kaboomOrder = asteroidMap.toAsteroids().kaboom(best.first)
    val asteroidCode = kaboomOrder[199].let { it.x * 100 + it.y }
    println("Part 2: 200th Asteroid to be blown up: ${kaboomOrder[199]}; code = $asteroidCode")
    assertEquals(1205, asteroidCode)
}


private fun bestPosition(asteroids: Set<XY>): Pair<XY, Int> =
    asteroids.associateWith { asteroids.visibleFrom(it).count() }
        .maxBy { it.value }.toPair()

private fun Set<XY>.visibleFrom(station: XY): Set<XY> {
    if (isEmpty()) return emptySet()

    val grid = boundingBox()
    val occludedPoints = map {
        if (it == station) setOf(station)
        else station.findOcclusionsBy(it, grid)
    }.reduce { acc, set -> acc.union(set) }

    return subtract(occludedPoints)
}

private fun XY.findOcclusionsBy(asteroid: XY, grid: Box): Set<XY> {
    val step = shortestStepTo(asteroid)
    return generateSequence(asteroid) { XY(it.x + step.x, it.y + step.y) }
        .dropWhile { it in (this to asteroid) }
        .takeWhile { it in grid }
        .toSet()
}

private fun Set<XY>.boundingBox(): Box =
    XY(minBy { it.x }.x, minBy { it.y }.y) to
            XY(maxBy { it.x }.x, maxBy { it.y }.y)

private operator fun Box.contains(point: XY) =
    (point.x in min(first.x, second.x)..max(first.x, second.x))
            && (point.y in (min(first.y, second.y)..max(first.y, second.y)))

private fun XY.shortestStepTo(asteroid: XY): XY {
    tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    val distance = XY(asteroid.x - x, asteroid.y - y)
    val divisor = gcd(distance.x.absoluteValue, distance.y.absoluteValue)
    return XY(distance.x / divisor, distance.y / divisor)
}

private fun List<String>.toAsteroids(): Set<XY> =
    mapIndexed { y, line ->
        line.mapIndexedNotNull { x, char -> if (char == '#') XY(x, y) else null }
    }.flatten().toSet()


private tailrec fun Set<XY>.kaboom(blaster: XY, acc: List<XY?> = emptyList()): List<XY> {
    val visibleThisTime = visibleFrom(blaster)

    val north = visibleThisTime.find { it.x == blaster.x && it.y < blaster.y }
    val east = visibleThisTime.find { it.y == blaster.y && it.x > blaster.x }
    val south = visibleThisTime.find { it.x == blaster.x && it.y > blaster.y }
    val west = visibleThisTime.find { it.y == blaster.y && it.x < blaster.x }

    val q1 = visibleThisTime.filter { it.x > blaster.x && it.y < blaster.y }.sortedBy(blaster::gradientTo)
    val q2 = visibleThisTime.filter { it.x > blaster.x && it.y > blaster.y }.sortedBy(blaster::gradientTo)
    val q3 = visibleThisTime.filter { it.x < blaster.x && it.y > blaster.y }.sortedBy(blaster::gradientTo)
    val q4 = visibleThisTime.filter { it.x < blaster.x && it.y < blaster.y }.sortedBy(blaster::gradientTo)

    val explosionOrder = acc + listOf(north) + q1 + listOf(east) + q2 + listOf(south) + q3 + listOf(west) + q4
    val remainingAsteroids = this.subtract(visibleThisTime)

    return if (remainingAsteroids.isEmpty() || (remainingAsteroids.size == 1 && remainingAsteroids.single() == blaster))
        explosionOrder.filterNotNull()
    else
        remainingAsteroids.kaboom(blaster, explosionOrder)
}


private fun runTests() {
    fun String.forTest() = split('\n').filter { it.trim().isNotEmpty() }

    val mapSmall = """
        .#..#
        .....
        #####
        ....#
        ...##
    """.trimIndent().forTest().toAsteroids()
    assertEquals(
        setOf(
            XY(1, 0), XY(4, 0),
            XY(0, 2), XY(1, 2), XY(2, 2), XY(3, 2), XY(4, 2),
            XY(4, 3),
            XY(3, 4), XY(4, 4)
        ),
        mapSmall
    )
    assertEquals(XY(3, 4) to 8, bestPosition(mapSmall))

    val map1 = """
        ......#.#.
        #..#.#....
        ..#######.
        .#.#.###..
        .#..#.....
        ..#....#.#
        #..#....#.
        .##.#..###
        ##...#..#.
        .#....####
    """.trimIndent().forTest().toAsteroids()
    assertEquals(XY(5, 8) to 33, bestPosition(map1))

    val map2 = """
        #.#...#.#.
        .###....#.
        .#....#...
        ##.#.#.#.#
        ....#.#.#.
        .##..###.#
        ..#...##..
        ..##....##
        ......#...
        .####.###.
    """.trimIndent().forTest().toAsteroids()
    assertEquals(XY(1, 2) to 35, bestPosition(map2))

    val map3 = """
        .#..#..###
        ####.###.#
        ....###.#.
        ..###.##.#
        ##.##.#.#.
        ....###..#
        ..#.#..#.#
        #..#.#.###
        .##...##.#
        .....#.#..
    """.trimIndent().forTest().toAsteroids()
    assertEquals(XY(6, 3) to 41, bestPosition(map3))

    val map4 = """
        .#..##.###...#######
        ##.############..##.
        .#.######.########.#
        .###.#######.####.#.
        #####.##.#.##.###.##
        ..#####..#.#########
        ####################
        #.####....###.#.#.##
        ##.#################
        #####.##.###..####..
        ..######..##.#######
        ####.##.####...##..#
        .#####..#.######.###
        ##...#.##########...
        #.##########.#######
        .####.#.###.###.#.##
        ....##.##.###..#####
        .#.#.###########.###
        #.#.#.#####.####.###
        ###.##.####.##.#..##
    """.trimIndent().forTest().toAsteroids()
    assertEquals(XY(11, 13) to 210, bestPosition(map4))


    val smallBlasterMap = """
        .#....#####...#..
        ##...##.#####..##
        ##...#...#.#####.
        ..#.....X...###..
        ..#.#.....#....##
    """.trimIndent().forTest().toAsteroids()
    val blasterStation = XY(8, 3)
    assertEquals(
        listOf(XY(8, 1), XY(9, 0), XY(9, 1), XY(10, 0), XY(9, 2), XY(11, 1), XY(12, 1), XY(11, 2), XY(15, 1)),
        smallBlasterMap.kaboom(blasterStation).take(9)
    )
    assertEquals(
        listOf(XY(12, 2), XY(13, 2), XY(14, 2), XY(15, 2), XY(12, 3), XY(16, 4), XY(15, 4), XY(10, 4), XY(4, 4)),
        smallBlasterMap.kaboom(blasterStation).drop(9).take(9)
    )
    assertEquals(
        listOf(XY(2, 4), XY(2, 3), XY(0, 2), XY(1, 2), XY(0, 1), XY(1, 1), XY(5, 2), XY(1, 0), XY(5, 1)),
        smallBlasterMap.kaboom(blasterStation).drop(18).take(9)
    )
    assertEquals(
        listOf(XY(6, 1), XY(6, 0), XY(7, 0), XY(8, 0), XY(10, 1), XY(14, 0), XY(16, 1), XY(13, 3), XY(14, 3)),
        smallBlasterMap.kaboom(blasterStation).drop(27).take(9)
    )
}
