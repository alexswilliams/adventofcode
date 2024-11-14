package aoc2023.day7

import common.TwoPartChallenge
import common.benchmark
import common.fromClasspathFileToLines
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day7/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day7/input.txt".fromClasspathFileToLines()

fun main() {
    Day7.assertPart1Correct()
    Day7.assertPart2Correct()
    benchmark { part1(puzzleInput) } // 201µs
    benchmark { part2(puzzleInput) } // 342µs
}

object Day7 : TwoPartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(6440, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(249638405, it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(5905, it) }
        part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(249776650, it) }
    }
}


private data class Hand(val ordering: Int, val bid: Int)
private enum class PokerType(val strength: Int) {
    FIVE_OF_A_KIND(7),
    FOUR_OF_A_KIND(6),
    FULL_HOUSE(5),
    THREE_OF_A_KIND(4),
    TWO_PAIR(3),
    ONE_PAIR(2),
    HIGH_CARD(1)
}

private fun part1(input: List<String>): Int {
    return input.map { it.split(' ', limit = 2) }
        .map { (cards, bid) -> Hand(classifyPokerHand(cards) * fieldShift + cardOrderingStrength(cards, part1Ordering), bid.toInt()) }
        .sortedBy { hand -> hand.ordering }
        .withIndex()
        .sumOf { (index, hand) -> (index + 1) * hand.bid }
}

private fun part2(input: List<String>): Int {
    return input.map { it.split(' ', limit = 2) }
        .map { (cards, bid) -> Hand(classifyCardsWithJoker(cards) * fieldShift + cardOrderingStrength(cards, part2Ordering), bid.toInt()) }
        .sortedBy { hand -> hand.ordering }
        .withIndex()
        .sumOf { (index, hand) -> (index + 1) * hand.bid }
}

private fun classifyCardsWithJoker(cards: String): Int {
    if ('J' !in cards) return classifyPokerHand(cards)
    val nonJokers = cards.filterNot { it == 'J' }
    val jokerCount = 5 - nonJokers.length
    if (jokerCount >= 4) return PokerType.FIVE_OF_A_KIND.strength
    return when (jokerCount) {
        1 -> sequenceOf(nonJokers + nonJokers[0], nonJokers + nonJokers[1], nonJokers + nonJokers[2], nonJokers + nonJokers[3])
        2 -> sequenceOf(
            nonJokers + nonJokers[0] + nonJokers[0], nonJokers + nonJokers[0] + nonJokers[1], nonJokers + nonJokers[0] + nonJokers[2],
            nonJokers + nonJokers[1] + nonJokers[1], nonJokers + nonJokers[1] + nonJokers[2],
            nonJokers + nonJokers[2] + nonJokers[2],
        ).distinct()

        else -> sequenceOf(
            nonJokers + nonJokers[0] + nonJokers[0] + nonJokers[0],
            nonJokers + nonJokers[0] + nonJokers[0] + nonJokers[1],
            nonJokers + nonJokers[0] + nonJokers[1] + nonJokers[1],
            nonJokers + nonJokers[1] + nonJokers[1] + nonJokers[1],
        ).distinct()
    }.map { classifyPokerHand(it) }.max()
}

private fun classifyPokerHand(cards: String): Int {
    val counts = intArrayOf(1, 1, 1, 1, 1)
    var unique = 5
    for (i in 1..4) {
        for (j in 0..<i) {
            if (cards[i] == cards[j]) {
                counts[j]++
                counts[i] = 0
                unique--
                break
            }
        }
    }
    return when {
        unique == 1 -> PokerType.FIVE_OF_A_KIND
        unique == 4 -> PokerType.ONE_PAIR
        unique == 5 -> PokerType.HIGH_CARD
        unique == 2 && counts.any { it == 4 } -> PokerType.FOUR_OF_A_KIND
        unique == 2 && counts.any { it == 3 } -> PokerType.FULL_HOUSE
        unique == 3 && counts.any { it == 3 } -> PokerType.THREE_OF_A_KIND
        unique == 3 && counts.any { it == 1 } && counts.any { it == 2 } -> PokerType.TWO_PAIR
        else -> throw Exception("Unexpected card type for $cards -> $counts")
    }.strength
}

private const val part1Ordering = "23456789TJQKA"
private const val part2Ordering = "J23456789TQKA"
private const val fieldShift = 13 * 13 * 13 * 13 * 13
private fun cardOrderingStrength(cards: String, ordering: String): Int {
    return ordering.indexOf(cards[0]) * 13 * 13 * 13 * 13 +
            ordering.indexOf(cards[1]) * 13 * 13 * 13 +
            ordering.indexOf(cards[2]) * 13 * 13 +
            ordering.indexOf(cards[3]) * 13 +
            ordering.indexOf(cards[4])
}
