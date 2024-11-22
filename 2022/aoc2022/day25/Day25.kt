package aoc2022.day25

import common.*
import kotlin.test.*

private val exampleInput = "aoc2022/day25/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day25/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = "2=-1=0"

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 2-212-2---=00-1--102, took 39.66µs
}

private fun part1(input: List<String>) = input.sumOf { balancedQuinaryToLong(it) }.toBalancedQuinary()

fun balancedQuinaryToLong(bq: String): Long {
    var acc = 0L
    var base = 1L
    for (c in bq.reversed()) {
        acc += when (c) {
            '0' -> 0L
            '1' -> 1L * base
            '2' -> 2L * base
            '-' -> -1L * base
            '=' -> -2L * base
            else -> throw Exception()
        }
        base *= 5L
    }
    return acc
}

private fun Long.toBalancedQuinary(): String {
    var output = ""
    var runningQuotient = this
    do {
        val remainder = runningQuotient % 5
        runningQuotient /= 5
        if (remainder == 3L || remainder == 4L) runningQuotient++
        output += when (remainder) {
            0L -> "0"
            1L -> "1"
            2L -> "2"
            3L -> "="
            4L -> "-"
            else -> throw Exception()
        }
    } while (runningQuotient != 0L)
    return output.reversed()
}
