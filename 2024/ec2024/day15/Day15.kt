package ec2024.day15

import common.*
import kotlinx.coroutines.*
import kotlin.math.*

private val examples = loadFilesToLines("ec2024/day15", "example.txt", "example2.txt")
private val puzzles = loadFilesToLines("ec2024/day15", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day15.assertCorrect()
    benchmark { part1(puzzles[0]) } // 77µs
    benchmark(10) { part2(puzzles[1]) } // 234ms
    benchmark(1) { part3(puzzles[2]) }
}

internal object Day15 : Challenge {
    override fun assertCorrect() {
        check(26, "P1 Example") { part1(examples[0]) }
        check(188, "P1 Puzzle") { part1(puzzles[0]) }

        check(38, "P2 Example") { part2(examples[1]) }
        check(526, "P2 Puzzle") { part2(puzzles[1]) }

        check(38, "P3 Example from part 2") { part3(examples[1]) }
        check(526, "P3 using Input from part 2") { part3(puzzles[1]) }
        check(1530, "P3 Puzzle") { part3(puzzles[2]) }
    }
}

private data class Destination(val end: Location1616, val distance: Int)

private fun part1(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
    val start = 0 by16 grid[0].indexOf('.')
    val herbs = grid.flatMapIndexed { row, line -> line.mapIndexed { col, ch -> if (ch == 'H') row by16 col else null }.filterNotNull() }
    return 2 * aStarSearch(herbs.map { it to 0 }, start, grid)
}


private fun part2(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
    val start = 0 by16 grid[0].indexOf('.')
    val herbs = grid.flatMapIndexed { row, line -> line.mapIndexed { col, ch -> if (ch.isLetter()) row by16 col to ch else null }.filterNotNull() }
        .groupBy({ it.second }) { it.first }

    val possibleOrderings = herbs.keys.permutations().let { strings -> strings.filter { it.reversed() !in strings || it <= it.reversed() } }

    val routePrefixCache = mutableMapOf<String, List<Destination>>()
    fun shortestPathForOrdering(ordering: String): Int {
        var distancesSoFar = routePrefixCache.getOrPut(ordering.take(1)) {
            herbs[ordering[0]]!!.map { herb ->
                Destination(herb, aStarSearch(listOf(start to 0), herb, grid))
            }
        }
        (1..ordering.lastIndex).forEach { i ->
            distancesSoFar = routePrefixCache.getOrPut(ordering.take(i + 1)) {
                herbs[ordering[i]]!!.map { herb ->
                    Destination(herb, aStarSearch(distancesSoFar.map { it.end to it.distance }, herb, grid))
                }
            }
        }
        return aStarSearch(distancesSoFar.map { it.end to it.distance }, start, grid)
    }

    val shortestPathPerOrder = possibleOrderings.map { ordering -> shortestPathForOrdering(ordering) }
    return shortestPathPerOrder.min()
}

private fun Iterable<Char>.lettersToBitSet() = this.map { 1L shl (it - 'A') }.asBitSet()
private fun String.lettersToBitSet() = this.asIterable().lettersToBitSet()

private fun part3(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
    val start = 0 by16 grid[0].indexOf('.')
    val herbs = grid.flatMapIndexed { row, line -> line.mapIndexed { col, ch -> if (ch.isLetter()) row by16 col to ch else null }.filterNotNull() }
        .groupBy({ it.second }) { it.first }

    val startToEachHerb = herbs.flatMap { (_, targets) ->
        targets.map { it to aStarCollectingHerbs(start, it, grid).let { (dist, seen) -> dist to (seen.map { it.lettersToBitSet() }) } }
    }.toMap()
    // manually verified: there are no herbs where the shortest path from the start accidentally also visits all other herbs
    println(startToEachHerb)
    println(herbs.keys)
    val reachableFromEachHerb = herbs.values.fold(Int.MAX_VALUE) { bestSoFar, places ->
        runBlocking(Dispatchers.Default) {
            places.mapIndexed { index, herb ->
                async {
                    val (distanceFromStartToHerb, herbsSeenBetweenStartAndHerb) = startToEachHerb[herb]!!
                    println("[${index + 1} of ${places.size}] Trying ${grid[herb.row()][herb.col()]} $herb (from Start = $distanceFromStartToHerb, $herbsSeenBetweenStartAndHerb) - best so far is $bestSoFar")
                    herbsSeenBetweenStartAndHerb.minOf { seenSoFar ->
                        bfsToOtherReachableHerbs(
                            start = herb,
                            distanceFromStart = distanceFromStartToHerb,
                            pathsBackHomeFromEachHerb = startToEachHerb,
                            seenBeforeStart = seenSoFar,
                            herbsStillNeeded = herbs.keys.lettersToBitSet().excluding(seenSoFar),
                            grid = grid,
                            bestSoFar = bestSoFar
                        )
                    }
                }
            }.awaitAll()
        }.min()
    }
    return reachableFromEachHerb
}


