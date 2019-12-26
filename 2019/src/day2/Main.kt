package day2

import common.fromClasspathFileToLines
import kotlin.test.assertEquals


fun main() {
    runTests()

    val memory = "day2/input.txt".fromClasspathFileToLines()
        .asSequence()
        .map { it.split(',') }.flatten()
        .map(String::trim).filter(String::isNotEmpty)
        .map(String::toInt)
        .toList().toIntArray()

    val memoryAtPointOfCrash = memory.setup(12, 2)
    val memoryAfterRun = runProgram(memoryAtPointOfCrash)
    println("Part 1: Memory Address 0 = ${memoryAfterRun[0]}")


    val target = 19690720

    val (noun, verb) =
        (0..99).flatMap { noun ->
            (0..99).mapNotNull { verb ->
                val finalMemory = runProgram(memory.setup(noun, verb))
                if (finalMemory[0] == target) (noun to verb) else null
            }
        }.first()

    println("Part 2: noun = $noun, verb = $verb, 100*noun+verb = ${100 * noun + verb}")

}

private fun IntArray.setup(noun: Int, verb: Int) = this.clone().apply { this[1] = noun; this[2] = verb }
private fun IntArray.withValue(address: Int, value: Int) = this.clone().apply { this[address] = value }

private tailrec fun runProgram(memory: IntArray, ip: Int = 0): IntArray =
    when (memory[ip]) {
        1 -> runProgram( // ADD
            memory.withValue(memory[ip + 3], memory[memory[ip + 1]] + memory[memory[ip + 2]]),
            ip + 4
        )
        2 -> runProgram( // MULTIPLY
            memory.withValue(memory[ip + 3], memory[memory[ip + 1]] * memory[memory[ip + 2]]),
            ip + 4
        )
        99 -> memory // HALT
        else -> throw Exception("Invalid opcode")
    }


private fun runTests() {
    assertEquals(listOf(2, 0, 0, 0, 99), runProgram(intArrayOf(1, 0, 0, 0, 99)).toList())
    assertEquals(listOf(2, 3, 0, 6, 99), runProgram(intArrayOf(2, 3, 0, 3, 99)).toList())
    assertEquals(listOf(2, 4, 4, 5, 99, 9801), runProgram(intArrayOf(2, 4, 4, 5, 99, 0)).toList())
    assertEquals(listOf(30, 1, 1, 4, 2, 5, 6, 0, 99), runProgram(intArrayOf(1, 1, 1, 4, 99, 5, 6, 0, 99)).toList())
}
