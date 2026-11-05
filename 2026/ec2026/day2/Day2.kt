package ec2026.day2

import common.Challenge
import common.benchmark
import common.loadFilesToLines
import common.splitForEachRange

private val example = loadFilesToLines("ec2026/day2", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToLines("ec2026/day2", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day2.assertCorrect()
    benchmark { part1(puzzle[0]) } // 16.4µs
    benchmark { part2(puzzle[1]) } // 126.4µs
    benchmark { part3(puzzle[2]) } // 1.9ms
}

internal object Day2 : Challenge {
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


private fun solve(input: List<String>): Long {
    val comparators = IntArray(90)
    return input
        .sumOf { line ->
            val (highestSword, ruleCount) = parseComparators(line, comparators)

            if (ruleCount < highestSword * (highestSword - 1) / 2)
                deriveMissingComparators(comparators, highestSword, ruleCount)

            (1..highestSword)
                .sortedWith { a, b -> find(a, b, comparators) }
                .fold(0L) { acc, i -> acc * 10 + i }
        }
}

private fun deriveMissingComparators(comparators: IntArray, highestSword: Int, given: Int) {
    val allMissingPairs: MutableList<Int> =
        uniqueMissingPairings(highestSword, comparators, given)

    while (allMissingPairs.isNotEmpty()) {
        val solvablePairs = allMissingPairs.mapNotNullTo(ArrayList(allMissingPairs.size)) { pair ->
            val a = pair / 10
            val b = pair % 10
            (1..highestSword).firstNotNullOfOrNull { x ->
                if (x == a || x == b) return@firstNotNullOfOrNull null
                val ax = find(a, x, comparators)
                if (ax != 0 && ax == find(x, b, comparators)) {
                    store(a, b, ax, comparators)
                    pair
                } else null
            }
        }
        allMissingPairs.removeAll(solvablePairs)
    }
}

private fun find(from: Int, to: Int, comparators: IntArray): Int =
    if (from < to) comparators[from * 10 + to]
    else -comparators[to * 10 + from]


private fun store(from: Int, to: Int, value: Int, comparators: IntArray) =
    if (from < to) comparators[from * 10 + to] = value else comparators[to * 10 + from] = -value

private fun parseComparators(line: String, comparators: IntArray): Pair<Int, Int> {
    var ruleCount = 0
    var highestSword = 0
    comparators.fill(0)
    line.splitForEachRange(" ") { str, start, _ ->
        ruleCount++
        val a = str[start].digitToInt()
        val b = str[start + 2].digitToInt()
        highestSword = highestSword.coerceAtLeast(a).coerceAtLeast(b)
        val ordering = when (str[start + 1]) {
            '<' -> 1
            '>' -> -1
            else -> throw IllegalStateException("Invalid input")
        }
        store(a, b, ordering, comparators)
    }
    return Pair(highestSword, ruleCount)
}

fun uniqueMissingPairings(highest: Int, comparators: IntArray, given: Int): MutableList<Int> =
    ArrayDeque<Int>(36 - given).apply {
        for (c in 1..<highest)
            for (d in c + 1..highest)
                if (comparators[c * 10 + d] == 0)
                    add(c * 10 + d)
    }
