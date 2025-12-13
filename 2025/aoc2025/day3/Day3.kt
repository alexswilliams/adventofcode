package aoc2025.day3

import common.*

private val example = loadFilesToLines("aoc2025/day3", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day3", "input.txt").single()

internal fun main() {
    Day3.assertCorrect()
    benchmark { part1(puzzle) } // 152.0µs
    benchmark { part2(puzzle) } // 254.9µs
}

internal object Day3 : Challenge {
    override fun assertCorrect() {
        check(357, "P1 Example") { part1(example) }
        check(17346, "P1 Puzzle") { part1(puzzle) }

        check(3121910778619L, "P2 Example") { part2(example) }
        check(172981362045136L, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Long = input.sumOf { bank -> bank.findHighestJoltage(2) }
private fun part2(input: List<String>): Long = input.sumOf { bank -> bank.findHighestJoltage(12) }

// This has n*m complexity - the worst case is that it scans the whole string only to find the highest number is at
// the start, then scans 2...n of the string to find the highest number is at position 2, etc, which ends up scanning
// the string in a triangular pattern.
// There is an n log m solution, which is possible because the number of digits needed is always fixed in advance and
// the possible range of values is limited (which are the same constraints that give the Dutch national flag algorithm
// its linear-time solution,) e.g. you might use 12 windows to track possible ranges for each digit - as you consume
// characters from the stream, you advance some ranges forward based on ranges in the past updating, and you could store
// these in a data structure that gives you log m access times; but honestly it's likely to be slower than the few
// hundred micro-seconds that the recursive n*m solution takes.
private fun String.findHighestJoltage(digitNumber: Int, rangeStart: Int = 0, rangeEnd: Int = length - digitNumber): Long =
    substring(rangeStart, rangeEnd + 1).withIndex().maxBy { it.value }
        .let { highestDigit ->
            if (digitNumber == 1)
                highestDigit.value.digitToInt().toLong()
            else
                highestDigit.value.digitToInt() * 10L.pow(digitNumber - 1) +
                        findHighestJoltage(
                            digitNumber = digitNumber - 1,
                            rangeStart = rangeStart + highestDigit.index + 1,
                            rangeEnd = length - digitNumber + 1
                        )
        }
