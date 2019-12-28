package day9

import common.fromClasspathFileToLongProgram
import kotlin.test.assertEquals
import kotlin.test.assertTrue


fun main() {
    runTests()

    val memory = "day9/input.txt".fromClasspathFileToLongProgram()

    val diagnostic = runProgram(RunState(memory, listOf(1)))
    println("Part 1: diagnostic code = " + diagnostic.outputs.last())
    assertEquals(3380552333, diagnostic.outputs.last())
    assertTrue(diagnostic.outputs.dropLast(1).isEmpty())

    val sensorBoost = runProgram(RunState(memory, listOf(2)))
    println("Part 2: sensor boost = " + sensorBoost.outputs.last())
    assertEquals(78831, sensorBoost.outputs.last())
}


@Suppress("ArrayInDataClass")
private data class RunState(
    val memory: LongArray,
    val inputs: List<Long> = emptyList(),
    val outputs: List<Long> = emptyList(),
    val ip: Int = 0,
    val state: MachineState = MachineState.RUNNING,
    val base: Int = 0,
    val sparseMem: Map<Int, Long> = emptyMap()
)

private tailrec fun runProgram(state: RunState): RunState {
    val (memory, inputs, outputs, ip) = state
    val (opcode, modes) = decodeInstruction(memory[ip])
    return when (opcode) {
        Opcode.ADD -> runProgram(
            state.updateMemory(
                state.paramAddress(3, modes[2]),
                state.paramValue(1, modes[0])
                        + state.paramValue(2, modes[1])
            ).copy(ip = ip + 4)
        )
        Opcode.MULTIPLY -> runProgram(
            state.updateMemory(
                state.paramAddress(3, modes[2]),
                state.paramValue(1, modes[0])
                        * state.paramValue(2, modes[1])
            ).copy(ip = ip + 4)
        )
        Opcode.INPUT ->
            if (inputs.isEmpty())
                state.copy(state = MachineState.YIELDING_ON_MISSING_INPUT)
            else runProgram(
                state.updateMemory(state.paramAddress(1, modes[0]), inputs.first())
                    .copy(
                        inputs = inputs.drop(1),
                        ip = ip + 2
                    )
            )
        Opcode.OUTPUT -> {
            runProgram(
                state.copy(
                    outputs = outputs.plus(state.paramValue(1, modes[0])),
                    ip = ip + 2
                )
            )
        }
        Opcode.JUMP_IF_TRUE -> runProgram(
            state.copy(
                ip = if (state.paramValue(1, modes[0]) != 0L)
                    state.paramValue(2, modes[1]).toInt()
                else ip + 3
            )
        )
        Opcode.JUMP_IF_FALSE -> runProgram(
            state.copy(
                ip = if (state.paramValue(1, modes[0]) == 0L)
                    state.paramValue(2, modes[1]).toInt()
                else ip + 3
            )
        )
        Opcode.LESS_THAN -> runProgram(
            state.updateMemory(
                state.paramAddress(3, modes[2]),
                if (state.paramValue(1, modes[0])
                    < state.paramValue(2, modes[1])
                ) 1 else 0
            ).copy(ip = ip + 4)
        )
        Opcode.EQUALS -> runProgram(
            state.updateMemory(
                state.paramAddress(3, modes[2]),
                if (state.paramValue(1, modes[0])
                    == state.paramValue(2, modes[1])
                ) 1 else 0
            ).copy(ip = ip + 4)
        )
        Opcode.BASE_OFFSET -> runProgram(
            state.copy(
                base = state.base + state.paramValue(1, modes[0]).toInt(),
                ip = ip + 2
            )
        )
        Opcode.HALT -> state.copy(state = MachineState.HALTED, ip = ip + 1)
    }
}


private fun RunState.updateMemory(address: Int, value: Long) =
    if (address < this.memory.size)
        this.copy(memory = memory.withValue(address, value))
    else
        this.copy(sparseMem = sparseMem.plus(address to value))

private fun LongArray.withValue(address: Int, value: Long) = this.clone().apply { this[address] = value }

private fun RunState.getMemory(address: Int) =
    if (address < this.memory.size)
        this.memory[address]
    else
        this.sparseMem[address] ?: 0

private fun RunState.paramValue(param: Int, parameterMode: ParameterMode): Long =
    when (parameterMode) {
        ParameterMode.IMMEDIATE -> getMemory(ip + param)
        ParameterMode.POSITION -> getMemory(getMemory(ip + param).toInt())
        ParameterMode.RELATIVE -> getMemory(getMemory(ip + param).toInt() + base)
    }

private fun RunState.paramAddress(param: Int, parameterMode: ParameterMode): Int =
    when (parameterMode) {
        ParameterMode.IMMEDIATE -> error("Cannot find address of immediate parameter")
        ParameterMode.POSITION -> getMemory(ip + param).toInt()
        ParameterMode.RELATIVE -> getMemory(ip + param).toInt() + base
    }

private fun decodeInstruction(input: Long): Pair<Opcode, List<ParameterMode>> {
    val inputAsString = input.toString().padStart(5, '0')
    return Pair(
        opcodeMap[inputAsString.takeLast(2).toInt()] ?: error("Unknown opcode $input"),
        inputAsString.reversed().drop(2).map { modeMap[it - '0'] ?: error("Unknown mode: $input") }
    )
}

private enum class ParameterMode(val encoding: Int) {
    POSITION(0),
    IMMEDIATE(1),
    RELATIVE(2)
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
    BASE_OFFSET(9),
    HALT(99)
}

private val opcodeMap = Opcode.values().associateBy { it.encoding }

private enum class MachineState {
    RUNNING,
    YIELDING_ON_MISSING_INPUT,
    HALTED
}

private fun runTests() {
    val quine = longArrayOf(109, 1, 204, -1, 1001, 100, 1, 100, 1008, 100, 16, 101, 1006, 101, 0, 99)
    assertEquals(quine.toList(), runProgram(RunState(quine)).outputs)

    val longNumberGenerator = longArrayOf(104, 1125899906842624, 99)
    assertEquals(listOf(1125899906842624), runProgram(RunState(longNumberGenerator)).outputs)

    val sixteenDigitNumberGenerator = longArrayOf(1102, 34915192, 34915192, 7, 4, 7, 99, 0)
    assertEquals(listOf(1219070632396864), runProgram(RunState(sixteenDigitNumberGenerator)).outputs)
}
