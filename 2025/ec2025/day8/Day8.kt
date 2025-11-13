package ec2025.day8

import common.*
import kotlinx.coroutines.*
import kotlin.math.*


private val examples = loadFilesToLines("ec2025/day8", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day8", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzles[0]) } // 19.6µs
    benchmark(100) { part2(puzzles[1]) } // 21.5ms
    benchmark(100) { part3(puzzles[2]) } // 696.9ms, or 69.6ms parallel
}

internal object Day8 : Challenge {
    override fun assertCorrect() {
        check(4, "P1 Example") { part1(examples[0], 8) }
        check(58, "P1 Puzzle") { part1(puzzles[0]) }

        check(21, "P2 Example") { part2(examples[1]) }
        check(2926290, "P2 Puzzle") { part2(puzzles[1]) }

        check(7, "P3 Example") { part3(examples[2], 8) }
        check(2786, "P3 Puzzle") { part3(puzzles[2]) }
    }
}

private fun part1(input: List<String>, pegCount: Int = 32): Int =
    input[0].splitToInts(",").zipWithNext { a, b -> (a - b).absoluteValue }.count { it == pegCount / 2 }

private fun part2(input: List<String>): Int {
    val threads = input[0].splitToInts(",").zipWithNext { a, b -> min(a, b) by16 max(a, b) }.sortedBy { it.start() }
    return threads.sumOfIndexed { index, thread ->
        countIntersections(thread, threads.subList(0, index))
    }
}

private fun part3(input: List<String>, pegCount: Int = 256): Int {
    val threads = input[0].splitToInts(",").zipWithNext { a, b -> min(a, b) by16 max(a, b) }.sortedBy { it.start() }
    return runBlocking(Dispatchers.Default) {
        triangularExclusiveSequenceOf(1, pegCount) { hi, lo -> lo by16 hi }
            .map { async { countIntersections(it, threads) } }.toList().awaitAll()
            .max()
    }
}


private typealias ThreadPair = Location1616

private fun ThreadPair.start() = this.row()
private fun ThreadPair.end() = this.col()

@Suppress("ConvertTwoComparisonsToRangeCheck")
private fun countIntersections(cut: ThreadPair, sortedThreads: List<ThreadPair>): Int {
    val x = cut.start()
    val y = cut.end()
    var count = 0
    for (thread in sortedThreads) {
        val a = thread.start()
        if (a > y) break
        val b = thread.end()
        val crossesLeft = x < a && a < y && y < b
        val crossesRight = a < x && x < b && b < y
        val crosses = crossesLeft || crossesRight || a == x && b == y || a == y && b == x
        if (crosses) count++
    }
    return count
}
