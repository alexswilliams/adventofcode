package ec2024.day9

import common.*
import kotlin.math.*
import kotlin.test.*

private const val rootFolder = "ec2024/day9"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example2Input = "$rootFolder/example2.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day9.assertPart1Correct()
    Day9.assertPart2Correct()
    Day9.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 1.28ms
    benchmark { part2(puzzle2Input) } // 178µs
    benchmark { part3(puzzle3Input) } // 18.1ms
}

internal object Day9 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(10, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(13498, it) }
    }

    override fun assertPart2Correct() {
        part2(example2Input).also { println("[Example] Part 2: $it") }.also { assertEquals(10, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(4890, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(10449, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(149564, it) }
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
