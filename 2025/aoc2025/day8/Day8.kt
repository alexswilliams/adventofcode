package aoc2025.day8

import common.*
import java.util.*
import java.util.Comparator.*

private val example = loadFilesToLines("aoc2025/day8", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day8", "input.txt").single()

internal fun main() {
    Day8.assertCorrect()
    benchmark(100) { part1(puzzle) } // 20.1ms
    benchmark(100) { part2(puzzle) } // 23.2ms
}

internal object Day8 : Challenge {
    override fun assertCorrect() {
        check(40, "P1 Example") { part1(example, rounds = 10) }
        check(105952, "P1 Puzzle") { part1(puzzle) }

        check(25272, "P2 Example") { part2(example) }
        check(975931446, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>, rounds: Int = 1000): Int {
    val (_, circuits, sortedDistances) = parseInput(input)
    sortedDistances.take(rounds).forEach { (a, b) -> circuits.merge(a, b) }
    return circuits.map { it.size }
        .sortedDescending()
        .take(3)
        .product()
}

private fun part2(input: List<String>): Long {
    val (boxes, circuits, sortedDistances) = parseInput(input)
    sortedDistances.forEach { (a, b, _) ->
        circuits.merge(a, b)
        if (circuits.size == 1) return 1L * boxes[a].x * boxes[b].x
    }
    throw Exception("Mega-clique was never reached")
}


private fun parseInput(input: List<String>): Triple<List<JunctionBox>, MutableList<Circuit>, Iterable<PairwiseDistance>> {
    val boxes = input.map { it.splitToInts(",").let { (x, y, z) -> JunctionBox(x, y, z) } }
    val circuits = boxes.indices.mapTo(ArrayList()) { arrayListOf(it) }
    val sortedDistances = sortedDistancesBetween(boxes)
    return Triple(boxes, circuits, sortedDistances)
}


private typealias Circuit = ArrayList<Int>

private fun MutableList<Circuit>.merge(a: Int, b: Int) {
    val circuitA = indexOfFirst { it.contains(a) }
    val circuitB = indexOfFirst { it.contains(b) }
    if (circuitA != circuitB) {
        this[circuitA].addAll(this[circuitB])
        removeAt(circuitB)
    }
}


private data class JunctionBox(val x: Int, val y: Int, val z: Int) {
    infix fun distanceTo(other: JunctionBox): Long =
        (1L * (x - other.x) * (x - other.x) +
                1L * (y - other.y) * (y - other.y) +
                1L * (z - other.z) * (z - other.z))
}

private data class PairwiseDistance(val a: Int, val b: Int, val distance: Long)

private fun sortedDistancesBetween(boxes: List<JunctionBox>): Iterable<PairwiseDistance> =
    ArrayList<PairwiseDistance>((boxes.lastIndex * boxes.size) / 2).apply {
        boxes.mapPairwiseIndexed { idx1, idx2, box1, box2 ->
            add(PairwiseDistance(idx1, idx2, box1 distanceTo box2))
        }
    }.toTypedArray().apply {
        // Sorting these distances is by far the slowest part of solving this problem - thankfully, parallel sort exists.
        Arrays.parallelSort(this, comparingLong(PairwiseDistance::distance))
    }.asIterable()
