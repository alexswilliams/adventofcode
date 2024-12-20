package aoc2024.day17

import common.*

private val examples = loadFilesToLines("aoc2024/day17", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("aoc2024/day17", "input.txt").single()

internal fun main() {
    Day17.assertCorrect()
    benchmark { part1(puzzle) } // 6.5µs
    benchmark { part2(puzzle) } // 231.2µs
}

internal object Day17 : Challenge {
    override fun assertCorrect() {
        check("4,6,3,5,6,3,5,2,1,0", "P1 Example 1") { part1(examples[0]) }
        check("3,6,3,7,0,7,0,3,0", "P1 Puzzle") { part1(puzzle) }

        check(117440, "P2 Example 2") { part2(examples[1]) }
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

private fun part2(input: List<String>): Long {
    val program = input[4].substring(9).splitToInts(",")
    val tails = program.indices.map { program.takeLast(it + 1) }
    val width = program.chunked(2).single { it[0] == 0 }[1] // look for the "a = a shl ?" operation to work out the step size - likely always 3 but :shrug:

    fun findQuines(previous: Long, depth: Int): List<Long> =
        if (depth == program.size)
            listOf(previous)
        else {
            (0b000L..<(1 shl width))
                .map { it xor (previous shl width) }
                .filter { newA -> tails[depth] == runMachine(newA, 0, 0, program) }
                .flatMap { findQuines(it, depth + 1) }
        }

    return findQuines(0, 0).min()
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
