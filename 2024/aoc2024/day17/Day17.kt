package aoc2024.day17

import common.*
import kotlin.test.*

private val example = loadFilesToLines("aoc2024/day17", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day17", "input.txt").single()

internal fun main() {
    Day17.assertCorrect()
    benchmark { part1(puzzle) } // 6.7µs
    benchmark { part2(puzzle) } // 235.1µs
}

internal object Day17 : Challenge {
    override fun assertCorrect() {
        check("4,6,3,5,6,3,5,2,1,0", "P1 Example") { part1(example) }
        check("3,6,3,7,0,7,0,3,0", "P1 Puzzle") { part1(puzzle) }
        assertEquals(part1(puzzle), part1Interpreted(puzzle))

//        check(117440, "P2 Example") { part2(example) }
        check(136904920099226L, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): String {
    val a = input[0].toLongFromIndex(12)
    val b = input[1].toLongFromIndex(12)
    val c = input[2].toLongFromIndex(12)
    val program = input[4].substring(9).splitToInts(",")

    return runMachine(a, b, c, program).joinToString(",")
}

private fun part1Interpreted(input: List<String>): String {
    var a = input[0].toIntFromIndex(12)
    val output = mutableListOf<Int>()
    while (a > 0) {
        output.add(puzzleStep(a))
        a = a shr 3
    }
    return output.joinToString(",")
}

private fun puzzleStep(a: Int) = (a shr ((a and 0b111) xor 0b101)) xor (a and 0b111) xor 0b011 and 0b111

private fun part2(input: List<String>): Long {
    val program = input[4].substring(9).splitToInts(",")

    fun test(previous: Long, depth: Int): List<Long> {
        if (depth == program.size) {
            return listOf(previous)
        } else {
            val mask = previous shl 3
            val possibleNextStates = (0b000L..0b111L)
                .map { it xor mask }
                .filter { newA -> runMachine(newA, 0, 0, program) == program.takeLast(depth + 1) }
            return possibleNextStates.flatMap { test(it, depth + 1) }
        }
    }

    val solutions = test(0, 0)
//    assertEquals(program, runMachine(solutions.min(), 0, 0, program))
    return solutions.min()
}


private fun runMachine(a: Long, b: Long, c: Long, program: List<Int>): List<Int> {
    var regA = a
    var regB = b
    var regC = c
    var pc = 0
    val output = mutableListOf<Int>()

    fun comboOf(operand: Int): Long = when (operand) {
        0, 1, 2, 3 -> operand.toLong()
        4 -> regA
        5 -> regB
        6 -> regC
        7 -> error("Reserved operand 7")
        else -> error("Invalid operand $operand")
    }

    while (pc < program.size) {
        val operand = program[pc + 1]
        val combo = comboOf(operand)
        when (program[pc]) {
            0 -> regA = regA shr combo.toInt()
            1 -> regB = regB xor operand.toLong()
            2 -> regB = combo and 0x7
            3 -> pc = if (regA == 0L) pc else (operand - 2)
            4 -> regB = regC xor regB
            5 -> output.add((combo and 0x7).toInt())
            6 -> regB = regA shr combo.toInt()
            7 -> regC = regA shr combo.toInt()
        }
        pc += 2
    }

    return output
}
