package ec2024.day7

import common.*
import kotlinx.coroutines.*

private val examples = loadFilesToLines("ec2024/day7", "example.txt", "example2.txt")
private val puzzles = loadFilesToLines("ec2024/day7", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day7.assertCorrect()
    benchmark { part1(puzzles[0]) } // 27µs
    benchmark { part2(puzzles[1]) } // 119µs
    benchmark(10) { part3(puzzles[2]) } // 19.3ms
}

internal object Day7 : Challenge {
    override fun assertCorrect() {
        check("BDCA", "P1 Example") { part1(examples[0]) }
        check("JGAKDFEHI", "P1 Puzzle") { part1(puzzles[0]) }

        check("DCBA", "P2 Example") { part2(examples[1]) }
        check("EABCIFJGH", "P2 Puzzle") { part2(puzzles[1]) }

        check(5749, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String {
    data class State(val player: Char, val currentPower: Int, val cumulativePower: Int)

    val ruleset: Map<Char, Iterator<Char>> = input.associate { Pair(it[0], it.drop(2).replace(",", "").cyclicIterator()) }
    var state = ruleset.keys.map { knight -> State(knight, 10, 0) }
    repeat(10) {
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
            "=++++==----=+=+=-S")
//        .repeat(2024) // total length is a multiple of 11 :eyes:
        .repeat(11)

    fun combinations(prefix: String = "", plus: Int = 5, minus: Int = 3, equals: Int = 3, acc: MutableList<String> = arrayListOf()): List<String> {
        if (plus == 0 && minus == 0 && equals == 0) acc.add(prefix)
        if (plus > 0) combinations("$prefix+", plus - 1, minus, equals, acc)
        if (minus > 0) combinations("$prefix-", plus, minus - 1, equals, acc)
        if (equals > 0) combinations("$prefix=", plus, minus, equals - 1, acc)
        return acc
    }

    val ruleset = combinations().plusElement(input.first().drop(2).replace(",", "")).map { it.cyclicIterator() }
    val cumulativePowers = runRaceParallel(ruleset, track)
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

private fun runRaceParallel(ruleset: List<Iterator<Char>>, track: String): IntArray {
    return ruleset.chunked((ruleset.size / Runtime.getRuntime().availableProcessors()).coerceAtLeast(1)).let { sets ->
        runBlocking(Dispatchers.Default) {
            sets.map { ruleSubSet ->
                async {
                    runRace(ruleSubSet, track)
                }
            }.awaitAll()
        }
    }.reduce { acc, it -> acc + it }
}

private fun deriveNextPower(override: Char, currentPower: Int, nextMove: Char): Int =
    when (override) {
        '+' -> currentPower + 1
        '-' -> (currentPower - 1).coerceAtLeast(0)
        else -> when (nextMove) {
            '+' -> currentPower + 1
            '-' -> (currentPower - 1).coerceAtLeast(0)
            else -> currentPower
        }
    }
