package aoc2022.day10

import common.*
import kotlin.math.*

private val example = loadFilesToLines("aoc2022/day10", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day10", "input.txt").single()

private val part2ExpectedExampleAnswer = """
    ██  ██  ██  ██  ██  ██  ██  ██  ██  ██  
    ███   ███   ███   ███   ███   ███   ███ 
    ████    ████    ████    ████    ████    
    █████     █████     █████     █████     
    ██████      ██████      ██████      ████
    ███████       ███████       ███████     """.trimIndent()
private val part2ExpectedPuzzleAnswer = """
    ███  ████ █  █ ████  ██  ███  ████ ████ 
    █  █ █    █ █     █ █  █ █  █ █    █    
    █  █ ███  ██     █  █    █  █ ███  ███  
    ███  █    █ █   █   █    ███  █    █    
    █ █  █    █ █  █    █  █ █    █    █    
    █  █ █    █  █ ████  ██  █    ████ █    """.trimIndent()


internal fun main() {
    Day10.assertCorrect()
    benchmark { part1(puzzle) } // 47.5µs
    benchmark { part2(puzzle) } // 58.2µs
}

internal object Day10 : Challenge {
    override fun assertCorrect() {
        check(13140, "P1 Example") { part1(example) }
        check(13760, "P1 Puzzle") { part1(puzzle) }

        check(part2ExpectedExampleAnswer, "P2 Example") { part2(example) }
        check(part2ExpectedPuzzleAnswer, "P2 Puzzle") { part2(puzzle) }
    }
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
