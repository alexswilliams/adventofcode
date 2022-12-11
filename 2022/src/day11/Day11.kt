package day11

import common.*
import kotlin.test.*
import kotlin.text.RegexOption.*

private val exampleInput = "day11/example.txt".fromClasspathFile()
private val puzzleInput = "day11/input.txt".fromClasspathFile()
private const val PART_1_EXPECTED_ANSWER = 10605L
private const val PART_2_EXPECTED_ANSWER = 2713310158L


fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1: " + part1(puzzleInput)) // 62491

    assertEquals(PART_2_EXPECTED_ANSWER, part2(exampleInput))
    println("Part 2: " + part2(puzzleInput)) // 17408399184
}

private fun part1(input: String) = runPuzzle(parseInput(input), 20, 3)
private fun part2(input: String) = runPuzzle(parseInput(input), 10_000, 1)


private fun runPuzzle(monkeys: List<Monkey>, iterations: Int, boredomDivisor: Int): Long {
    val modulus = lcm(monkeys.map { it.divisor })
    val counts = Array(monkeys.size) { 0L }
    val allItems = monkeys.flatMapIndexed { index, monkey -> monkey.initialItems.map { it to index } }
    allItems.forEach { (initialValue, startingMonkey) ->
        var round = 1
        var value = initialValue
        var thisMonkey = startingMonkey
        while (round <= iterations) {
            counts[thisMonkey]++
            value = (monkeys[thisMonkey].worry(value) / boredomDivisor) % modulus
            val nextMonkey = monkeys[thisMonkey].next(value)
            if (nextMonkey < thisMonkey) round++
            thisMonkey = nextMonkey
        }
    }
    return counts.sortedArrayDescending().take(2).product()
}


private val matcher = Regex(".*items: ([0-9, ]+)+.*new = old ([^\n]+).*by ([0-9]+).*monkey ([0-9]+).*monkey ([0-9]+)", DOT_MATCHES_ALL)
private fun parseInput(input: String): List<Monkey> = input.split("\n\n")
    .mapMatching(matcher).map { (items, op, mod, ifTrue, ifFalse) ->
        Monkey(
            initialItems = items.split(", ").map(String::toLong),
            divisor = mod.toInt(),
            multiplication = if (op.startsWith('*') && op != "* old") op.drop(2).toInt() else 1,
            addition = if (op.startsWith('+')) op.drop(2).toInt() else 0,
            square = op == "* old",
            ifTrue = ifTrue.toInt(),
            ifFalse = ifFalse.toInt()
        )
    }

private data class Monkey(
    val initialItems: List<Long>,
    val divisor: Int,
    private val multiplication: Int = 1,
    private val addition: Int = 0,
    private val square: Boolean = false,
    private val ifTrue: Int,
    private val ifFalse: Int
) {
    fun worry(old: Long) = if (square) (old * old) else (old * multiplication + addition)
    fun next(toTest: Long) = if (toTest % divisor == 0L) ifTrue else ifFalse
}
