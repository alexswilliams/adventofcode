package ec2024.day17

import common.*
import kotlin.math.*

private val examples = loadFilesToGrids("ec2024/day17", "example1.txt", "example3.txt")
private val puzzles = loadFilesToGrids("ec2024/day17", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day17.assertCorrect()
    benchmark { part1(puzzles[0]) } // 106µs
    benchmark(100) { part2(puzzles[1]) } // 7.3ms
    benchmark(10) { part3(puzzles[2]) } // 89.0ms
}

internal object Day17 : Challenge {
    override fun assertCorrect() {
        check(16, "P1 Example") { part1(examples[0]) }
        check(133, "P1 Puzzle") { part1(puzzles[0]) }

        check(1278, "P2 Puzzle") { part2(puzzles[1]) }

        check(15624, "P3 Example") { part3(examples[1]) }
        check(3451133634L, "P3 Puzzle") { part3(puzzles[2]) }
    }
}

private fun part1(input: Grid): Int {
    val stars = input.mapCartesianNotNull { row, col, char -> if (char == '*') row by16 col else null }
    val edges = distancesBetweenStars(stars).sortedBy { (_, _, distance) -> distance }

    val forest = mutableListOf<MutableSet<Location1616>>()
    var totalWeight = 0
    for (edge in edges) {
        val firstTreeIndex = forest.indexOfFirst { tree -> edge.first in tree }
        val secondTreeIndex = forest.indexOfFirst { tree -> edge.second in tree }
        if (firstTreeIndex == -1 && secondTreeIndex == -1) {
            // new tree in the forest
            forest.add(mutableSetOf(edge.first, edge.second))
        } else if (firstTreeIndex == -1 && secondTreeIndex >= 0) {
            // first star is new, but second star has already been seen
            forest[secondTreeIndex].add(edge.first)
        } else if (firstTreeIndex >= 0 && secondTreeIndex == -1) {
            // second star is new, but first star has already been seen
            forest[firstTreeIndex].add(edge.second)
        } else if (firstTreeIndex != secondTreeIndex) {
            // this edge joins two trees together
            val secondTree = forest[secondTreeIndex]
            forest[firstTreeIndex].addAll(secondTree)
            forest.removeAt(secondTreeIndex)
        } else continue // this edge would form a loop

        totalWeight += edge.third
        if (forest.size == 1 && forest[0].size == stars.size) break
    }
    return totalWeight + stars.size
}

private fun part2(input: Grid): Int = part1(input)

private fun part3(input: Grid): Long {
    val stars = input.mapCartesianNotNull { row, col, char -> if (char == '*') row by16 col else null }
    val edges = distancesBetweenStars(stars, lengthLimit = 6)
    val closeEdges = edges.sortedBy { (_, _, distance) -> distance }

    data class SpanningTree(var weight: Int, val nodes: MutableSet<Location1616>)

    val forest = mutableListOf<SpanningTree>()
    for (edge in closeEdges) {
        val firstTreeIndex = forest.indexOfFirst { tree -> edge.first in tree.nodes }
        val secondTreeIndex = forest.indexOfFirst { tree -> edge.second in tree.nodes }
        if (firstTreeIndex == -1 && secondTreeIndex == -1) {
            // new tree in the forest
            forest.add(SpanningTree(edge.third, mutableSetOf(edge.first, edge.second)))
        } else if (firstTreeIndex == -1 && secondTreeIndex >= 0) {
            // first star is new, but second star has already been seen
            val secondTree = forest[secondTreeIndex]
            if (secondTree.nodes.any { star -> manhattan(star, edge.first) < 6 }) {
                secondTree.weight += edge.third
                secondTree.nodes.add(edge.first)
            } else continue // edge would have brought in a star that expanded the constellation beyond a size of 6
        } else if (firstTreeIndex >= 0 && secondTreeIndex == -1) {
            // second star is new, but first star has already been seen
            val firstTree = forest[firstTreeIndex]
            if (firstTree.nodes.any { star -> manhattan(star, edge.second) < 6 }) {
                firstTree.weight += edge.third
                firstTree.nodes.add(edge.second)
            } else continue // edge would have brought in a star that expanded the constellation beyond a size of 6
        } else if (firstTreeIndex != secondTreeIndex) {
            // this edge joins two trees together
            val firstTree = forest[firstTreeIndex]
            val secondTree = forest[secondTreeIndex]
            if (firstTree.nodes.any { star1 -> secondTree.nodes.any { star2 -> manhattan(star1, star2) < 6 } }) {
                // only merge the two forests if at least one star in each falls within 6 units
                firstTree.weight += secondTree.weight + edge.third
                firstTree.nodes.addAll(secondTree.nodes)
                forest.removeAt(secondTreeIndex)
            } else continue
        } else continue // this edge would form a loop
    }
    return forest.map { it.weight.toLong() + it.nodes.size }.sortedDescending().take(3).product()
}

private fun manhattan(from: Location1616, to: Location1616): Int = (from.row() - to.row()).absoluteValue + (from.col() - to.col()).absoluteValue

private tailrec fun distancesBetweenStars(
    stars: List<Location1616>,
    startAt: Int = 0,
    soFar: MutableList<Triple<Location1616, Location1616, Int>> = arrayListOf(),
    lengthLimit: Int = Int.MAX_VALUE,
): List<Triple<Location1616, Location1616, Int>> {
    if (startAt > stars.lastIndex) return soFar
    val head = stars[startAt]
    (startAt..stars.lastIndex).forEach { i ->
        val dest = stars[i]
        val dist = manhattan(head, dest)
        if (dist < lengthLimit)
            soFar.add(Triple(head, dest, dist))
    }
    return distancesBetweenStars(stars, startAt + 1, soFar, lengthLimit)
}
