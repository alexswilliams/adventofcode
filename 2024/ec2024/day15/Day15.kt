package ec2024.day15

import common.*
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

        check(0, "P3 Puzzle") { part3(puzzles[2]) }
    }
}

private data class Place(val r: Int, val c: Int)

private data class Destination(val end: Place, val distance: Int)

private fun part1(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
    val start = Place(r = 0, c = grid[0].indexOf('.'))
    val herbs = grid.flatMapIndexed { row, line -> line.mapIndexed { col, ch -> if (ch == 'H') Place(row, col) else null }.filterNotNull() }
    return 2 * aStarSearch(herbs.map { it to 0 }, start, grid)
}


private fun part2(input: List<String>): Int {
    val grid = input.asArrayOfCharArrays()
    val start = Place(r = 0, c = grid[0].indexOf('.'))
    val herbs = grid.flatMapIndexed { row, line -> line.mapIndexed { col, ch -> if (ch.isLetter()) Place(row, col) to ch else null }.filterNotNull() }
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


private fun part3(input: List<String>): Int = throw Error("Need more coffee")


private fun aStarSearch(starts: Collection<Pair<Place, Int>>, end: Place, grid: Array<CharArray>, heuristic: (Place) -> Int = { manhattan(it, end) }): Int {
    val heap = TreeQueue<Place>(heuristic)
    val shortestPath = Array(grid.size) { row -> IntArray(grid[0].size) { Int.MAX_VALUE } }
    starts.forEach { (start, distanceSoFar) ->
        shortestPath[start.r][start.c] = distanceSoFar
        heap.offer(start, weight = distanceSoFar)
    }
    while (true) {
        val u = heap.poll() ?: throw Error("All routes explored with no solution")
        val distanceToU = shortestPath[u.r][u.c]
        if (u == end) return distanceToU
        for (n in neighboursOf(u, grid)) {
            val originalDistance = shortestPath[n.r][n.c]
            val newDistance = distanceToU + 1
            if (newDistance < originalDistance) {
                heap.offerOrReposition(n, originalDistance, newDistance)
                shortestPath[n.r][n.c] = newDistance
            }
        }
    }
}

private fun manhattan(t1: Place, t2: Place) = (t1.r - t2.r).absoluteValue + (t1.c - t2.c).absoluteValue

private val impassable = charArrayOf('#', '~')
private fun neighboursOf(center: Place, grid: Array<CharArray>) = buildList(4) {
    if (center.c > 0 && grid[center.r][center.c - 1] !in impassable) add(center.copy(c = center.c - 1))
    if (center.c < grid[0].lastIndex && grid[center.r][center.c + 1] !in impassable) add(center.copy(c = center.c + 1))
    if (center.r > 0 && grid[center.r - 1][center.c] !in impassable) add(center.copy(r = center.r - 1))
    if (center.r < grid.lastIndex && grid[center.r + 1][center.c] !in impassable) add(center.copy(r = center.r + 1))
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
