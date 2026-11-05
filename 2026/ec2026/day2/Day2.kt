package ec2026.day2

import common.Challenge
import common.benchmark
import common.loadFilesToLines
import common.splitMappingRanges
import java.io.Serializable
import java.util.*

private val example = loadFilesToLines("ec2026/day2", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToLines("ec2026/day2", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day1.assertCorrect()
    benchmark { part1(puzzle[0]) } // 31.0µs
    benchmark { part2(puzzle[1]) } // 566.9µs
    benchmark(10) { part3(puzzle[2]) } // 12.9ms
}

internal object Day1 : Challenge {
    override fun assertCorrect() {
        check(122157, "P1 Example") { part1(example[0]) }
        check(364740, "P1 Puzzle") { part1(puzzle[0]) }

        check(122157, "P2 Example") { part2(example[1]) }
        check(65685452301, "P2 Puzzle") { part2(puzzle[1]) }

        check(122157, "P3 Example") { part3(example[2]) }
        check(586452966624, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: List<String>): Long = solve(input)
private fun part2(input: List<String>): Long = solve(input)
private fun part3(input: List<String>): Long = solve(input)


private fun solve(input: List<String>): Long = input
    .sumOf { line ->
        val uniqueSwords = TreeSet<Char>();
        val comparators: MutableMap<Pair<Char, Char>, Int> = parseComparators(line, uniqueSwords)

        val needed = uniqueSwords.size * (uniqueSwords.size - 1) / 2
        val given = comparators.size
        if (given < needed)
            deriveMissingComparators(uniqueSwords, comparators)

        uniqueSwords.sortedWith { a, b -> find(a, b, comparators)!! }
            .joinToString("")
            .toLong()
    }

private fun deriveMissingComparators(uniqueSwords: TreeSet<Char>, comparators: MutableMap<Pair<Char, Char>, Int>) {
    val allMissingPairs: MutableList<Pair<Char, Char>> =
        uniquePairings(uniqueSwords.max())
            .filterNotTo(ArrayList(45)) { it in comparators }

    while (allMissingPairs.isNotEmpty()) {
        val solvablePairs = allMissingPairs.mapNotNull { pair ->
            val (a, b) = pair
            uniqueSwords.subtract(setOf(a, b)).firstNotNullOfOrNull { x ->
                val ax = find(a, x, comparators)
                val xb = find(x, b, comparators)
                if (ax != null && xb != null && ax == xb) pair to ax else null
            }
        }
        if (solvablePairs.isEmpty()) throw RuntimeException("Cannot be solved")
        solvablePairs.forEach { (pair, ax) ->
            val (a, b) = pair
            store(a, b, ax, comparators)
        }
        allMissingPairs.removeAll(solvablePairs.map { it.first })
    }
}

private fun find(from: Char, to: Char, comparators: MutableMap<Pair<Char, Char>, Int>): Int? =
    if (from < to) comparators[from to to] else comparators[to to from]?.let { -it }

private fun store(from: Char, to: Char, value: Int, comparators: MutableMap<Pair<Char, Char>, Int>) =
    if (from < to) comparators[from to to] = value else comparators[to to from] = -value

private fun parseComparators(line: String, uniqueSwords: TreeSet<Char>): MutableMap<Pair<Char, Char>, Int> {
    var highestSword = '0'
    val result = LinkedHashMap<Pair<Char, Char>, Int>(45)
    line.splitMappingRanges(" ") { str, start, _ ->
        val a = str[start]
        val b = str[start + 2]
        highestSword = highestSword.coerceAtLeast(a).coerceAtLeast(b)
        val ordering = when (str[start + 1]) {
            '<' -> 1
            '>' -> -1
            else -> throw IllegalStateException("Invalid input")
        }
        if (a < b) result[a to b] = ordering else result[b to a] = -ordering
    }
    uniqueSwords += '1'..highestSword
    return result
}

fun uniquePairings(highest: Char): List<Pair<Char, Char>> {
    val result = ArrayList<Pair<Char, Char>>(45)
    for (c in '1'..highest - 1)
        for (d in c + 1..highest)
            result.add(c to d)
    return result
}
