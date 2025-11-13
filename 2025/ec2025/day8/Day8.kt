package ec2025.day8

import common.*
import kotlinx.coroutines.*
import kotlin.math.*


private val examples = loadFiles("ec2025/day8", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFiles("ec2025/day8", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzles[0]) } // 19.6µs
    benchmark(100) { part2(puzzles[1]) } // 13.9ms
    benchmark(100) { part3(puzzles[2]) } // 305.6ms, or 34.4ms parallel
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

private fun part1(input: String, pegCount: Int = 32): Int =
    input.splitToInts(",").zipWithNext { a, b -> (a - b).absoluteValue }.count { it == pegCount / 2 }

private fun part2(input: String): Int {
    val threads = input.splitToInts(",").zipWithNext { a, b -> min(a, b) by16 max(a, b) }.sorted().toIntArray()
    return threads.sumOfIndexed { index, thread ->
        countIntersections(thread, threads.sliceArray(0..<index))
    }
}

private fun part3(input: String, pegCount: Int = 256): Int {
    val threads = input.splitToInts(",").zipWithNext { a, b -> min(a, b) by16 max(a, b) }.sorted().toIntArray()
    return runBlocking(Dispatchers.Default) {
        triangularExclusiveSequenceOf(1, pegCount) { hi, lo -> lo by16 hi }
            .mapTo(arrayListOf()) { async { countIntersections(it, threads) } }.awaitAll() // parallel
//            .mapTo(arrayListOf()) { countIntersections(it, threads) } // single-thread
            .max()
    }
}


private typealias ThreadPair = Location1616 // borrowing an efficient bit-packing type from previous years' grid puzzles

private fun ThreadPair.start() = row()
private fun ThreadPair.end() = col()
private fun ThreadPair.reverse() = col() by16 row()

@Suppress("ConvertTwoComparisonsToRangeCheck")
private fun countIntersections(cut: ThreadPair, orderedThreads: IntArray): Int {
    val cutStart = cut.start()
    val cutEnd = cut.end()
    var count = 0
    for (thread in orderedThreads) {
        val threadStart = thread.start()
        val threadEnd = thread.end()
        if (threadStart > cutEnd) break // because the list is sorted, you can shortcut the rest of the list once it has moved out of intersection range
        val crossesLeft = cutStart < threadStart && threadStart < cutEnd && cutEnd < threadEnd
        val crossesRight = threadStart < cutStart && cutStart < threadEnd && threadEnd < cutEnd
        val isEqual = thread == cut || thread.reverse() == cut
        val crosses = crossesLeft || crossesRight || isEqual
        if (crosses) count++
    }
    return count
}
