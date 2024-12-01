package ec2024.day15

import common.*
import java.util.HashMap.*
import kotlin.math.*

private val examples = loadFilesToLines("ec2024/day15", "example.txt", "example2.txt")
private val puzzles = loadFilesToLines("ec2024/day15", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day15.assertCorrect()
    benchmark { part1(puzzles[0]) } // 77µs
    benchmark(100) { part2(puzzles[1]) } // 25.5ms
    benchmark(100) { part3(puzzles[2]) } // 86.2ms
}

internal object Day15 : Challenge {
    override fun assertCorrect() {
        check(26, "P1 Example") { part1(examples[0]) }
        check(188, "P1 Puzzle") { part1(puzzles[0]) }

        check(38, "P2 Example") { part2(examples[1]) }
        check(526, "P2 Puzzle") { part2(puzzles[1]) }

        check(1530, "P3 Puzzle") { part3(puzzles[2]) }
    }
}

private fun part1(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
    val start = 0 by16 grid[0].indexOf('.')
    val herbs = grid.mapCartesianNotNull { row, col, char -> if (char == 'H') row by16 col else null }
    return 2 * aStarSearch(herbs.map { it to 0 }, start, grid)
}


private fun part2(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
    val start = 0 by16 grid[0].indexOf('.')
    fillDeadEnds(grid)
    return shortestLengthViaAllHerbs(grid, start)
}

private fun part3(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
    val start = 0 by16 grid[0].indexOf('.')
    fillDeadEnds(grid)

    val barriers = grid[0].indices.filter { col -> grid.count { it[col] == '#' } == grid.size - 1 && grid.any { it[col] == '.' } }
    if (barriers.isEmpty()) return shortestLengthViaAllHerbs(grid, start)

    if (barriers.size != 2 || start.col() !in barriers[0]..barriers[1]) throw Error("Solution needs to be made more generic")
    val grids = listOf(
        Array(grid.size) { row -> grid[row].copyOfRange(0, barriers[0] + 1) },
        Array(grid.size) { row -> grid[row].copyOfRange(barriers[0], barriers[1] + 1) },
        Array(grid.size) { row -> grid[row].copyOfRange(barriers[1], grid[0].lastIndex + 1) },
    )
    val startLeft = (grids[0].indexOfLast { it.last() == '.' }) by16 grids[0][0].lastIndex
    val startMiddle = 0 by16 grids[1][0].indexOf('.')
    val startRight = (grids[2].indexOfLast { it.first() == '.' }) by16 0
    grids[1][startLeft.row()][0] = 'X'
    grids[1][startRight.row()][grids[1][0].lastIndex] = 'Y'

    return shortestLengthViaAllHerbs(grids[0], startLeft) +
            shortestLengthViaAllHerbs(grids[1], startMiddle) +
            shortestLengthViaAllHerbs(grids[2], startRight)
}

private fun shortestLengthViaAllHerbs(grid: Array<CharArray>, start: Location1616): Int {
    val herbs = grid.mapCartesianNotNull { row, col, char -> if (char.isLetter()) row by16 col to char else null }
        .groupBy({ it.second }) { it.first }

    val startToEachHerb = herbs.values.flatMap { targets ->
        targets.map { target -> target to aStarCollectingHerbs(start, target, grid).let { (dist, seen) -> dist to (seen.map { it.lettersToBitSet() }) } }
    }.toMap()

    val startConditions = herbs.values.sortedBy { it.size }.flatMap { places ->
        places.flatMap { herbLocation ->
            val (distanceFromStartToHerb, herbsSeenBetweenStartAndHerb) = startToEachHerb[herbLocation]!!
            herbsSeenBetweenStartAndHerb.map { seenSoFar -> Triple(herbLocation, seenSoFar, distanceFromStartToHerb) }
        }
    }
    return bfsToOtherReachableHerbs(startConditions, startToEachHerb, herbs.keys.lettersToBitSet(), grid)
}

private fun Iterable<Char>.lettersToBitSet() = this.map { 1L shl (it - 'A') }.asBitSet()
private fun String.lettersToBitSet() = this.asIterable().lettersToBitSet()

private fun pack(location: Location1616, seen: BitSet): Long = (location.toLong() shl 32) or seen
private fun unpackSeen(l: Long): BitSet = l and 0xffff_ffff
private fun unpackLocation(l: Long): Location1616 = (l shr 32).toInt()

