package ec2024.day17

import common.*
import kotlinx.collections.immutable.*
import kotlin.math.*

private val examples = loadFilesToGrids("ec2024/day17", "example1.txt", "example3.txt")
private val puzzles = loadFilesToGrids("ec2024/day17", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day17.assertCorrect()
    benchmark { part1(puzzles[0]) } // 106µs
    benchmark { part2(puzzles[1]) } // 7.9ms
    benchmark(10) { part3(puzzles[2]) } // 745ms
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
    val edges = distancesBetweenStars(stars).sortedBy { (_, _, distance) -> distance }
    val closeEdges = edges.filter { it.third < 6 }

    val forest = mutableListOf<Pair<Int, PersistentSet<Location1616>>>()
    for (edge in closeEdges) {
        val firstTreeIndex = forest.indexOfFirst { tree -> edge.first in tree.second }
        val secondTreeIndex = forest.indexOfFirst { tree -> edge.second in tree.second }
        if (firstTreeIndex == -1 && secondTreeIndex == -1) {
            // new tree in the forest
            forest.add(edge.third to persistentSetOf(edge.first, edge.second))
        } else if (firstTreeIndex == -1 && secondTreeIndex >= 0) {
            // first star is new, but second star has already been seen
            val secondTree = forest[secondTreeIndex]
            if (secondTree.second.any { star -> manhattan(star, edge.first) < 6 })
                forest[secondTreeIndex] = secondTree.first + edge.third to secondTree.second.plus(edge.first)
            else continue // edge would have brought in a star that expanded the constellation beyond a size of 6
        } else if (firstTreeIndex >= 0 && secondTreeIndex == -1) {
            // second star is new, but first star has already been seen
            val firstTree = forest[firstTreeIndex]
            if (firstTree.second.any { star -> manhattan(star, edge.second) < 6 })
                forest[firstTreeIndex] = firstTree.first + edge.third to firstTree.second.plus(edge.second)
            else continue // edge would have brought in a star that expanded the constellation beyond a size of 6
        } else if (firstTreeIndex != secondTreeIndex) {
            // this edge joins two trees together
            val firstTree = forest[firstTreeIndex]
            val secondTree = forest[secondTreeIndex]
            if (firstTree.second.any { star1 -> secondTree.second.any { star2 -> manhattan(star1, star2) < 6 } }) {
                // only merge the two forests if all stars in both are all within the same 6-wide radius
                forest[firstTreeIndex] = firstTree.first + secondTree.first + edge.third to firstTree.second.plus(secondTree.second)
                forest.removeAt(secondTreeIndex)
            } else continue
        } else continue // this edge would form a loop
    }
    return forest.map { (dist, locs) -> dist.toLong() + locs.size }.sortedDescending().take(3).product()
}

private fun manhattan(from: Location1616, to: Location1616): Int = (from.row() - to.row()).absoluteValue + (from.col() - to.col()).absoluteValue

tailrec fun distancesBetweenStars(
    stars: List<Location1616>,
    soFar: MutableList<Triple<Location1616, Location1616, Int>> = arrayListOf(),
): List<Triple<Location1616, Location1616, Int>> {
    if (stars.isEmpty()) return soFar
    val head = stars.first()
    val tail = stars.tail()
    tail.forEach { dest -> soFar.add(Triple(head, dest, manhattan(head, dest))) }
    return distancesBetweenStars(tail, soFar)
}
