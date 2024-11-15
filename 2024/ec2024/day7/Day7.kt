package ec2024.day7

import common.ThreePartChallenge
import common.benchmark
import common.cyclicIterator
import common.fromClasspathFileToLines
import kotlin.test.assertEquals

private const val rootFolder = "ec2024/day7"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example2Input = "$rootFolder/example2.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day7.assertPart1Correct()
    Day7.assertPart2Correct()
//    Day7.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 27µs
    benchmark { part2(puzzle2Input) } // 499µs
//    benchmark { part3(puzzle3Input) } //
}

internal object Day7 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals("BDCA", it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals("JGAKDFEHI", it) }
    }

    override fun assertPart2Correct() {
        part2(example2Input).also { println("[Example] Part 2: $it") }.also { assertEquals("DCBA", it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals("EABCIFJGH", it) }
    }

    override fun assertPart3Correct() {
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(5749, it) }
    }
}


data class State(val player: Char, val currentPower: Int, val cumulativePower: Int)

private fun part1(input: List<String>): String {
    val ruleset: Map<Char, Iterator<Char>> = input.associate { Pair(it[0], it.drop(2).replace(",", "").cyclicIterator()) }

    fun iterate(state: List<State>): List<State> {
        return state.map { (squire, currentPower, cumulativePower) ->
            val nextMove = ruleset[squire]!!.next()
            val newPower = when (nextMove) {
                '+' -> currentPower + 1
                '-' -> currentPower - 1
                else -> currentPower
            }.coerceAtLeast(0)
            State(squire, newPower, cumulativePower + newPower)
        }
    }

    var state = ruleset.keys.map { knight -> State(knight, 10, 0) }
    (1..10).forEach { i ->
        state = iterate(state)
    }
    return state.sortedByDescending { it.cumulativePower }.map { it.player }.joinToString("")
}

private fun part2(input: List<String>): String {
    val ruleset = input.takeWhile { it.isNotBlank() }.associate { it[0] to it.drop(2).replace(",", "").cyclicIterator() }
    val track = "-=++=-==++=++=-=+=-=+=+=--=-=++=-==++=-+=-=+=-=+=+=++=-+==++=++=-=-=--" +
            "-=++==--" +
            "--==++++==+=+++-=+=-=+=-+-=+-=+-=+=-=+=--=+++=++=+++==++==--=+=++==+++".reversed() +
            "-=+=+=-S"
    val loopLength = track.length
    val trackRuleset = track.cyclicIterator()

    fun iterate(state: List<State>): List<State> {
        val override = trackRuleset.next()
        return state.map { (knight, currentPower, cumulativePower) ->
            val nextMove = ruleset[knight]!!.next()
            val newPower = when (override) {
                '+' -> currentPower + 1
                '-' -> currentPower - 1
                else -> when (nextMove) {
                    '+' -> currentPower + 1
                    '-' -> currentPower - 1
                    else -> currentPower
                }
            }.coerceAtLeast(0)
            State(knight, newPower, cumulativePower + newPower)
        }
    }

    var state = ruleset.keys.map { knight -> State(knight, 10, 0) }
    (1..10 * loopLength).forEach { i ->
        state = iterate(state)
    }

    return state.sortedByDescending { it.cumulativePower }.map { it.player }.joinToString("")
}

private fun part3(input: List<String>): Int {
    val track = "+=+++===-+++++=-==+--+=+===-++=====+--===++=-==+=++====-==-===+=+=--==++=+========" +
            "-=======++--+++=-++=-+=+==-=++=--+=-====++--+=-==++======+=++=-+==+=-==++=-=-=--" +
            "-++=-=++==++===--==+===++===---+++==++=+=-=====+==++===--==-==+++==+++=++=+===--" +
            "==++--===+=====-=++====-+=-+--=+++=-+-===++====+++--=++====+=-=+===+=====-+++=+=" +
            "=++++==----=+=+=-S"
    val loopLength = track.length
    val trackRuleset = track.cyclicIterator()

    fun combinations(prefix: String = "", plus: Int = 5, minus: Int = 3, equals: Int = 3, acc: MutableList<String> = arrayListOf()): List<String> {
        if (plus == 0 && minus == 0 && equals == 0) acc.add(prefix)
        if (plus > 0) combinations("$prefix+", plus - 1, minus, equals, acc)
        if (minus > 0) combinations("$prefix-", plus, minus - 1, equals, acc)
        if (equals > 0) combinations("$prefix=", plus, minus, equals - 1, acc)
        return acc
    }

    val allCombinations = combinations()
    val competitorPlan = input.first().drop(2).replace(",", "")
    val ruleset = allCombinations.mapIndexed { index, string -> index to string.cyclicIterator() }.plus(-1 to competitorPlan.cyclicIterator()).toMap()

    data class State(val player: Int, val currentPower: Int, val cumulativePower: Int)

    fun iterate(state: List<State>): List<State> {
        val override = trackRuleset.next()
        return state.map { (knight, currentPower, cumulativePower) ->
            val nextMove = ruleset[knight]!!.next()
            val newPower = when (override) {
                '+' -> currentPower + 1
                '-' -> currentPower - 1
                else -> when (nextMove) {
                    '+' -> currentPower + 1
                    '-' -> currentPower - 1
                    else -> currentPower
                }
            }.coerceAtLeast(0)
            State(knight, newPower, cumulativePower + newPower)
        }
    }

    var state = ruleset.keys.map { player -> State(player, 10, 0) }
    (1..2024 * loopLength).forEach { i ->
        state = iterate(state)
    }

    val competitorScore = state.first { it.player == -1 }.cumulativePower
    return state.count { it.cumulativePower > competitorScore }
}