private fun bfsToOtherReachableHerbs(
    starts: List<Triple<Location1616, BitSet, Int>>,
    pathsBackHomeFromEachHerb: Map<Location1616, Pair<Int, List<BitSet>>>,
    herbsNeeded: BitSet,
    grid: Array<CharArray>,
): Int {
    val height = grid.size
    val width = grid[0].size
    val newPlane = { Array(height) { BooleanArray(width) } }
    val visitedGrids = newHashMap<Long, Array<BooleanArray>>(90)
        .apply { starts.forEach { (start, seen) -> getOrPut(seen, newPlane)[start.row()][start.col()] = true } }
    val work = TreeQueue<Long>()
        .apply { starts.forEach { offer(pack(it.first, it.second), weight = it.third) } }
    var shortestRouteLength: Int = Int.MAX_VALUE
    val neighboursArray = IntArray(4)

    while (true) {
        val distance: Int
        val node = work.poll { weight -> distance = weight } ?: break
        val u = unpackLocation(node)
        val seenSoFar = unpackSeen(node)
        if (distance >= shortestRouteLength) continue
        val visited = visitedGrids.getOrPut(seenSoFar, newPlane)

        for (n in neighboursOf(u, grid, impassable, neighboursArray)) {
            if (n == -1) continue
            val floorType = grid[n.row()][n.col()]

            fun maybeAddBlankTile() {
                if (!visited[n.row()][n.col()]) {
                    visited[n.row()][n.col()] = true
                    work.offer(pack(n, seenSoFar), distance + 1)
                }
            }

            fun maybeAddLetterTile() {
                val newSeenSoFar = seenSoFar plusItem (1L shl (floorType - 'A'))
                val newVisited = if (newSeenSoFar != seenSoFar) visitedGrids.getOrPut(newSeenSoFar, newPlane) else visited
                if (!newVisited[n.row()][n.col()]) {
                    newVisited[n.row()][n.col()] = true
                    work.offer(pack(n, newSeenSoFar), distance + 1)

                    val (distanceBackHome, pathsBackHome) = pathsBackHomeFromEachHerb[n]!!
                    if (pathsBackHome.any { pathBackHome -> herbsNeeded.excluding(pathBackHome plusItem newSeenSoFar) == EMPTY_BITSET }) {
                        if (distance + distanceBackHome < shortestRouteLength) shortestRouteLength = distance + distanceBackHome + 1
                        return
                    }
                }
            }

            if (floorType.isLetter()) maybeAddLetterTile() else maybeAddBlankTile()
        }
    }
    return shortestRouteLength
}

private fun aStarCollectingHerbs(
    start: Location1616,
    end: Location1616,
    grid: Array<CharArray>,
    heuristic: (Location1616) -> Int = { manhattan(it, end) },
): Pair<Int, List<String>> {
    val heap = TreeQueue(heuristic)
    val shortestPaths = Array(grid.size) { Array(grid[0].size) { Int.MAX_VALUE to listOf<String>() } }
    val offered = mutableSetOf<Location1616>()
    shortestPaths[start.row()][start.col()] = 0 to listOf()
    heap.offer(start, weight = 0)
    val neighbourArray = IntArray(4)

    while (true) {
        val u = heap.poll() ?: throw Error("All routes explored with no solution")
        val (distanceToU, pathsToU) = shortestPaths[u.row()][u.col()]
        if (u == end) return distanceToU to pathsToU

        for (n in neighboursOf(u, grid, impassable, neighbourArray)) {
            if (n == -1) continue
            val (oldDistanceToN, oldPathsToN) = shortestPaths[n.row()][n.col()]
            val newDistanceToN = distanceToU + 1
            val floorType = grid[n.row()][n.col()]
            if (newDistanceToN < oldDistanceToN) {
                heap.offerOrReposition(n, oldDistanceToN, newDistanceToN)
                offered.add(n)
                if (floorType.isLetter()) shortestPaths[n.row()][n.col()] = newDistanceToN to pathsToU
                    .map { if (floorType !in it) it + floorType else it }
                    .let { it.ifEmpty { listOf(floorType.toString()) } }
                    .distinct()
                else shortestPaths[n.row()][n.col()] = newDistanceToN to pathsToU
            }
            if (newDistanceToN == oldDistanceToN) {
                if (n !in offered) {
                    heap.offerOrReposition(n, oldDistanceToN, newDistanceToN)
                    offered.add(n)
                }
                shortestPaths[n.row()][n.col()] =
                    if (floorType.isLetter()) newDistanceToN to pathsToU
                        .map { if (floorType !in it) it + floorType else it }
                        .let { it.ifEmpty { listOf(floorType.toString()) } }
                        .plus(oldPathsToN).distinct()
                    else newDistanceToN to pathsToU.plus(oldPathsToN).distinct()
            }
        }
    }
}


private fun aStarSearch(
    starts: Collection<Pair<Location1616, Int>>,
    end: Location1616,
    grid: Array<CharArray>,
    heuristic: (Location1616) -> Int = { manhattan(it, end) },
): Int {
    val heap = TreeQueue(heuristic)
    val shortestPath = Array(grid.size) { IntArray(grid[0].size) { Int.MAX_VALUE } }
    val neighboursArray = IntArray(4)
    starts.forEach { (start, distanceSoFar) ->
        shortestPath[start.row()][start.col()] = distanceSoFar
        heap.offer(start, weight = distanceSoFar)
    }
    while (true) {
        val u = heap.poll() ?: throw Error("All routes explored with no solution")
        val distanceToU = shortestPath[u.row()][u.col()]
        if (u == end) return distanceToU
        for (n in neighboursOf(u, grid, impassable, neighboursArray)) {
            if (n == -1) continue
            val originalDistance = shortestPath[n.row()][n.col()]
            val newDistance = distanceToU + 1
            if (newDistance < originalDistance) {
                heap.offerOrReposition(n, originalDistance, newDistance)
                shortestPath[n.row()][n.col()] = newDistance
            }
        }
    }
}

private fun manhattan(t1: Location1616, t2: Location1616) = (t1.row() - t2.row()).absoluteValue + (t1.col() - t2.col()).absoluteValue

private val impassable = charArrayOf('#', '~')