private fun bfsToOtherReachableHerbs(
    start: Location1616,
    distanceFromStart: Int,
    pathsBackHomeFromEachHerb: Map<Location1616, Pair<Int, List<BitSet>>>,
    seenBeforeStart: BitSet,
    herbsStillNeeded: BitSet,
    grid: Array<CharArray>,
    bestSoFar: Int,
): Int {
    val work = ArrayDeque(listOf(Triple(start, seenBeforeStart, distanceFromStart)))
    val visitedGrids = mutableMapOf(seenBeforeStart to Array(grid.size) { BooleanArray(grid[0].size) })
        .apply { get(seenBeforeStart)!![start.row()][start.col()] = true }
    var shortestRouteLength: Int = bestSoFar
    val neighboursArray = IntArray(4)

    while (work.isNotEmpty()) {
        val (u, seenSoFar, distance) = work.removeFirst()
        if (distance >= shortestRouteLength) continue
        val plane = visitedGrids.getOrPut(seenSoFar) { Array(grid.size) { BooleanArray(grid[0].size) } }

        for (n in neighboursOf(u, grid, neighboursArray)) {
            if (n == -1) continue
            val floorType = grid[n.row()][n.col()]
            if (floorType.isLetter()) {
                val newSeenSoFar = seenSoFar plusItem (1L shl (floorType - 'A'))
                val newPlane = if (newSeenSoFar != seenSoFar) visitedGrids.getOrPut(newSeenSoFar) { Array(grid.size) { BooleanArray(grid[0].size) } } else plane
                if (!newPlane[n.row()][n.col()]) {
                    newPlane[n.row()][n.col()] = true
                    work.addLast(Triple(n, newSeenSoFar, distance + 1))

                    val (distanceBackHome, pathsBackHome) = pathsBackHomeFromEachHerb[n]!!
                    if (pathsBackHome.any { pathBackHome ->
                            val discoveredInPath = pathBackHome plusItem newSeenSoFar plusItem seenBeforeStart
                            val difference = herbsStillNeeded.excluding(discoveredInPath)
                            difference == EMPTY_BITSET
                        }) {
                        if (distance + distanceBackHome < shortestRouteLength) {
                            shortestRouteLength = distance + distanceBackHome + 1
                        }
                        continue
                    }
                }
            } else {
                if (!plane[n.row()][n.col()]) {
                    plane[n.row()][n.col()] = true
                    work.addLast(Triple(n, seenSoFar, distance + 1))
                }
            }
        }
    }
    return shortestRouteLength.also { if (it == Int.MAX_VALUE) throw Error("Could not find a complete route - should be impossible") }
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

        for (n in neighboursOf(u, grid, neighbourArray)) {
            if (n == -1) continue
            val (oldDistanceToN, oldPathsToN) = shortestPaths[n.row()][n.col()]
            val newDistanceToN = distanceToU + 1
            val floorType = grid[n.row()][n.col()]
            if (newDistanceToN < oldDistanceToN) {
                heap.offerOrReposition(n, oldDistanceToN, newDistanceToN)
                offered.add(n)
                if (floorType.isLetter()) shortestPaths[n.row()][n.col()] = newDistanceToN to pathsToU
                    .map { if (floorType !in it) it + floorType else it }
                    .let { if (it.isEmpty()) listOf(floorType.toString()) else it }
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
                        .let { if (it.isEmpty()) listOf(floorType.toString()) else it }
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
    val heap = TreeQueue<Location1616>(heuristic)
    val shortestPath = Array(grid.size) { row -> IntArray(grid[0].size) { Int.MAX_VALUE } }
    starts.forEach { (start, distanceSoFar) ->
        shortestPath[start.row()][start.col()] = distanceSoFar
        heap.offer(start, weight = distanceSoFar)
    }
    while (true) {
        val u = heap.poll() ?: throw Error("All routes explored with no solution")
        val distanceToU = shortestPath[u.row()][u.col()]
        if (u == end) return distanceToU
        for (n in neighboursOf(u, grid)) {
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
private fun neighboursOf(center: Location1616, grid: Array<CharArray>) = buildList(4) {
    if (center.col() > 0 && grid[center.row()][center.col() - 1] !in impassable) add(center.minusCol())
    if (center.col() < grid[0].lastIndex && grid[center.row()][center.col() + 1] !in impassable) add(center.plusCol())
    if (center.row() > 0 && grid[center.row() - 1][center.col()] !in impassable) add(center.minusRow())
    if (center.row() < grid.lastIndex && grid[center.row() + 1][center.col()] !in impassable) add(center.plusRow())
}

private fun neighboursOf(center: Location1616, grid: Array<CharArray>, array: IntArray) = array.apply {
    this[0] = if (center.col() > 0 && grid[center.row()][center.col() - 1] !in impassable) center.minusCol() else -1
    this[1] = if (center.col() < grid[0].lastIndex && grid[center.row()][center.col() + 1] !in impassable) center.plusCol() else -1
    this[2] = if (center.row() > 0 && grid[center.row() - 1][center.col()] !in impassable) center.minusRow() else -1
    this[3] = if (center.row() < grid.lastIndex && grid[center.row() + 1][center.col()] !in impassable) center.plusRow() else -1
}

private fun Set<Char>.permutations(prefix: List<Char> = listOf()): List<String> =
    if (isEmpty()) listOf(prefix.joinToString("")) else flatMap { (this - it).permutations(prefix + it) }

private fun orderingsOf(choices: String): List<String> {
    if (choices.length <= 1) return listOf(choices)
    return choices.flatMapIndexed { index, thisLetter ->
        val allSuffixes = orderingsOf(choices.removeRange(index, index + 1))
        allSuffixes.map { string -> thisLetter + string }
    }
}
