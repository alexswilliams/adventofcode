package aoc2025.day11

import common.*

private val example = loadFilesToLines("aoc2025/day11", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("aoc2025/day11", "input.txt").single()

internal fun main() {
    Day11.assertCorrect()
    benchmark { part1(puzzle) } // 117.9µs
    benchmark { part2(puzzle) } // 274.9µs
}

internal object Day11 : Challenge {
    override fun assertCorrect() {
        check(5, "P1 Example") { part1(example[0]) }
        check(791, "P1 Puzzle") { part1(puzzle) }

        check(2, "P2 Example") { part2(example[1]) }
        check(520476725037672, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val mappings = input.associate { line -> line.split(": ").let { it[0] to it[1].split(' ') } }

    // cache not actually needed, it still completes in 150µs without, but cache does speed things up a little
    val cache = mutableMapOf<String, Int>()
    fun followMappingsToOut(current: String): Int {
        cache[current]?.also { return it }
        if (current == "out") return 1
        return mappings[current]!!.sumOf {
            followMappingsToOut(it)
        }.also { cache[current] = it }
    }
    return followMappingsToOut("you")
}

private fun part2(input: List<String>): Long {
    val mappings = input.associate { line -> line.split(": ").let { it[0] to it[1].split(' ') } }

    data class Cache(val string: String, val seenDac: Boolean, val seenFft: Boolean)

    val cache = mutableMapOf<Cache, Long>()
    fun followMappingsToOutViaDacAndFft(current: String, seenDac: Boolean = false, seenFft: Boolean = false): Long {
        cache[Cache(current, seenDac, seenFft)]?.also { return it }
        if (current == "out")
            return if (seenDac && seenFft) 1 else 0
        val newSeenDac = seenDac || current == "dac"
        val newSeenFft = seenFft || current == "fft"
        return mappings[current]!!.sumOf {
            followMappingsToOutViaDacAndFft(it, newSeenDac, newSeenFft)
        }.also { cache[Cache(current, newSeenDac, newSeenFft)] = it }
    }
    return followMappingsToOutViaDacAndFft("svr")
}

