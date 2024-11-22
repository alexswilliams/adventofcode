package ec2024.day14

import common.*
import kotlinx.coroutines.*
import java.util.HashSet.*
import kotlin.test.*

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
    benchmark { part1(puzzleInput) } // 21µs
    benchmark { part2(puzzle2Input) } // 1.3ms
    benchmark(100) { part3(puzzle3Input) } // 12.6ms
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


private data class Point3D(val x: Int, val y: Int, val z: Int) {
    override fun equals(other: Any?): Boolean = (other as? Point3D)?.let { x == it.x && y == it.y && z == it.z } == true
    override fun hashCode(): Int = (x * 31 + y) * 31 + z
    override fun toString(): String = "($x, $y, $z)"
}


private fun part1(input: String): Int =
    input.split(',').runningFold(0) { currentHeight, string ->
        when (string[0]) {
            'U' -> currentHeight + string.toIntFromIndex(1)
            'D' -> currentHeight - string.toIntFromIndex(1)
            else -> currentHeight
        }
    }.max()

private fun part2(input: List<String>): Int =
    buildBranches(input).flatten().distinct().count()

private fun part3(input: List<String>): Int {
    val allBranches = buildBranches(input)
    val grid = allBranches.flatten().toSet()
    val leaves = allBranches.map { it.last() }.toSet()
    val neighbourhoods = grid.associate { point -> point to neighboursOfPoint(point, grid) }
    val mainBranch = grid.filter { it.x == 0 && it.y == 0 && it.z != 0 }.distinct()
    return runBlocking(Dispatchers.Default) {
        mainBranch.map { candidate ->
            async {
                bfsToAll(candidate, leaves, neighbourhoods)
            }
        }.awaitAll()
    }.min()
}


private fun buildBranches(input: List<String>): List<List<Point3D>> =
    input.map { string ->
        string.split(',').runningFold(listOf(Point3D(0, 0, 0))) { previousRun, op ->
            val last = previousRun.last()
            when (op[0]) {
                'U' -> (1..op.toIntFromIndex(1)).map { last.copy(z = last.z + it) }
                'D' -> (1..op.toIntFromIndex(1)).map { last.copy(z = last.z - it) }
                'L' -> (1..op.toIntFromIndex(1)).map { last.copy(x = last.x - it) }
                'R' -> (1..op.toIntFromIndex(1)).map { last.copy(x = last.x + it) }
                'F' -> (1..op.toIntFromIndex(1)).map { last.copy(y = last.y + it) }
                'B' -> (1..op.toIntFromIndex(1)).map { last.copy(y = last.y - it) }
                else -> throw Error()
            }
        }.drop(1).flatten()
    }

private fun neighboursOfPoint(center: Point3D, grid: Set<Point3D>): Collection<Point3D> = buildList(6) {
    center.copy(z = center.z - 1).also { if (it in grid) add(it) }
    center.copy(z = center.z + 1).also { if (it in grid) add(it) }
    center.copy(x = center.x - 1).also { if (it in grid) add(it) }
    center.copy(x = center.x + 1).also { if (it in grid) add(it) }
    center.copy(y = center.y - 1).also { if (it in grid) add(it) }
    center.copy(y = center.y + 1).also { if (it in grid) add(it) }
}

private fun bfsToAll(start: Point3D, ends: Set<Point3D>, neighboursOf: Map<Point3D, Collection<Point3D>>): Int {
    val work = ArrayDeque(listOf(start to 0))
    val visited = newHashSet<Point3D>(neighboursOf.size).apply { add(start) }
    val endsRemaining = ends.toMutableSet()
    var endDistances = 0

    while (true) {
        val (point, distance) = work.removeFirst()
        if (point in endsRemaining) {
            endDistances += distance
            endsRemaining.remove(point)
            if (endsRemaining.isEmpty()) return endDistances
        }
        for (n in neighboursOf[point]!!) {
            if (n !in visited) {
                visited.add(n)
                work.add(n to distance + 1)
            }
        }
    }
}
