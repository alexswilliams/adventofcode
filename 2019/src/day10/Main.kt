package day10

import common.fromClasspathFileToLines
import kotlin.math.absoluteValue
import kotlin.test.assertEquals

data class XY(val x: Int, val y: Int)

fun main() {
    runTests()

    val asteroidMap = "day10/input.txt".fromClasspathFileToLines()

    val best = bestPosition(asteroidMap.toAsteroids())
    println(best)

}

private fun bestPosition(asteroids: Set<XY>): Pair<XY, Int> {
    val asteroidsWithVisibilities = asteroids.associateWith { asteroids.visibleFrom(it) }
    return asteroidsWithVisibilities.entries.maxBy { it.value }?.toPair() ?: error("No asteroids provided")
}

private fun Set<XY>.visibleFrom(candidate: XY): Int {
    if (isEmpty()) error("Cannot operate on empty asteroid map")
    val xRange = IntRange(map { it.x }.min()!!, map { it.x }.max()!!)
    val yRange = IntRange(map { it.y }.min()!!, map { it.y }.max()!!)


    val excludedPoints = map { asteroid ->
        if (asteroid == candidate) return@map setOf(candidate)

        tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

        val distance = XY(asteroid.x - candidate.x, asteroid.y - candidate.y)
        val divisor = gcd(distance.x.absoluteValue, distance.y.absoluteValue)
        val step = XY(distance.x / divisor, distance.y / divisor)
        val xProgression = generateSequence(asteroid.x) { i -> if (i + step.x in xRange) (i + step.x) else null }
        val yProgression = generateSequence(asteroid.y) { i -> if (i + step.y in yRange) (i + step.y) else null }
        val progression = xProgression.zip(yProgression) { x, y -> XY(x, y) }
        progression.toList().drop(1).toSet()
    }.reduce { acc, set -> acc.union(set) }

    val remaining = this.subtract(excludedPoints)
    return remaining.size
}

private fun List<String>.toAsteroids(): Set<XY> =
    mapIndexed { y, line ->
        line.mapIndexedNotNull { x, c ->
            if (c == '#') XY(x, y) else null
        }
    }.flatten().toSet()


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
    assertEquals(XY(3,4) to 8, bestPosition(mapSmall))

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
}
