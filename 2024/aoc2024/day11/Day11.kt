package aoc2024.day11

import common.*
import java.util.HashMap.*
import kotlin.math.*

private val example = loadFiles("aoc2024/day11", "example.txt").single()
private val puzzle = loadFiles("aoc2024/day11", "input.txt").single()

internal fun main() {
    Day11.assertCorrect()
    benchmark { part1(puzzle) } // 141µs
    benchmark(100) { part2(puzzle) } // 6.9ms
}

internal object Day11 : Challenge {
    override fun assertCorrect() {
        check(55312, "P1 Example") { part1(example) }
        check(202019, "P1 Puzzle") { part1(puzzle) }

        check(65601038650482L, "P2 Example") { part2(example) }
        check(239321955280205L, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: String): Long = with(newCache(25)) { input.splitToLongs(" ").sumOf { countValuesBeneath(25, it, this) } }
private fun part2(input: String): Long = with(newCache(75)) { input.splitToLongs(" ").sumOf { countValuesBeneath(75, it, this) } }


fun countValuesBeneath(iterRemaining: Int, number: Long, cache: Array<MutableMap<Long, Long>>): Long {
    if (iterRemaining == 0) return 1
    cache[iterRemaining - 1][number]?.let { return it }

    val digitCount = log10(number.toFloat()).toInt() + 1
    val countAtThisNode = if (number == 0L)
        countValuesBeneath(iterRemaining - 1, 1L, cache)
    else if (digitCount and 1 == 0) {
        val power = powersOfTen[digitCount / 2]
        val left = number / power
        val right = number % power
        countValuesBeneath(iterRemaining - 1, left, cache) + countValuesBeneath(iterRemaining - 1, right, cache)
    } else countValuesBeneath(iterRemaining - 1, number * 2024, cache)

    return countAtThisNode.also { cache[iterRemaining - 1][number] = it }
}

private fun newCache(breathCount: Int): Array<MutableMap<Long, Long>> = Array(breathCount) { i -> newHashMap(cacheSize(breathCount, i)) }
private fun cacheSize(breathCount: Int, i: Int) =
    if (breathCount <= 25 || i >= 50) 300
    else if (i >= 40) 1000
    else if (i >= 30) 2000
    else if (i >= 20) 3000
    else 4000

private val powersOfTen = longArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)
