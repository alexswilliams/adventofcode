@file:OptIn(ExperimentalUnsignedTypes::class)

package ec2025.day9

import common.*
import java.util.*

private val examples = loadFilesToLines("ec2025/day9", "example1.txt", "example2.txt", "example3a.txt", "example3b.txt")
private val puzzles = loadFilesToLines("ec2025/day9", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day9.assertCorrect()
    benchmark { part1(puzzles[0]) } // 16.6µs
    benchmark(100) { part2(puzzles[1]) } // 8.5ms
    benchmark(1) { part3(puzzles[2]) } // 1.0s
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
    degreeOf(input.map { it.substringAfter(':') }, 0, 1, 2)

private fun part2(input: List<String>): Int {
    val duckDnaStrings = input.map { it.substringAfter(':') }
    val duckDna = duckDnaStrings.map { it.toDna() }
    val likelyMatchCache = emptyMap<Int, List<Int>>()
//    val likelyMatchCache = triangularExclusiveSequenceOf(0, duckDna.lastIndex) { hi, lo -> lo to hi }
//        .filter { (lo, hi) -> isWorthChecking(duckDna[lo], duckDna[hi]) }
//        .groupBy({ it.first }) { it.second }
    return duckDna.indices.sumOf { index ->
        val parents = findParents(duckDna, index, likelyMatchCache) ?: return@sumOf 0
        degreeOf(duckDnaStrings, parents.first, parents.second, index)
    }
}

private fun part3(input: List<String>): Int {
    val duckDna = input.map { it.substringAfter(':').toDna() }
    val relationships = mutableMapOf<Int, MutableList<Int>>()
    val likelyMatchCache = emptyMap<Int, List<Int>>()
//    val likelyMatchCache = triangularExclusiveSequenceOf(0, duckDna.lastIndex) { hi, lo -> lo to hi }
//        .filter { (lo, hi) -> isWorthChecking(duckDna[lo], duckDna[hi]) }
//        .groupBy({ it.first }) { it.second }
    for (index in duckDna.indices) {
        val parents = findParents(duckDna, index, likelyMatchCache) ?: continue
        relationships.getOrPut(index + 1) { mutableListOf() }.apply { add(parents.first + 1); add(parents.second + 1) }
    }

    val subsets = relationships.entries.mapTo(LinkedList()) { (key, value) -> value.toMutableSet().apply { add(key) } }
    while (true) {
        val toMerge = triangularExclusiveSequenceOf(0, subsets.lastIndex) { hi, lo -> lo to hi }
            .firstOrNull { (lo, hi) -> (subsets[lo] intersect subsets[hi]).isNotEmpty() }
        if (toMerge == null) break
        subsets[toMerge.first].addAll(subsets[toMerge.second])
        subsets.removeAt(toMerge.second)
    }
    return subsets.maxBy { it.size }.sum()
}


private fun degreeOf(duckDna: List<String>, parentA: Int, parentB: Int, child: Int): Int =
    duckDna[child].zip(duckDna[parentA]).count { it.second == it.first } *
            duckDna[child].zip(duckDna[parentB]).count { it.second == it.first }

private fun findParents(duckDna: List<ULongArray>, childIndex: Int, likelyMatchCache: Map<Int, List<Int>>): Pair<Int, Int>? =
    triangularExclusiveSequenceOf(0, duckDna.lastIndex) { hi, lo -> lo to hi }
        .filter { it.first != childIndex && it.second != childIndex }
//        .filter { it.second in likelyMatchCache[it.first].orEmpty() }
        .firstOrNull { (a, b) -> related(duckDna[childIndex], duckDna[a], duckDna[b]) }

//private fun isWorthChecking(a: ULongArray, b: ULongArray): Boolean {
//    return a.indices.sumOf { index -> (a[index] xor b[index]).countOneBits() } < a.size * 28
//}
//
private fun related(c: ULongArray, a: ULongArray, b: ULongArray): Boolean =
    c.indices.all { i ->
        val ai = a[i]
        val bi = b[i]
        val ci = c[i]
        val diff = ai xor bi
        val sames = ai and diff.inv() // parts where parents match
        val aa = ci and diff and ai // parts where child and ONLY parent a match
        val bb = ci and diff and bi // parts where child and ONLY parent b match
        sames or aa or bb == ci // recombining all above parts should yield the child
    }

private fun letterToQuad(letter: Char): ULong = when (letter) {
    'C' -> 0b0001uL
    'G' -> 0b0010uL
    'A' -> 0b0100uL
    'T' -> 0b1000uL
    else -> throw Exception()
}

private fun String.toDna(): ULongArray =
    this.windowed(16) { it.fold(0uL) { acc, ch -> (acc shl 4) or letterToQuad(ch) } }
        .toULongArray()
