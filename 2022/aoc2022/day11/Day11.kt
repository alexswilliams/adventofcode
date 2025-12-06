package aoc2022.day11

import common.*
import kotlin.test.*
import kotlin.text.RegexOption.*

private val example = loadFiles("aoc2022/day11", "example.txt").single()
private val puzzle = loadFiles("aoc2022/day11", "input.txt").single()

internal fun main() {
    Day11.assertCorrect()
    benchmark { part1(puzzle) } // 89.7µs
    benchmark { part2(puzzle) } // 1.1ms
}

internal object Day11 : Challenge {
    override fun assertCorrect() {
        check(10605, "P1 Example") { part1(example) }
        check(62491, "P1 Puzzle") { part1(puzzle) }

        check(2713310158, "P2 Example") { part2(example) }
        check(17408399184, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: String) = runPuzzle(parseInput(input), iterations = 20, 3, lookForCyclesUntilPercent = 55)
private fun part2(input: String) = runPuzzle(parseInput(input), iterations = 10_000, 1, lookForCyclesUntilPercent = 15)


private fun runPuzzle(monkeys: List<Monkey>, iterations: Int, boredomDivisor: Int, lookForCyclesUntilPercent: Int): Long {
    val modulus = lcm(monkeys.map { it.divisor })
    val allItems = monkeys.flatMapIndexed { index, (initialItems) -> initialItems.map { it to index } }
    val roundToGiveUpLookingForCycles = (iterations * lookForCyclesUntilPercent) / 100

    val counts = LongArray(monkeys.size)
    allItems.forEach { item ->
        val cycleFinder = CycleFinder(iterations, roundToGiveUpLookingForCycles, monkeys.size)
        var (value, thisMonkey) = item
        var round = 1
        while (round <= iterations) {
            counts[thisMonkey]++
            round += cycleFinder.advanceOverCycleIfFound(round, value, thisMonkey, counts)
            value = ((monkeys[thisMonkey].worry(value) / boredomDivisor) % modulus).toInt()
            val nextMonkey = monkeys[thisMonkey].next(value)
            if (nextMonkey < thisMonkey) round++
            thisMonkey = nextMonkey
        }
    }
    return counts.sortedArrayDescending().take(2).product()
}


private class CycleFinder(val iterations: Int, val roundToGiveUp: Int, val numberOfMonkeys: Int) {
    var lookingForCycles = true
    val stateHistory = HashMap<Int, Int>(iterations / 4, 0.9f) // The first round at which any given "state" was seen
    val countHistory = HashMap<Int, LongArray>(iterations / 4, 0.9f) // Item counts when each state was encountered

    fun advanceOverCycleIfFound(round: Int, value: Int, thisMonkey: Int, countsMaybeMutated: LongArray) =
        if (lookingForCycles && round <= roundToGiveUp) {
            val idx = value * numberOfMonkeys + thisMonkey
            val roundFirstSeen = stateHistory[idx]
            if (roundFirstSeen != null) {
                lookingForCycles = false
                advanceOverCycle(round, countsMaybeMutated, roundFirstSeen, countHistory.getValue(idx))
            } else {
                stateHistory[idx] = round
                countHistory[idx] = countsMaybeMutated.copyOf()
                0 // still looking, so no adjustment
            }
        } else 0 // no longer looking, so no adjustment

    private fun advanceOverCycle(round: Int, counts: LongArray, roundFirstSeen: Int, countsAtStartOfCycle: LongArray): Int {
        val cycleLength = round - roundFirstSeen
        val repeats = (iterations - round) / cycleLength
        counts.forEachIndexed { idx, _ -> counts[idx] += repeats * (counts[idx] - countsAtStartOfCycle[idx]) }
        return repeats * cycleLength
    }
}


private val matcher = Regex(".*items: ([0-9, ]+)+.*new = old ([^\n]+).*by ([0-9]+).*monkey ([0-9]+).*monkey ([0-9]+)", DOT_MATCHES_ALL)
private fun parseInput(input: String): List<Monkey> = input.split("\n\n")
    .mapMatching(matcher).map { (items, op, mod, ifTrue, ifFalse) ->
        Monkey(
            initialItems = items.split(", ").map(String::toInt),
            divisor = mod.toInt(),
            multiplication = if (op.startsWith('*') && op != "* old") op.drop(2).toInt() else 1,
            addition = if (op.startsWith('+')) op.drop(2).toInt() else 0,
            square = op == "* old",
            ifTrue = ifTrue.toInt(),
            ifFalse = ifFalse.toInt()
        )
    }

private data class Monkey(
    val initialItems: List<Int>,
    val divisor: Int,
    private val multiplication: Int = 1,
    private val addition: Int = 0,
    private val square: Boolean = false,
    private val ifTrue: Int,
    private val ifFalse: Int
) {
    fun worry(old: Int): Long = if (square) (old.toLong() * old.toLong()) else (old.toLong() * multiplication + addition)
    fun next(toTest: Int) = if (toTest % divisor == 0) ifTrue else ifFalse
}
