package aoc2025.day10

import common.*
import kotlinx.coroutines.*

private val example = loadFilesToLines("aoc2025/day10", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2025/day10", "input.txt").single()

internal fun main() {
    Day10.assertCorrect()
    benchmark { part1(puzzle) } // 780.6µs
    benchmark(100) { part2(puzzle) } // 41.0ms serial, 11.0ms parallel
}

internal object Day10 : Challenge {
    override fun assertCorrect() {
        check(7, "P1 Example") { part1(example) }
        check(502, "P1 Puzzle") { part1(puzzle) }

        check(33, "P2 Example") { part2(example) }
        check(196, "P2 Regression: start by dividing by 2") { part2(listOf("[###.##] (1,5) (0,1,2,5) (0,3,4,5) (0,1,3) (2,3,4,5) {172,166,34,166,30,60}")) }
        check(47, "P2 Regression: repeated division by 2 tends to nothing") { part2(listOf("[#...] (1,2,3) (2) (0,2) (0,1,3) (0) {34,22,38,22}")) }
        check(21467, "P2 Puzzle") { part2(puzzle) }
    }
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
    return runBlocking(Dispatchers.Default) {
        targetJoltages.zip(switchActions).map { (targetJoltages, actions) ->
            async {
                searchButtonPressesForJoltage(targetJoltages, actions)
            }
        }.awaitAll().sum()
    }
}

private fun searchButtonPressesForJoltage(targetStates: List<Int>, buttons: List<List<Int>>): Int {
    val (buttons, targetStates, previousPushes) = simplfy(buttons, targetStates)
    val buttonMasks = buttons.map { it.sumOf { i -> pow2L(i) } }
    val combinedButtonMasks =
        LongArray(pow2L(buttonMasks.size).toInt()) { combination ->
            var states = 0L; combination.toLong().forEach { buttonMask -> states = states xor buttonMasks[buttonMask.countTrailingZeroBits()] }; states
        }

    val cache = HashMap<List<Int>, Int>()
    fun divideAndConquer(targetStates: List<Int>): Int {
        cache[targetStates]?.also { return it }
        if (targetStates.all { it == 0 }) return 0
        val targetParities = targetStates.sumOfIndexed(0L) { index, i -> if (i % 2 == 1) pow2L(index) else 0L }
        val combinations = (0L..<pow2L(buttonMasks.size)).mapNotNull { combination -> if (targetParities xor combinedButtonMasks[combination.toInt()] == 0L) combination else null }

        return if (combinations.isEmpty())
            Int.MAX_VALUE.also { cache[targetStates] = it }
        else
            combinations.minOf { combination ->
                var states = targetStates
                combination.forEach { buttonMask -> states = applyButtonsToState(states, buttons[buttonMask.countTrailingZeroBits()]) }

                if (states.any { it < 0 }) return@minOf Int.MAX_VALUE
                val smallestChild = divideAndConquer(states.map { it / 2 })

                if (smallestChild == Int.MAX_VALUE) Int.MAX_VALUE
                else combination.countOneBits() + 2 * smallestChild
            }.also { cache[targetStates] = it }
    }

    return previousPushes + divideAndConquer(targetStates)
}

private fun applyButtonsToState(targetState: List<Int>, action: List<Int>): List<Int> =
    targetState.mapIndexed { index, i -> if (index in action) i - 1 else i }

private fun simplfy(
    buttons: List<List<Int>>,
    targetStates: List<Int>,
    previousDepth: Int = 0
): Triple<List<List<Int>>, List<Int>, Int> {
    var simplifiedStartState = Triple(buttons, targetStates, previousDepth)
    while (true) {
        val (buttons, targetJoltages, pushesSoFar) = simplifiedStartState
        val solvedIndex = (0..targetJoltages.lastIndex).find { buttons.count { button -> it in button } == 1 } ?: break

        val pushesNeeded = targetJoltages[solvedIndex]
        val solvedButtonIndex = buttons.indexOfFirst { solvedIndex in it }
        val solvedButton = buttons[solvedButtonIndex]
        simplifiedStartState = Triple(
            buttons.mapIndexedNotNull { index, button ->
                if (index == solvedButtonIndex) null else button.mapNotNull {
                    when {
                        it < solvedIndex -> it
                        it == solvedIndex -> null
                        else -> it - 1
                    }
                }
            },
            targetJoltages.mapIndexedNotNull { index, i ->
                when (index) {
                    solvedIndex -> null
                    in solvedButton -> i - pushesNeeded
                    else -> i
                }
            },
            pushesSoFar + pushesNeeded
        )
    }
    return simplifiedStartState
}
