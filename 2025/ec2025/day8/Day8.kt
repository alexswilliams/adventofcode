package ec2025.day8

import common.*
import kotlin.math.*


private val examples = loadFilesToLines("ec2025/day8", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day8", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzles[0]) } // 19.6µs
    benchmark(10) { part2(puzzles[1]) } // 62ms
    benchmark(10) { part3(puzzles[2]) } // 1.2s
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


private fun part1(input: List<String>, pegCount: Int = 32): Int {
    val nodes = input[0].splitToInts(",")
    val centreDistance = pegCount / 2
    return nodes.zipWithNext { a, b -> (a - b).absoluteValue }.count { it == centreDistance }
}

private fun part2(input: List<String>, pegCount: Int = 256): Int {
    val nodes = input[0].splitToInts(",").map { it % pegCount }
    val threads = nodes.zipWithNext()

    var crosses = 0
    threads.fold(listOf<Pair<Int, Int>>()) { addedSoFar, thread ->
        val rotationToAdd = pegCount - thread.first
        val rotatedSecond = (thread.second + rotationToAdd) % pegCount
        val leftSide = rotatedSecond..<pegCount // plus 0
        val rightSide = 0..rotatedSecond
        crosses += addedSoFar.count { (a, b) ->
            val aR = (a + rotationToAdd) % pegCount
            val bR = (b + rotationToAdd) % pegCount
            val doesntCross = ((aR == 0 || aR in leftSide) && (bR == 0 || bR in leftSide)) || (aR in rightSide && bR in rightSide)
            !doesntCross
        }
        addedSoFar.plus(thread)
    }
    return crosses
}

private fun part3(input: List<String>, pegCount: Int = 256): Int {
    val nodes = input[0].splitToInts(",").map { it % pegCount }
    val threads = nodes.zipWithNext()

    return cartesianProductSequenceOf(0..<pegCount, 0..<pegCount).filter { (a, b) -> a < b }.maxOf { thread ->
        val rotationToAdd = pegCount - thread.first
        val rotatedSecond = (thread.second + rotationToAdd) % pegCount
        val leftSide = rotatedSecond..<pegCount // plus 0
        val rightSide = 0..rotatedSecond
        threads.count { (a, b) ->
            val aR = (a + rotationToAdd) % pegCount
            val bR = (b + rotationToAdd) % pegCount
            val isSameAsThread = ((aR == 0) && (bR == rotatedSecond)) || ((bR == 0) && (aR == rotatedSecond))
            val doesntCross = ((aR == 0 || aR in leftSide) && (bR == 0 || bR in leftSide)) || (aR in rightSide && bR in rightSide)
            isSameAsThread || !doesntCross
        }
    }
}
