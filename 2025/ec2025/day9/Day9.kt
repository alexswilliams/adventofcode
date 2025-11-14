package ec2025.day9

import common.*
import java.lang.Integer.min

private val examples = loadFilesToLines("ec2025/day9", "example1.txt", "example2.txt", "example3a.txt", "example3b.txt")
private val puzzles = loadFilesToLines("ec2025/day9", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day9.assertCorrect()
    benchmark { part1(puzzles[0]) } // 22.9µs
    benchmark(100) { part2(puzzles[1]) } // 13.3ms
    benchmark(1) { part3(puzzles[2]) } // 1.8s
}

internal object Day9 : Challenge {
    override fun assertCorrect() {
        check(414, "P1 Example") { part1(examples[0]) }
        check(6230, "P1 Puzzle") { part1(puzzles[0]) }

        check(1245, "P2 Example") { part2(examples[1]) }
        check(318706, "P2 Puzzle") { part2(puzzles[1]) }

        check(12, "P3 Example") { part3(examples[2]) }
        check(36, "P3 Example") { part3(examples[3]) }
        check(37236, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): Int =
    input[0].substring(2).zip(input[2].substring(2)).count { it.second == it.first } *
            input[1].substring(2).zip(input[2].substring(2)).count { it.second == it.first }

private fun part2(input: List<String>): Int {
    val duckDna = input.map { it.substringAfter(':') }
    var totalDegrees = 0
    for (child in duckDna) {
        val relations = triangularExclusiveSequenceOf(0, duckDna.lastIndex) { hi, lo -> duckDna[lo] to duckDna[hi] }
            .filter { it.first != child && it.second != child }
            .firstOrNull { (a, b) -> related(child, a, b) }
        if (relations == null) continue
        val degree = child.zip(relations.first).count { it.second == it.first } *
                child.zip(relations.second).count { it.second == it.first }
        totalDegrees += degree
    }
    return totalDegrees
}

private fun part3(input: List<String>): Int {
    val duckDna = input.map { it.substringAfter(':') }
    val relationships = mutableMapOf<Int, MutableList<Int>>()
    for ((index, child) in duckDna.withIndex()) {
        val relations = triangularExclusiveSequenceOf(0, duckDna.lastIndex) { hi, lo -> lo to hi }
            .filter { it.first != index && it.second != index }
            .firstOrNull { (a, b) -> related(child, duckDna[a], duckDna[b]) }
        if (relations == null) continue

        relationships.getOrPut(min(index, relations.first) + 1) { mutableListOf() }.add(max(index, relations.first) + 1)
        relationships.getOrPut(min(index, relations.second) + 1) { mutableListOf() }.add(max(index, relations.second) + 1)
    }

    val rels = relationships.entries.mapTo(ArrayList()) { (key, value) -> value.toMutableSet().apply { add(key) } }
    while (true) {
        val toMerge = triangularExclusiveSequenceOf(0, rels.lastIndex) { hi, lo -> lo to hi }
            .firstOrNull { (lo, hi) -> (rels[lo] intersect rels[hi]).isNotEmpty() }
        if (toMerge == null) break
        rels[toMerge.first].addAll(rels[toMerge.second])
        rels.removeAt(toMerge.second)
    }

    return rels.maxBy { it.size }.sum()

    // thought...
//    val a = 0b11011000u
//    val b = 0b11010110u
//    val c = 0b11011010u
//    println((c and (a xor b).inv()) or (c and (a xor b) and a) or (c and (a xor b) and b) == c)
}


fun related(child: String, a: String, b: String): Boolean {
    for (i in child.indices)
        if (a[i] != child[i] && b[i] != child[i]) return false
    return true
}
