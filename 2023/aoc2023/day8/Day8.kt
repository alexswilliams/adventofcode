package aoc2023.day8

import common.*

private val examples = loadFilesToLines("aoc2023/day8", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("aoc2023/day8", "input.txt")

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzles[0]) } // 205µs
    benchmark { part2(puzzles[0]) } // 1.2ms
}

internal object Day8 : Challenge {
    override fun assertCorrect() {
        check(2, "P1 Example 1") { part1(examples[0]) }
        check(6, "P1 Example 2") { part1(examples[1]) }
        check(12361, "P1 Puzzle") { part1(puzzles[0]) }

        check(6, "P2 Example 3") { part2(examples[2]) }
        check(18215611419223L, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private fun part1(input: List<String>): Int {
    val patternTemplate = input.first()
    val mappings = input.drop(2).associate { it.substring(0, 3) to (it.substring(7, 10) to it.substring(12, 15)) }
    return stepsToTerminalState(mappings, patternTemplate, "AAA")
}

private fun part2(input: List<String>): Long {
    val patternTemplate = input.first()
    val mappings = input.drop(2).associate { it.substring(0, 3) to (it.substring(7, 10) to it.substring(12, 15)) }
    val starts = mappings.keys.filter { it.endsWith('A') }

    // val cycles = starts.map { findCycle(mappings, patternTemplate, it) }
    // Observation: the end state always happens at the end of the cycle.
    // Cycle(startStep=3, length=20777, endState=20777), A
    // Cycle(startStep=5, length=19199, endState=19199), B
    // Cycle(startStep=4, length=18673, endState=18673), C
    // Cycle(startStep=2, length=16043, endState=16043), D
    // Cycle(startStep=4, length=12361, endState=12361), E
    // Cycle(startStep=2, length=15517, endState=15517)  F

    // So it should just be LCM
    val lcm = lcm(starts.map { stepsToTerminalState(mappings, patternTemplate, it).toLong() })
    return lcm
}


private fun stepsToTerminalState(mappings: Map<String, Pair<String, String>>, patternTemplate: String, start: String): Int {
    var current = start
    var steps = 0
    patternTemplate.cyclicIterator().forEach { direction ->
        val (left, right) = mappings[current]!!
        current = if (direction == 'L') left else right
        steps++
        if (current.endsWith('Z')) return steps
    }
    throw Exception("Can never reach this point")
}

//private data class Cycle(val startStep: Int, val length: Int)
//private fun findCycle(mappings: Map<String, Pair<String, String>>, patternTemplate: String, start: String): Cycle {
//    val alreadyVisited = mutableMapOf((start to 0) to 0)
//    var steps = 0
//    var current = start
//    patternTemplate.cyclicIteratorIndexed().forEach { (patternIndex, direction) ->
//        steps++
//        val (left, right) = mappings[current]!!
//        current = if (direction == 'L') left else right
//
//        val seen = alreadyVisited[current to patternIndex]
//        if (seen != null) return Cycle(seen, steps - seen)
//        alreadyVisited[current to patternIndex] = steps
//    }
//    throw Exception("Can never reach this point")
//}
