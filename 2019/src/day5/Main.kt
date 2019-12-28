package day5

import common.fromClasspathFileToProgram
import kotlin.test.assertEquals


fun main() {
    runTests()

    val memory = "day5/input.txt".fromClasspathFileToProgram()

    val airConditionerId = 1
    val (_, outputs) = runProgram(memory, listOf(airConditionerId))
    println("Part 1: Diagnostic Code = ${outputs.last()}")
    assertEquals(0, outputs.dropLast(1).sum())
    assertEquals(15426686, outputs.last())

    val thermalRadiatorControllerId = 5
    val (_, outputs2) = runProgram(memory, listOf(thermalRadiatorControllerId))
    println("Part 2: Diagnostic Code = ${outputs2.last()}")
    assertEquals(0, outputs2.dropLast(1).sum())
    assertEquals(11430197, outputs2.last())
}

private fun IntArray.withValue(address: Int, value: Int) = this.clone().apply { this[address] = value }

@Suppress("TAILREC_WITH_DEFAULTS") // this is a bug in kotlin < 1.4, but this function's default
// params are initialised with values which may as well be static, so not an issue.
private tailrec fun runProgram(
    memory: IntArray,
    inputs: List<Int> = emptyList(),
    outputs: List<Int> = emptyList(),
    ip: Int = 0
): Pair<IntArray, List<Int>> {
    val (opcode, modes) = decodeInstruction(memory[ip])
    return when (opcode) {
        Opcode.ADD -> runProgram(
            memory.withValue(
                memory[ip + 3],
                paramValue(memory, ip + 1, modes[0])
                        + paramValue(memory, ip + 2, modes[1])
            ),
            inputs,
            outputs,
            ip + 4
        )
        Opcode.MULTIPLY -> runProgram(
            memory.withValue(
                memory[ip + 3],
                paramValue(memory, ip + 1, modes[0])
                        * paramValue(memory, ip + 2, modes[1])
            ),
            inputs,
            outputs,
            ip + 4
        )
        Opcode.INPUT -> runProgram(
            memory.withValue(memory[ip + 1], inputs.first()),
            inputs.drop(1),
            outputs,
            ip + 2
        )
        Opcode.OUTPUT -> runProgram(
            memory,
            inputs,
            outputs.plus(paramValue(memory, ip + 1, modes[0])),
            ip + 2
        )
        Opcode.JUMP_IF_TRUE -> runProgram(
            memory,
            inputs,
            outputs,
            if (paramValue(memory, ip + 1, modes[0]) != 0)
                paramValue(memory, ip + 2, modes[1])
            else ip + 3
        )
        Opcode.JUMP_IF_FALSE -> runProgram(
            memory,
            inputs,
            outputs,
            if (paramValue(memory, ip + 1, modes[0]) == 0)
                paramValue(memory, ip + 2, modes[1])
            else ip + 3
        )
        Opcode.LESS_THAN -> runProgram(
            memory.withValue(
                memory[ip + 3],
                if (paramValue(memory, ip + 1, modes[0])
                    < paramValue(memory, ip + 2, modes[1])
                ) 1 else 0
            ),
            inputs,
            outputs,
            ip + 4
        )
        Opcode.EQUALS -> runProgram(
            memory.withValue(
                memory[ip + 3],
                if (paramValue(memory, ip + 1, modes[0])
                    == paramValue(memory, ip + 2, modes[1])
                ) 1 else 0
            ),
            inputs,
            outputs,
            ip + 4
        )
        Opcode.HALT -> memory to outputs
    }
}

private fun paramValue(memory: IntArray, param: Int, parameterMode: ParameterMode) = when (parameterMode) {
    ParameterMode.IMMEDIATE -> memory[param]
    ParameterMode.POSITION -> memory[memory[param]]
}

private fun decodeInstruction(input: Int): Pair<Opcode, List<ParameterMode>> {
    val inputAsString = input.toString().padStart(5, '0')
    return Pair(
        opcodeMap[inputAsString.takeLast(2).toInt()] ?: throw Exception("Unknown opcode $input"),
        inputAsString.reversed().drop(2).map { modeMap[it - '0'] ?: throw Exception("Unknown mode: $input") }
    )
}

private enum class ParameterMode(val encoding: Int) {
    POSITION(0),
    IMMEDIATE(1)
}

private val modeMap = ParameterMode.values().associateBy { it.encoding }

private enum class Opcode(val encoding: Int) {
    ADD(1),
    MULTIPLY(2),
    INPUT(3),
    OUTPUT(4),
    JUMP_IF_TRUE(5),
    JUMP_IF_FALSE(6),
    LESS_THAN(7),
    EQUALS(8),
    HALT(99)
}

