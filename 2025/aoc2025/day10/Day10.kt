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
        // This means "toggling" lights using a button is just `lights xor button`, which is very fast.  The bit counting
        // tools that java exposes all work from Long, so using Int would require extra conversions.
        val lights = line.substring(1, firstSpace - 1).sumOfIndexedLong { index, light -> if (light == '#') pow2L(index) else 0L }
        val buttonMasks = line.splitMappingRanges(" ", firstSpace + 1, lastSpace - 1) { _, start, end ->
            line.splitToInts(",", start + 1, end).sumOf { lights -> pow2L(lights) }
        }.distinct()
        searchButtonPresses(lights, buttonMasks.toLongArray())
    }

// A button is either used once, or not at all.  (Any multiple of 2 pushes of the same button will cancel out, so all even numbers
// of pushes cause no net change, and odd numbers over 1 reduce to the net change of a single button push, i.e. 1+2n => 1.)
// Given this, you can represent all combinations of N buttons as the binary representation of the numbers up to 2^N.
private fun searchButtonPresses(targetStates: BitSet, actions: LongArray): Int =
    (0L..<pow2L(actions.size)).minOf { combination ->
        var states = targetStates
        combination.forEach { buttonMask -> states = states xor actions[buttonMask.countTrailingZeroBits()] }
        if (states == 0L) combination.countOneBits()
        else Int.MAX_VALUE
    }


private fun part2(input: List<String>): Int =
    runBlocking(Dispatchers.Default) {
        input.map { line ->
            async {
                val firstSpace = line.indexOf(' ', 3)
                val lastSpace = line.lastIndexOf(' ', line.lastIndex - 3)
                val targetJoltages = line.splitToInts(",", lastSpace + 2, line.lastIndex - 1)
                val actions = line.splitMappingRanges(" ", firstSpace + 1, lastSpace - 1) { _, start, end ->
                    line.splitToInts(",", start + 1, end)
                }.distinct()
                searchButtonPressesForJoltage(targetJoltages, actions)
            }
        }.awaitAll().sum()
    }

private fun searchButtonPressesForJoltage(targetStates: List<Int>, buttons: List<List<Int>>): Int {
    // A few things going on here:
    //  - First, remove any fixed variables from the problem, e.g. if only a single button can impact one target, that button MUST be pressed exactly that many
    //    times.  Given the button has been pressed the correct number of times, you can remove the button and the (now zero) target state, which shrinks some
    //    of the inputs significantly.
    //  - Second, divide and conquer: if you end up with an even number for every target state, the route to that target state will be exactly twice as long
    //    as the route to the target state with halved values.  This cuts out half of the search space beneath it every time it happens.
    //  - Third, you can persuade an all-even target using part 1, which made us all build an algorithm to determine the smallest number of buttons pushed to
    //    reach a given parity (which you can set to be all the odd target states.)

    // Part 1: Remove fixed variables
    val (buttons, targetStates, previousPushes) = removeImmediatelyObviousFixedOutcomes(buttons, targetStates)

    val buttonMasks = buttons.map { it.sumOf { i -> pow2L(i) } }
    // Building a cache of the possible button push combinations turns out to be much faster than computing on demand.
    val combinedButtonMasks =
        LongArray(pow2L(buttonMasks.size).toInt()) { combination ->
            var states = 0L; combination.toLong().forEach { buttonMask -> states = states xor buttonMasks[buttonMask.countTrailingZeroBits()] }; states
        }

    val cache = HashMap<List<Int>, Int>()
    fun pushesNeededToReachAll0s(targetStates: List<Int>): Int {
        cache[targetStates]?.also { return it }
        // Recursion Base Case - all-0s requires
        if (targetStates.all { it == 0 }) return 0

        // Find which combinations of button pushes will cause an all-even state once pushed:
        //  - base case: 0,0,0,...,0 has even parity
        //  - pushing a button once flips the parity of every state it lists; pushing twice leaves all parities unaltered
        //  - so to eventually arrive at all-0s, you MUST flip the parity of any odd states.
        // It's worth noting the 0 combination is also a valid option, representing the situation where all states are already even.  You could instead
        // detect states divisible by any power of two.  (I failed with this approach due to the complexity of handling partially-0 states.)
        val targetParities = targetStates.sumOfIndexed(0L) { index, i -> if (i % 2 == 1) pow2L(index) else 0L }
        val combinations = (0L..<pow2L(buttonMasks.size)).mapNotNull { combination ->
            if (targetParities xor combinedButtonMasks[combination.toInt()] == 0L) combination else null
        }

        // A nice side effect, if you can't find any combinations, then there are no solutions beneath this point in the search tree
        return if (combinations.isEmpty()) Int.MAX_VALUE.also { cache[targetStates] = it }
        // and only the combinations that reduce to all-even states are solvable, so those are the only ones that need searching.
        else combinations.minOf { combination ->
            var states = targetStates
            combination.forEach { buttonMask -> states = applyButtonToState(states, buttons[buttonMask.countTrailingZeroBits()]) }
            // At this point, states will be all-even, but not necessarily valid: pushing the buttons to fix the parity might have over-joltaged the system.
            if (states.any { it < 0 }) return@minOf Int.MAX_VALUE

            val shortestHalfSequence = pushesNeededToReachAll0s(states.map { it / 2 })

            if (shortestHalfSequence == Int.MAX_VALUE) Int.MAX_VALUE
            // button pushes required to reach even parity, plus two lots of the sequence derived to reach half the target.
            else combination.countOneBits() + 2 * shortestHalfSequence
        }.also { cache[targetStates] = it }
    }

    return previousPushes + pushesNeededToReachAll0s(targetStates)
}

private fun applyButtonToState(targetJoltages: List<Int>, button: List<Int>): List<Int> =
    targetJoltages.mapIndexedTo(ArrayList(targetJoltages.size)) { index, i -> if (index in button) i - 1 else i }

private tailrec fun removeImmediatelyObviousFixedOutcomes(
    buttons: List<List<Int>>,
    targetJoltageStates: List<Int>,
    pushesFromPastSimplifications: Int = 0,
): Triple<List<List<Int>>, List<Int>, Int> {
    // If a state with value X is only mutated by a single button, that button MUST be pushed exactly X times.
    // After this happens, the button must never be pushed again, so you can remove the button and shrink the search space.
    val solvedIndex = targetJoltageStates.indices.firstOrNull { index ->
        1 == buttons.count { button -> index in button }
    } ?: return Triple(buttons, targetJoltageStates, pushesFromPastSimplifications)

    val solvedButtonIndex = buttons.indexOfFirst { solvedIndex in it }
    val solvedButton = buttons[solvedButtonIndex]
    val pushesSpent = targetJoltageStates[solvedIndex]

    return removeImmediatelyObviousFixedOutcomes(
        buttons.mapIndexedNotNull { index, button ->
            if (index == solvedButtonIndex) null
            else button.map {
                if (it < solvedIndex) it
                else it - 1 // this also includes (it == solvedIndex), but that only appears in one button, and it will never be these
            }
        },
        targetJoltageStates.mapIndexedNotNull { index, i ->
            // A micro-optimisation, you can also remove the fully-satisfied joltage state, which will now always be 0.
            when (index) {
                solvedIndex -> null
                in solvedButton -> i - pushesSpent
                else -> i
            }
        },
        pushesFromPastSimplifications + pushesSpent
    )
}
