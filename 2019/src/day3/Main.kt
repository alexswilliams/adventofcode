package day3

import common.*
import kotlin.math.*
import kotlin.test.*

data class XYC(val x: Int, val y: Int, val count: Int)

fun main() {
    runTests()

    val puzzleInput = "day3/input.txt".fromClasspathFileToLines().map { it.split(',') }

    val points = puzzleInput.map { pointsVisited(it) }

    val intersections = points
        .map { path -> path.map { it.x to it.y }.toSet() }
        .reduce { acc, next -> acc.intersect(next) }
    val withDistancesFromOrigin = intersections.map { (x, y) -> Triple(x, y, manhattanDistance(x, y)) }
    val closestToOrigin = withDistancesFromOrigin.minBy { it.third }
    println("Part 1: shortest = $closestToOrigin, distance = ${closestToOrigin.third}")
    assertEquals(1431, closestToOrigin.third)


    val soonestIntersectionPerPath = points.map { path ->
        path.filter { (it.x to it.y) in intersections }
            .groupBy({ it.x to it.y }) { it.count }
            .mapValues { it.value.min() }
    }
    val soonestIntersectionTotals = soonestIntersectionPerPath.reduce { acc, next ->
        (acc.asSequence() + next.asSequence())
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.sum() }
    }

    val soonest = soonestIntersectionTotals.minBy { it.value }
    println("Part 2: soonest = $soonest, combined distance = ${soonest.value}")
    assertEquals(48012, soonest.value)
}


private fun manhattanDistance(x: Int, y: Int) = abs(x) + abs(y)

private tailrec fun pointsVisited(path: List<String>, points: List<XYC> = emptyList()): List<XYC> {
    return if (path.isEmpty())
        points
    else {
        val newAccumulator = points.plus(pointsFor(path.first(), points.lastOrNull() ?: XYC(0, 0, 0)))
        pointsVisited(path.drop(1), newAccumulator)
    }
}

private fun pointsFor(command: String, start: XYC = XYC(0, 0, 0)): List<XYC> {
    val amount = command.drop(1).toInt()
    val (x, y, count) = start
    return when (command.first()) {
        'U' -> ((y + 1)..(y + amount)).mapIndexed { index, newY -> XYC(x, newY, count + index + 1) }
        'D' -> ((y - 1) downTo (y - amount)).mapIndexed { index, newY -> XYC(x, newY, count + index + 1) }
        'R' -> ((x + 1)..(x + amount)).mapIndexed { index, newX -> XYC(newX, y, count + index + 1) }
        'L' -> ((x - 1) downTo (x - amount)).mapIndexed { index, newX -> XYC(newX, y, count + index + 1) }
        else -> throw Exception("Unknown direction")
    }
}

private fun runTests() {
    assertEquals(6, manhattanDistance(3, 3))

    assertEquals(
        listOf(
            XYC(1, 0, 1), XYC(2, 0, 2), XYC(3, 0, 3), XYC(4, 0, 4),
            XYC(5, 0, 5), XYC(6, 0, 6), XYC(7, 0, 7), XYC(8, 0, 8)
        ), pointsFor("R8")
    )
    assertEquals(
        listOf(XYC(0, 1, 1), XYC(0, 2, 2), XYC(0, 3, 3), XYC(0, 4, 4), XYC(0, 5, 5)),
        pointsFor("U5")
    )
    assertEquals(
        listOf(XYC(8, 1, 9), XYC(8, 2, 10), XYC(8, 3, 11), XYC(8, 4, 12), XYC(8, 5, 13)),
        pointsFor("U5", XYC(8, 0, 8))
    )
    assertEquals(
        listOf(XYC(-1, 0, 1), XYC(-2, 0, 2), XYC(-3, 0, 3), XYC(-4, 0, 4), XYC(-5, 0, 5)),
        pointsFor("L5")
    )
    assertEquals(
        listOf(XYC(0, -1, 1), XYC(0, -2, 2), XYC(0, -3, 3)),
        pointsFor("D3")
    )

    assertEquals(emptyList(), pointsVisited(emptyList()))
    assertEquals(
        listOf(
            XYC(1, 0, 1), XYC(2, 0, 2), XYC(3, 0, 3), XYC(4, 0, 4),
            XYC(5, 0, 5), XYC(6, 0, 6), XYC(7, 0, 7), XYC(8, 0, 8),
            XYC(8, 1, 9), XYC(8, 2, 10), XYC(8, 3, 11), XYC(8, 4, 12), XYC(8, 5, 13),
            XYC(7, 5, 14), XYC(6, 5, 15), XYC(5, 5, 16), XYC(4, 5, 17), XYC(3, 5, 18),
            XYC(3, 4, 19), XYC(3, 3, 20), XYC(3, 2, 21)
        ), pointsVisited(listOf("R8", "U5", "L5", "D3"))
    )
}