private val opcodeMap = Opcode.values().associateBy { it.encoding }

private fun runTests() {
    fun Pair<IntArray, List<Int>>.forTest() = first.toList() to second
    val noOutput = emptyList<Int>()

    // For this incarnation
    assertEquals(
        Opcode.MULTIPLY to listOf(ParameterMode.POSITION, ParameterMode.IMMEDIATE, ParameterMode.POSITION),
        decodeInstruction(1002)
    )

    assertEquals(listOf(123, 0, 4, 0, 99) to listOf(123), runProgram(intArrayOf(3, 0, 4, 0, 99), listOf(123)).forTest())

    // Input == 8
    val equalPosition = intArrayOf(3, 9, 8, 9, 10, 9, 4, 9, 99, -1, 8)
    assertEquals(listOf(0), runProgram(equalPosition, listOf(10)).second)
    assertEquals(listOf(1), runProgram(equalPosition, listOf(8)).second)
    assertEquals(listOf(0), runProgram(equalPosition, listOf(4)).second)
    val equalImmediate = intArrayOf(3, 3, 1108, -1, 8, 3, 4, 3, 99)
    assertEquals(listOf(0), runProgram(equalImmediate, listOf(10)).second)
    assertEquals(listOf(1), runProgram(equalImmediate, listOf(8)).second)
    assertEquals(listOf(0), runProgram(equalImmediate, listOf(4)).second)

    // Input < 8
    val lessThanPosition = intArrayOf(3, 9, 7, 9, 10, 9, 4, 9, 99, -1, 8)
    assertEquals(listOf(0), runProgram(lessThanPosition, listOf(12)).second)
    assertEquals(listOf(0), runProgram(lessThanPosition, listOf(8)).second)
    assertEquals(listOf(1), runProgram(lessThanPosition, listOf(5)).second)
    val lessThanImmediate = intArrayOf(3, 3, 1107, -1, 8, 3, 4, 3, 99)
    assertEquals(listOf(0), runProgram(lessThanImmediate, listOf(12)).second)
    assertEquals(listOf(0), runProgram(lessThanImmediate, listOf(8)).second)
    assertEquals(listOf(1), runProgram(lessThanImmediate, listOf(5)).second)

    // Jump (Input == 0)
    val jumpPosition = intArrayOf(3, 12, 6, 12, 15, 1, 13, 14, 13, 4, 13, 99, -1, 0, 1, 9)
    assertEquals(listOf(0), runProgram(jumpPosition, listOf(0)).second)
    assertEquals(listOf(1), runProgram(jumpPosition, listOf(2)).second)
    val jumpImmediate = intArrayOf(3, 3, 1105, -1, 9, 1101, 0, 0, 12, 4, 12, 99, 1)
    assertEquals(listOf(0), runProgram(jumpImmediate, listOf(0)).second)
    assertEquals(listOf(1), runProgram(jumpImmediate, listOf(2)).second)

    // Compare (999 when <8, 1000 when ==8, 1001 when >8)
    val compareProgram = intArrayOf(
        3, 21, 1008, 21, 8, 20, 1005, 20, 22, 107, 8, 21, 20, 1006, 20, 31,
        1106, 0, 36, 98, 0, 0, 1002, 21, 125, 20, 4, 20, 1105, 1, 46, 104,
        999, 1105, 1, 46, 1101, 1000, 1, 20, 4, 20, 1105, 1, 46, 98, 99
    )
    assertEquals(listOf(999), runProgram(compareProgram, listOf(5)).second)
    assertEquals(listOf(1000), runProgram(compareProgram, listOf(8)).second)
    assertEquals(listOf(1001), runProgram(compareProgram, listOf(12)).second)


    // Tests from previous incarnation of opcode computer
    assertEquals(listOf(2, 0, 0, 0, 99) to noOutput, runProgram(intArrayOf(1, 0, 0, 0, 99)).forTest())
    assertEquals(listOf(2, 3, 0, 6, 99) to noOutput, runProgram(intArrayOf(2, 3, 0, 3, 99)).forTest())
    assertEquals(listOf(2, 4, 4, 5, 99, 9801) to noOutput, runProgram(intArrayOf(2, 4, 4, 5, 99, 0)).forTest())
    assertEquals(
        listOf(30, 1, 1, 4, 2, 5, 6, 0, 99) to noOutput,
        runProgram(intArrayOf(1, 1, 1, 4, 99, 5, 6, 0, 99)).forTest()
    )
}
