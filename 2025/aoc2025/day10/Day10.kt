package aoc2025.day10

import common.*
import common.BitSet
import java.util.*
import java.util.Comparator.*

private val example = loadFilesToLines("aoc2025/day10", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day10", "input.txt").single()

internal fun main() {
    Day10.assertCorrect()
    benchmark { part1(puzzle) } // 791.7µs
    benchmark(0) { part2(puzzle) } // basically infinite?
}

internal object Day10 : Challenge {
    override fun assertCorrect() {
        check(7, "P1 Example") { part1(example) }
        check(502, "P1 Puzzle") { part1(puzzle) }

        check(33, "P2 Example") { part2(example) }
        check(0, "P2 Puzzle") { part2(puzzle) }
    }

    override val skipTests: Boolean get() = true
}


private fun part1(input: List<String>): Int =
    input.sumOf { line ->
        val firstSpace = line.indexOf(' ', 3)
        val lastSpace = line.lastIndexOf(' ', line.lastIndex - 3)
        // There seem to always be fewer than 64 lights and 64 buttons, so they can be represented as 64-bit bitmasks
        // This means "toggling" lights using a button is just `lights xor button`, which is super fast.
        val lights = line.substring(1, firstSpace - 1).sumOfIndexedLong { index, light -> if (light == '#') pow2L(index) else 0L }
        val buttonMasks = line.splitMappingRanges(" ", firstSpace + 1, lastSpace - 1) { _, start, end ->
            line.splitToInts(",", start + 1, end).sumOf { lights -> pow2L(lights) }
        }.distinct()
        searchButtonPresses(lights, buttonMasks.toLongArray())
    }

// A button is either used once, or not at all.  (Any multiple of 2 pushes of the same button will cancel out, so all even numbers
// of pushes cause no net change, and odd numbers over 1 reduce to the net change of a single button push, i.e. 1+2n => 1.
// Given this, you can represent all combinations of N buttons as the binary representatin of the numbers up to 2^N.
private fun searchButtonPresses(targetStates: BitSet, actions: LongArray): Int =
    (0L..<pow2L(actions.size)).minOf { combination ->
        var states = targetStates
        combination.forEach { buttonMask -> states = states xor actions[buttonMask.countTrailingZeroBits()] }
        if (states == 0L) combination.countOneBits()
        else Int.MAX_VALUE
    }


private fun part2(input: List<String>): Int {
    val switchActions = input.map { line -> line.substringAfter(' ').substringBeforeLast(' ').split(' ').map { it.trim('(', ')').splitToInts(",") }.distinct() }
    val targetJoltages = input.map { line -> line.substringAfterLast(' ').trim('{', '}').splitToInts(",") }
    return targetJoltages.zip(switchActions).sumOf { (targetJoltages, actions) ->
        searchButtonPressesForJoltage(targetJoltages, actions).also { println("Depth $it for $targetJoltages from $actions") }
    }
}

fun searchButtonPressesForJoltage(targetStates: List<Int>, actions: List<List<Int>>): Int {
    data class State(val targetJoltages: List<Int>, val depth: Int)

    val maxDepth = targetStates.sum()
    val queue = PriorityQueue(comparingInt(State::depth))
        .apply { offer(State(targetStates, 0)) }
    val cache = HashSet<List<Int>>().apply { add(targetStates) }
    while (true) {
        val (joltages, depth) = queue.poll() ?: throw Exception("Search space exhausted")
        actions.forEach { button ->
            val newJoltages = applyButtonsToJoltage(joltages, button)
            if (newJoltages.all { it == 0 }) return depth + 1
            if (depth + 1 <= maxDepth && newJoltages.all { it >= 0 }) {
                val added = cache.add(newJoltages)
                if (added)
                    queue.offer(State(newJoltages, depth + 1))
            }
        }
    }
}

fun applyButtonsToJoltage(joltages: List<Int>, action: List<Int>): List<Int> {
    return joltages.mapIndexed { index, i -> if (index in action) i - 1 else i }
}


