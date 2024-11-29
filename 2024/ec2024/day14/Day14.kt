package ec2024.day14

import common.*
import kotlinx.coroutines.*
import java.util.HashSet.*

private val examples = loadFilesToLines("ec2024/day14", "example.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day14", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day14.assertCorrect()
    benchmark { part1(puzzles[0]) } // 21µs
    benchmark { part2(puzzles[1]) } // 1.3ms
    benchmark(100) { part3(puzzles[2]) } // 12.6ms
}

internal object Day14 : Challenge {
    override fun assertCorrect() {
        check(7, "P1 Example") { part1(examples[0]) }
        check(140, "P1 Puzzle") { part1(puzzles[0]) }

        check(32, "P2 Example") { part2(examples[1]) }
        check(5172, "P2 Puzzle") { part2(puzzles[1]) }

        check(5, "P3 Example 1") { part3(examples[1]) }
        check(46, "P3 Example 2") { part3(examples[2]) }
        check(1494, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private data class Point3D(val x: Int, val y: Int, val z: Int) {
    override fun equals(other: Any?): Boolean = (other as? Point3D)?.let { x == it.x && y == it.y && z == it.z } == true
    override fun hashCode(): Int = (x * 31 + y) * 31 + z
    override fun toString(): String = "($x, $y, $z)"
}


private fun part1(input: List<String>): Int =
    input.first().split(',').runningFold(0) { currentHeight, string ->
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
    val neighbourhoods = grid.associateWith { point -> neighboursOfPoint(point, grid) }
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
