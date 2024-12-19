package aoc2024.day17

import common.*
import kotlin.math.*
import kotlin.test.*

private val example = loadFilesToLines("aoc2024/day17", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day17", "input.txt").single()

internal fun main() {
    Day17.assertCorrect()
    benchmark { part1(puzzle) } // 13.2µs
//    benchmark(10) { part2(puzzle) }
}

internal object Day17 : Challenge {
    override fun assertCorrect() {
        check("4,6,3,5,6,3,5,2,1,0", "P1 Example") { part1(example) }
        check("3,6,3,7,0,7,0,3,0", "P1 Puzzle") { part1(puzzle) }
        assertEquals(part1(puzzle), part1Interpreted(puzzle))

//        check(117440, "P2 Example") { part2(example) }
        check(0, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): String {
    val a = input[0].toIntFromIndex(input[0].lastIndexOf(' ') + 1)
    val b = input[1].toIntFromIndex(input[1].lastIndexOf(' ') + 1)
    val c = input[2].toIntFromIndex(input[2].lastIndexOf(' ') + 1)
    val program = input[4].substringAfter(' ').splitToInts(",")

    return runMachine(a, b, c, program).joinToString(",")
}

private fun part1Interpreted(input: List<String>): String {
    var a = input[0].toIntFromIndex(input[0].lastIndexOf(' ') + 1)
    val output = mutableListOf<Int>()
    while (a > 0) {
        output.add(puzzleStep(a))
        a = a shr 3
    }
    return output.joinToString(",")
}

private fun puzzleStep(a: Int) = (a shr ((a and 0b111) xor 0b101)) xor ((a and 0b111) xor 0b011) and 0b111

private fun part2(input: List<String>): Int {
    val program = input[4].substringAfter(' ').splitToInts(",")

    fun test() {
        (0..(1 shl min(10, program.size))).forEach { i ->
            if (puzzleStep(i) == program.last()) {
                println(i)
            }
        }
    }

    test()
    return 0
}


private fun runMachine(a: Int, b: Int, c: Int, program: List<Int>): List<Int> {
    var regA = a
    var regB = b
    var regC = c
    var pc = 0
    val output = mutableListOf<Int>()

    fun comboOf(operand: Int): Int = when (operand) {
        0, 1, 2, 3 -> operand
        4 -> regA
        5 -> regB
        6 -> regC
        7 -> -1
        else -> error("Invalid operand $operand")
    }

    while (pc < program.size) {
        val operand = program[pc + 1]
        val combo = comboOf(operand)
        when (program[pc]) {
            0 -> regA = regA shr combo
            1 -> regB = regB xor operand
            2 -> regB = combo and 0x7
            3 -> pc = if (regA == 0) pc else (operand - 2)
            4 -> regB = regC xor regB
            5 -> output.add(combo and 0x7)
            6 -> regB = regA shr combo
            7 -> regC = regA shr combo
        }
        pc += 2
    }

    return output
}
