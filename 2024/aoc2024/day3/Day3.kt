package aoc2024.day3

import common.*

private val examples = loadFiles("aoc2024/day3", "example1.txt", "example2.txt")
private val puzzle = loadFiles("aoc2024/day3", "input.txt").single()

internal fun main() {
    Day3.assertCorrect()
    benchmark { part1(puzzle) } // 253µs
    benchmark(10) { part2(puzzle) } // 1.25ms
}

internal object Day3 : Challenge {
    override fun assertCorrect() {
        check(161, "P1 Example") { part1(examples[0]) }
        check(173517243, "P1 Puzzle") { part1(puzzle) }

        check(48, "P2 Example") { part2(examples[1]) }
        check(100450138, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: String): Int =
    mulRegex.findAll(input)
        .sumOf { asPairOfIntegers(it).product() }

private fun part2(input: String): Int {
    val doPositions = input.locationOfEach("do()")
    val dontPositions = input.locationOfEach("don't()")
    return mulRegex.findAll(input).sumOf { match ->
        val lastDoBefore = doPositions.lastOrNull { it < match.range.first } ?: Int.MIN_VALUE
        val lastDontBefore = dontPositions.lastOrNull { it < match.range.first } ?: Int.MIN_VALUE
        if (max(lastDoBefore, lastDontBefore) == lastDoBefore)
            asPairOfIntegers(match).product()
        else 0
    }
}


private val mulRegex = Regex("mul\\([0-9]{1,3},[0-9]{1,3}\\)")

private fun asPairOfIntegers(match: MatchResult) =
    match.value.substring(4, match.value.length - 1).splitToInts(",")
