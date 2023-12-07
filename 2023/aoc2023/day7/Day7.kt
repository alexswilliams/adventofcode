package aoc2023.day7

import common.benchmark
import common.fromClasspathFileToLines
import kotlin.test.assertEquals


private val exampleInput = "aoc2023/day7/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2023/day7/input.txt".fromClasspathFileToLines()

fun main() {
    part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(6440, it) }
    part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(249638405, it) }
    part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(5905, it) }
    part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(249776650, it) }
    benchmark { part1(puzzleInput) } // 397µs
    benchmark { part2(puzzleInput) } // 614µs
}


private data class Hand(val cards: String, val cardOrderStrength: Int, val type: PokerType, val bid: Int)
private enum class PokerType(val strength: Int) {
    FIVE_O_A_K(7),
    FOUR_O_A_K(6),
    FULL_HOUSE(5),
    THREE_O_A_K(4),
    TWO_PAIR(3),
    ONE_PAIR(2),
    HIGH_CARD(1)
}

private fun part1(input: List<String>): Int {
    return input.map { it.split(' ') }
        .map { (cards, bid) -> Hand(cards, cardToOrderedStrength(cards, part1Ordering), classifyCards(cards), bid.toInt()) }
        .sortedWith(compareBy<Hand> { hand -> hand.type.strength }.thenComparing { hand -> hand.cardOrderStrength })
        .withIndex()
        .sumOf { (index, hand) -> (index + 1) * hand.bid }
}

private fun part2(input: List<String>): Int {
    return input.map { it.split(' ') }
        .map { (cards, bid) -> Hand(cards, cardToOrderedStrength(cards, part2Ordering), classifyCardsWithJoker(cards), bid.toInt()) }
        .sortedWith(compareBy<Hand> { hand ->
            hand.type.strength
        }.thenComparing { hand ->
            hand.cardOrderStrength
        })
        .withIndex()
        .sumOf { (index, hand) -> (index + 1) * hand.bid }
}

private fun classifyCardsWithJoker(cards: String): PokerType {
    if ('J' !in cards) return classifyCards(cards)
    if (cards == "JJJJJ") return PokerType.FIVE_O_A_K
    val nonJokers = cards.filterNot { it == 'J' }
    val jokerCount = 5 - nonJokers.length
    return when (jokerCount) {
        1 -> listOf(nonJokers + nonJokers[0], nonJokers + nonJokers[1], nonJokers + nonJokers[2], nonJokers + nonJokers[3])
        2 -> listOf(
            nonJokers + nonJokers[0] + nonJokers[0], nonJokers + nonJokers[0] + nonJokers[1], nonJokers + nonJokers[0] + nonJokers[2],
            nonJokers + nonJokers[1] + nonJokers[1], nonJokers + nonJokers[1] + nonJokers[2],
            nonJokers + nonJokers[2] + nonJokers[2],
        )

        3 -> listOf(
            nonJokers + nonJokers[0] + nonJokers[0] + nonJokers[0],
            nonJokers + nonJokers[0] + nonJokers[0] + nonJokers[1],
            nonJokers + nonJokers[0] + nonJokers[1] + nonJokers[1],
            nonJokers + nonJokers[1] + nonJokers[1] + nonJokers[1],
        )

        else -> listOf(nonJokers.repeat(5))
    }.map { classifyCards(it) }.maxBy { it.strength }
}

private fun classifyCards(cards: String): PokerType {
    val groups = cards.groupBy { it }
    return when {
        groups.size == 1 -> PokerType.FIVE_O_A_K
        groups.size == 2 && groups.values.any { it.size == 4 } -> PokerType.FOUR_O_A_K
        groups.size == 2 && groups.values.any { it.size == 3 } -> PokerType.FULL_HOUSE
        groups.size == 3 && groups.values.any { it.size == 3 } -> PokerType.THREE_O_A_K
        groups.size == 3 && groups.values.any { it.size == 1 } && groups.values.any { it.size == 2 } -> PokerType.TWO_PAIR
        groups.size == 4 && groups.values.any { it.size == 2 } -> PokerType.ONE_PAIR
        groups.size == 5 -> PokerType.HIGH_CARD
        else -> throw Exception("Unexpected card type for $cards -> $groups")
    }
}


private const val part1Ordering = "23456789TJQKA"
private const val part2Ordering = "J23456789TQKA"
private fun cardToOrderedStrength(cards: String, ordering: String): Int {
    return ordering.indexOf(cards[0]) * 100_00_00_00 +
            ordering.indexOf(cards[1]) * 100_00_00 +
            ordering.indexOf(cards[2]) * 100_00 +
            ordering.indexOf(cards[3]) * 100 +
            ordering.indexOf(cards[4])
}
