package ec2024.day9

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("ec2024/day9", "example.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day9", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day9.assertCorrect()
    benchmark { part1(puzzles[0]) } // 1.28ms
    benchmark { part2(puzzles[1]) } // 178µs
    benchmark { part3(puzzles[2]) } // 18.1ms
}

internal object Day9 : Challenge {
    override fun assertCorrect() {
        check(10, "P1 Example") { part1(examples[0]) }
        check(13498, "P1 Puzzle") { part1(puzzles[0]) }

        check(10, "P2 Example") { part2(examples[1]) }
        check(4890, "P2 Puzzle") { part2(puzzles[1]) }

        check(10449, "P3 Example") { part3(examples[2]) }
        check(149564, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): Int {
    val stamps = intArrayOf(1, 3, 5, 10).sortedArrayDescending()
    val memory = HashMap<Int, Int>(stamps.associate { it to 1 })
    return input.sumOf { shortestWayToMake(it.toInt(), memory, stamps) }
}

private fun part2(input: List<String>): Int {
    val stamps = intArrayOf(1, 3, 5, 10, 15, 16, 20, 24, 25, 30).sortedArrayDescending()
    val memory = HashMap<Int, Int>(stamps.associate { it to 1 })
    return input.sumOf { shortestWayToMake(it.toInt(), memory, stamps) }
}

private fun part3(input: List<String>): Int {
    val stamps = intArrayOf(1, 3, 5, 10, 15, 16, 20, 24, 25, 30, 37, 38, 49, 50, 74, 75, 100, 101).sortedArrayDescending()
    val memory = HashMap<Int, Int>(stamps.associate { it to 1 })
    return input
        .map { it.toInt() }
        .sumOf { n ->
            (((((n + 1) - 100) / 2)..(n / 2)))
                .minOf { shortestWayToMake(it, memory, stamps) + shortestWayToMake(n - it, memory, stamps) }
        }
}

private fun shortestWayToMake(target: Int, cache: MutableMap<Int, Int>, stamps: IntArray): Int {
    val fromCache = cache[target]
    if (fromCache != null) return fromCache

    var minSoFar = Integer.MAX_VALUE
    for (i in stamps) {
        if (i < target)
            minSoFar = min(minSoFar, shortestWayToMake(target - i, cache, stamps))
    }
    return (minSoFar + 1).also { cache[target] = it }
}
