package ec2024.day14

import common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.HashMap.newHashMap
import kotlin.math.absoluteValue
import kotlin.test.assertEquals

private const val rootFolder = "ec2024/day14"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFile()
private val example2Input = "$rootFolder/example2.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFile()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day14.assertPart1Correct()
    Day14.assertPart2Correct()
    Day14.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 22µs
    benchmark { part2(puzzle2Input) } // 1.3ms
    benchmark(10) { part3(puzzle3Input) } // 58.1ms
}

internal object Day14 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(7, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(140, it) }
    }

    override fun assertPart2Correct() {
        part2(example2Input).also { println("[Example] Part 2: $it") }.also { assertEquals(32, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(5172, it) }
    }

    override fun assertPart3Correct() {
        part3(example2Input).also { println("[Example] Part 3: $it") }.also { assertEquals(5, it) }
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(46, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(1494, it) }
    }
}


private data class Point3D(val x: Int, val y: Int, val z: Int)


private fun part1(input: String): Int =
    input.split(',').runningFold(0) { currentHeight, string ->
        when (string[0]) {
            'U' -> currentHeight + string.drop(1).toInt()
            'D' -> currentHeight - string.drop(1).toInt()
            else -> currentHeight
        }
    }.max()

private fun part2(input: List<String>): Int =
    buildBranches(input)
        .flatten().distinct().count() - 1

private fun part3(input: List<String>): Int {
    val allBranches = buildBranches(input)
    val grid = allBranches.flatten().toSet()
    val neighbourhoods = grid.associate { point -> point to neighboursOfPoint(point, grid) }

    val mainBranch = grid.filter { it.x == 0 && it.y == 0 && it.z != 0 }.distinct()
    val leaves = allBranches.map { it.last() }.distinct()

    return runBlocking(Dispatchers.Default) {
        mainBranch.map { candidate ->
            async {
                leaves.sumOf { leaf -> aStarSearch(candidate, leaf, neighbourhoods) }
            }
        }.awaitAll()
    }.min()
}


private fun buildBranches(input: List<String>): List<List<Point3D>> =
    input.map { string ->
        string.split(',').runningFold(listOf(Point3D(0, 0, 0))) { previousRun, op ->
            val distance = op.drop(1).toInt()
            val last = previousRun.last()
            when (op[0]) {
                'U' -> (1..distance).map { last.copy(z = last.z + it) }
                'D' -> (1..distance).map { last.copy(z = last.z - it) }
                'L' -> (1..distance).map { last.copy(x = last.x - it) }
                'R' -> (1..distance).map { last.copy(x = last.x + it) }
                'F' -> (1..distance).map { last.copy(y = last.y + it) }
                'B' -> (1..distance).map { last.copy(y = last.y - it) }
                else -> throw Error()
            }
        }.flatten()
    }

private fun aStarSearch(
    start: Point3D,
    end: Point3D,
    neighboursOf: Map<Point3D, Collection<Point3D>>,
    heuristic: (Point3D) -> Int = { manhattan(it, end) },
): Int {
    val heap = TreeStack<Point3D>(heuristic)
    val shortestPath = newHashMap<Point3D, Int>(neighboursOf.size)
    shortestPath[start] = 0
    heap.offer(start, weight = 0)

    while (true) {
        val u = heap.poll() ?: throw Error("All routes explored with no solution")
        val distanceToU = shortestPath[u] ?: Int.MAX_VALUE
        if (u == end) return distanceToU

        for (n in neighboursOf[u]!!) {
            val originalDistance = shortestPath[n] ?: Int.MAX_VALUE
            val newDistance = distanceToU + 1
            if (newDistance < originalDistance) {
                heap.offerOrReposition(n, originalDistance, newDistance)
                shortestPath[n] = newDistance
            }
        }
    }
}

private fun manhattan(a: Point3D, b: Point3D): Int {
    return (a.x - b.x).absoluteValue + (a.y - b.y).absoluteValue + (a.z - b.z).absoluteValue
}

private fun neighboursOfPoint(center: Point3D, grid: Set<Point3D>): Collection<Point3D> = buildList(6) {
    center.copy(z = center.z - 1).also { if (it in grid) add(it) }
    center.copy(z = center.z + 1).also { if (it in grid) add(it) }
    center.copy(x = center.x - 1).also { if (it in grid) add(it) }
    center.copy(x = center.x + 1).also { if (it in grid) add(it) }
    center.copy(y = center.y - 1).also { if (it in grid) add(it) }
    center.copy(y = center.y + 1).also { if (it in grid) add(it) }
}
