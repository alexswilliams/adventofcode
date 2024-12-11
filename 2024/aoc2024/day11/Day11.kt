package aoc2024.day11

import common.*
import java.math.*
import java.util.HashMap.*
import kotlin.math.*

private val example = loadFiles("aoc2024/day11", "example.txt").single()
private val puzzle = loadFiles("aoc2024/day11", "input.txt").single()

internal fun main() {
    Day11.assertCorrect()
    benchmark { part1(puzzle) } // 593µs
    benchmark(100) { part2(puzzle) } // 21.5ms :(
}

internal object Day11 : Challenge {
    override fun assertCorrect() {
        check(55312, "P1 Example") { part1(example) }
        check(202019, "P1 Puzzle") { part1(puzzle) }

        check(65601038650482L, "P2 Example") { part2(example) }
        check(239321955280205L, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: String): Long = input.splitToLongs(" ").sumOf { countValuesBeneath(25, it) }

private fun part2(input: String): Long = input.splitToLongs(" ").sumOf { countValuesBeneath(75, it) }

fun countValuesBeneath(iterRemaining: Int, number: Long, cache: MutableMap<Pair<Long, Int>, Long> = newHashMap(120_000)): Long {
    if (iterRemaining == 0) return 1
    val cacheKey = number to iterRemaining
    cache[cacheKey]?.let { return it }

    val digitCount = log10(number.toFloat()).toInt() + 1
    return if (number == 0L) {
        countValuesBeneath(iterRemaining - 1, 1L, cache)
    } else if (digitCount % 2 == 0) {
        val power = powersOfTen[digitCount / 2]
        val left = number / power
        val right = number % power
        countValuesBeneath(iterRemaining - 1, left, cache) + countValuesBeneath(iterRemaining - 1, right, cache)
    } else {
        countValuesBeneath(iterRemaining - 1, number * 2024, cache)
    }.also { cache[cacheKey] = it }
}

private val powersOfTen = longArrayOf(
    1,
    10,
    100,
    1000,
    10000,
    100000,
    1000000,
    10000000,
    100000000,
)