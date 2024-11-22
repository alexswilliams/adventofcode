package day4

import common.*
import kotlin.test.*


fun main() {
    runTests()

    val (first, last) = "day4/input.txt".fromClasspathFileToLines()
        .first()
        .split('-')
        .map { it.toLong() }

    val onlySixDigitNumbers = LongRange(
        first.coerceAtLeast(100_000L),
        last.coerceAtMost(999_999L)
    ).asSequence()

    val withIncreasingDigits = onlySixDigitNumbers.filter(::hasIncreasingDigits)
    val withAnyTwoAdjacentDigitsBeingTheSame = withIncreasingDigits.filter(::hasDoubledUpDigits)

    val count = withAnyTwoAdjacentDigitsBeingTheSame.count()
    println("Part 1: count = $count")
    assertEquals(1625, count)

    val withRunLengthsOfTwo = withAnyTwoAdjacentDigitsBeingTheSame.filter(::containsRunsOfExactlyTwo)
    val partTwoCount = withRunLengthsOfTwo.count()
    println("Part 2: count = $partTwoCount")
    assertEquals(1111, partTwoCount)
}

private fun hasDoubledUpDigits(candidate: Long) =
    candidate.toString().zipWithNext().any { it.first == it.second }

private fun hasIncreasingDigits(candidate: Long) =
    candidate.toString().zipWithNext().all { it.first <= it.second }

private fun containsRunsOfExactlyTwo(candidate: Long): Boolean {
    val startsOfRuns = listOf(0) +
            candidate.toString().zipWithNext()
                .mapIndexedNotNull { index, pair -> if (pair.first != pair.second) index + 1 else null }
    val runRanges = (startsOfRuns.plus(candidate.toString().length)).zipWithNext()
    val runLengths = runRanges.map { it.second - it.first }
    return runLengths.contains(2)
}


private fun runTests() {
    assertTrue(hasIncreasingDigits(111111))
    assertFalse(hasIncreasingDigits(223450))
    assertFalse(hasDoubledUpDigits(123789))

    assertTrue(containsRunsOfExactlyTwo(112233))
    assertFalse(containsRunsOfExactlyTwo(123444))
    assertTrue(containsRunsOfExactlyTwo(111122))
}
