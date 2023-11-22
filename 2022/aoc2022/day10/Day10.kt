package aoc2022.day10

import common.*
import kotlin.math.*
import kotlin.test.*

private val exampleInput = "aoc2022/day10/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day10/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_ANSWER = 13140
private val part2ExpectedAnswer = """
    ██  ██  ██  ██  ██  ██  ██  ██  ██  ██  
    ███   ███   ███   ███   ███   ███   ███ 
    ████    ████    ████    ████    ████    
    █████     █████     █████     █████     
    ██████      ██████      ██████      ████
    ███████       ███████       ███████     """.trimIndent()

fun main() {
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleInput))
    println("Part 1: " + part1(puzzleInput)) // 13140

    assertEquals(part2ExpectedAnswer, part2(exampleInput))
    println("Part 2: \n" + part2(puzzleInput)) // RFKZCPEF
}

private fun part1(input: List<String>): Int =
    valuesAfterAnyCycle(input)
        .let { values -> (20..values.size step 40).sumOf { values[it - 1] * it } }

private fun part2(input: List<String>): String =
    valuesAfterAnyCycle(input)
        .mapIndexed { cycle, x -> if ((x - (cycle % 40)).absoluteValue <= 1) '█' else ' ' }
        .windowed(size = 40, step = 40, partialWindows = false)
        .joinToString("\n") { it.joinToString("") }


// The input is conveniently set up such that:
//  - ops that take 2 cycles have 2 fields,
//  - ops that take 1 cycle have 1 field,
//  - any value affecting the register appears as the last field.
// So you can turn "noop, addx 1, addx 2" into [0 0 1 0 2] to get a list of values to add to the register after each cycle.  The value of the register
// at cycle C is (op[0] + op[1] + ... + op[C] + 1), so a running total gives the entire history of the register indexed by cycle number.
private fun valuesAfterAnyCycle(input: List<String>): List<Int> =
    input.splitOnSpaces()
        .flatMap { op -> op.map { it.toIntOrNull() ?: 0 } }
        .runningTotal(1)
