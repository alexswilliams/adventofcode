package aoc2024.day7

import common.*
import kotlin.math.*

private val example = loadFilesToLines("aoc2024/day7", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day7", "input.txt").single()

internal fun main() {
    Day7.assertCorrect()
    benchmark { part1(puzzle) } // 256µs
    benchmark { part2(puzzle) } // 672µs
}

internal object Day7 : Challenge {
    override fun assertCorrect() {
        check(3749, "P1 Example") { part1(example) }
        check(3312271365652, "P1 Puzzle") { part1(puzzle) }

        check(1100, "P2 Test") { part2(listOf("1100: 1 100")) }
        check(11387, "P2 Example") { part2(example) }
        check(509463489296712, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Long = input.asSequence().map { it.splitToLongs(" ") }
    .filter { numbers -> possibleToMake(numbers[0], numbers.tail(), numbers.lastIndex - 1, false) }
    .sumOf { it[0] }

private fun part2(input: List<String>): Long = input.asSequence().map { it.splitToLongs(" ") }
    .filter { numbers -> possibleToMake(numbers[0], numbers.tail(), numbers.lastIndex - 1, true) }
    .sumOf { it[0] }


fun possibleToMake(target: Long, using: List<Long>, startAt: Int, includeConcat: Boolean): Boolean {
    if (startAt == 0 && using[0] == target) return true
    if (startAt < 0) return false
    val n = using[startAt]

    if (target % n == 0L && possibleToMake(target / n, using, startAt - 1, includeConcat)) return true
    if (target - n >= 0 && possibleToMake(target - n, using, startAt - 1, includeConcat)) return true
    if (includeConcat) {
        val divider = POWERS_OF_TEN[log10(n.toFloat()).toInt() + 1]
        if (target % divider == n && possibleToMake(target / divider, using, startAt - 1, true)) return true
    }
    return false
}

private val POWERS_OF_TEN = longArrayOf(1, 10, 100, 1000, 10000)
