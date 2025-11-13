package ec2025.day8

import common.*
import kotlin.math.*


private val examples = loadFilesToLines("ec2025/day8", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day8", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzles[0]) } // 19.6µs
    benchmark(100) { part2(puzzles[1]) } // 42.7ms
    benchmark(1) { part3(puzzles[2]) } // 1.2s
}

internal object Day8 : Challenge {
    override fun assertCorrect() {
        check(4, "P1 Example") { part1(examples[0], 8) }
        check(58, "P1 Puzzle") { part1(puzzles[0]) }

        check(21, "P2 Example") { part2(examples[1], 8) }
        check(2926290, "P2 Puzzle") { part2(puzzles[1]) }

        check(7, "P3 Example") { part3(examples[2], 8) }
        check(2786, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>, pegCount: Int = 32): Int =
    input[0].splitToInts(",")
        .zipWithNext { a, b -> (a - b).absoluteValue }
        .count { it == pegCount / 2 }

private fun part2(input: List<String>, pegCount: Int = 256): Int {
    val threads = input[0].splitToInts(",").zipWithNext { a, b -> a % pegCount to b % pegCount }
    return threads.sumOfIndexed { index, thread ->
        countIntersections(pegCount, thread, threads.subList(0, index))
    }
}

private fun part3(input: List<String>, pegCount: Int = 256): Int {
    val threads = input[0].splitToInts(",").map { it % pegCount }.zipWithNext()
    return triangularExclusiveSequenceOf(0, pegCount - 1) { a, b -> b to a }
        .maxOf { countIntersections(pegCount, it, threads) }
}


fun <R> triangularExclusiveSequenceOf(min: Int, max: Int, transform: (Int, Int) -> R): Sequence<R> = sequence {
    for (a in min..max) {
        for (b in min..<a) {
            yield(transform(a, b))
        }
    }
}

private fun countIntersections(
    pegCount: Int,
    thread: Pair<Int, Int>,
    threads: List<Pair<Int, Int>>,
): Int {
    // Rotate the board so that thread.first == 0 and thread.second is somewhere below
    val rotation = pegCount - thread.first
    val bottom = (thread.second + rotation) % pegCount
    return threads.count { (a, b) ->
        val aR = (a + rotation) % pegCount
        val bR = (b + rotation) % pegCount
        val fullyOnLeftSide = ((aR == 0 || aR >= bottom) && (bR == 0 || bR >= bottom))
        val fullyOnRightSide = (aR <= bottom && bR <= bottom)
        val downCentre = fullyOnLeftSide && fullyOnRightSide
        downCentre || !(fullyOnLeftSide || fullyOnRightSide)
    }
}
