package ec2024.day7

import common.*
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
    Day7.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 27µs
    benchmark { part2(puzzle2Input) } // 170µs
    benchmark(1) { part3(puzzle3Input) } // 13s
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


private fun part1(input: List<String>): String {
    data class State(val player: Char, val currentPower: Int, val cumulativePower: Int)

    val ruleset: Map<Char, Iterator<Char>> = input.associate { Pair(it[0], it.drop(2).replace(",", "").cyclicIterator()) }
    var state = ruleset.keys.map { knight -> State(knight, 10, 0) }
    (1..10).forEach { i ->
        state = state.map { (squire, currentPower, cumulativePower) ->
            val nextMove = ruleset[squire]!!.next()
            val newPower = deriveNextPower('=', currentPower, nextMove)
            State(squire, newPower, cumulativePower + newPower)
        }
    }
    return state.sortedByDescending { it.cumulativePower }.map { it.player }.joinToString("")
}

private fun part2(input: List<String>): String {
    val track = input.takeLastWhile { it.isNotBlank() }.let { original ->
        val transposed = original.transposeToStrings()
        original.first().drop(1) +
                transposed.last().drop(1) +
                original.last().reversed().drop(1) +
                transposed.first().reversed().drop(1)
    }.repeat(10)

    val knightsToRules = input.takeWhile { it.isNotBlank() }.map { it[0] to it.drop(2).replace(",", "") }
    val ruleset = knightsToRules.map { it.second.cyclicIterator() }
    val cumulativePowers = runRace(ruleset, track)
    return cumulativePowers.withIndex().sortedByDescending { it.value }.map { knightsToRules[it.index].first }.joinToString("")
}

private fun part3(input: List<String>): Int {
    val track = ("+=+++===-+++++=-==+--+=+===-++=====+--===++=-==+=++====-==-===+=+=--==++=+========" +
            "-=======++--+++=-++=-+=+==-=++=--+=-====++--+=-==++======+=++=-+==+=-==++=-=-=--" +
            "-++=-=++==++===--==+===++===---+++==++=+=-=====+==++===--==-==+++==+++=++=+===--" +
            "==++--===+=====-=++====-+=-+--=+++=-+-===++====+++--=++====+=-=+===+=====-+++=+=" +
            "=++++==----=+=+=-S").repeat(2024)

    fun combinations(prefix: String = "", plus: Int = 5, minus: Int = 3, equals: Int = 3, acc: MutableList<String> = arrayListOf()): List<String> {
        if (plus == 0 && minus == 0 && equals == 0) acc.add(prefix)
        if (plus > 0) combinations("$prefix+", plus - 1, minus, equals, acc)
        if (minus > 0) combinations("$prefix-", plus, minus - 1, equals, acc)
        if (equals > 0) combinations("$prefix=", plus, minus, equals - 1, acc)
        return acc
    }

    val ruleset = combinations().plusElement(input.first().drop(2).replace(",", "")).map { it.cyclicIterator() }
    val cumulativePowers = runRace(ruleset, track)
    val competitorScore = cumulativePowers.last()
    return cumulativePowers.count { it > competitorScore }
}


private fun runRace(ruleset: List<Iterator<Char>>, track: String): IntArray {
    val currentPowers = IntArray(ruleset.size) { 10 }
    val cumulativePowers = IntArray(ruleset.size) { 0 }
    track.forEach { override ->
        currentPowers.forEachIndexed { i, currentPower ->
            val newPower = deriveNextPower(override, currentPower, ruleset[i].next())
            currentPowers[i] = newPower
            cumulativePowers[i] += newPower
        }
    }
    return cumulativePowers
}

private fun deriveNextPower(override: Char, currentPower: Int, nextMove: Char): Int =
    when (override) {
        '+' -> currentPower + 1
        '-' -> currentPower - 1
        else -> when (nextMove) {
            '+' -> currentPower + 1
            '-' -> currentPower - 1
            else -> currentPower
        }
    }.coerceAtLeast(0)
