package day7

import common.fromClasspathFileToProgram
import kotlin.test.assertEquals


fun main() {
    runTests()

    val memory = "day7/input.txt".fromClasspathFileToProgram()

    val permutations01234 = setOf(0, 1, 2, 3, 4).permutations().map { it to memory.runAmplifierChain(it) }
    val maxByPhase = permutations01234.maxBy { it.second }
    println("Part 1: Best phases = ${maxByPhase?.first}; Power = ${maxByPhase?.second}")
    assertEquals(929800, maxByPhase?.second)

    val permutations56789 = setOf(5, 6, 7, 8, 9).permutations().map { it to memory.runAmplifierLoop(it) }
    val maxByPhaseLoop = permutations56789.maxBy { it.second }
    println("Part 2: Best phases = ${maxByPhaseLoop?.first}; Power = ${maxByPhaseLoop?.second}")
    assertEquals(15432220, maxByPhaseLoop?.second)
}

fun IntArray.runAmplifier(phase: Int, input: Int) = runProgram(RunState(this, listOf(phase, input))).outputs[0]

fun IntArray.runAmplifierChain(phases: List<Int>) = phases.fold(0) { acc, phase -> this.runAmplifier(phase, acc) }

fun IntArray.runAmplifierLoop(phases: List<Int>): Int {
    val initialStates = phases.map { RunState(this, listOf(it)) } to 0

    tailrec fun runUntilHalt(states: Pair<List<RunState>, Int>): Int {
        val new = states.first.fold(emptyList<RunState>() to states.second) { acc, next ->
            runProgram(next.copy(inputs = next.inputs.plus(acc.second)))
                .let { acc.first.plus(it) to it.outputs.last() }
        }
        return if (new.first.last().state == MachineState.HALTED)
            new.second
        else
            runUntilHalt(new)
    }

    return runUntilHalt(initialStates)
}


fun Set<Int>.permutations(): Set<List<Int>> = when {
    isEmpty() -> emptySet()
    size == 1 -> setOf(listOf(this.first()))
    else -> {
        this.map { it to this.minus(it).permutations() }
            .flatMap { mapping -> mapping.second.map { listOf(mapping.first) + it } }
            .toSet()
    }
}


@Suppress("ArrayInDataClass")
private data class RunState(
    val memory: IntArray,
    val inputs: List<Int> = emptyList(),
    val outputs: List<Int> = emptyList(),
    val ip: Int = 0,
    val state: MachineState = MachineState.RUNNING
)


private fun IntArray.withValue(address: Int, value: Int) = this.clone().apply { this[address] = value }

@Suppress("TAILREC_WITH_DEFAULTS") // this is a bug in kotlin < 1.4, but this function's default
// params are initialised with values which may as well be static, so not an issue.
private tailrec fun runProgram(state: RunState): RunState {
    val (memory, inputs, outputs, ip) = state
    val (opcode, modes) = decodeInstruction(memory[ip])
    return when (opcode) {
        Opcode.ADD -> runProgram(
            state.copy(
                memory = memory.withValue(
                    memory[ip + 3],
                    paramValue(memory, ip + 1, modes[0])
                            + paramValue(memory, ip + 2, modes[1])
                ),
                ip = ip + 4
            )
        )
        Opcode.MULTIPLY -> runProgram(
            state.copy(
                memory = memory.withValue(
                    memory[ip + 3],
                    paramValue(memory, ip + 1, modes[0])
                            * paramValue(memory, ip + 2, modes[1])
                ),
                ip = ip + 4
            )
        )
        Opcode.INPUT ->
            if (inputs.isEmpty())
                state.copy(state = MachineState.YIELDING_ON_MISSING_INPUT)
            else runProgram(
                state.copy(
                    memory = memory.withValue(memory[ip + 1], inputs.first()),
                    inputs = inputs.drop(1),
                    ip = ip + 2
                )
            )
        Opcode.OUTPUT -> {
            runProgram(
                state.copy(
                    outputs = outputs.plus(paramValue(memory, ip + 1, modes[0])),
                    ip = ip + 2
                )
            )
        }
        Opcode.JUMP_IF_TRUE -> runProgram(
            state.copy(
                ip = if (paramValue(memory, ip + 1, modes[0]) != 0)
                    paramValue(memory, ip + 2, modes[1])
                else ip + 3
            )
        )
        Opcode.JUMP_IF_FALSE -> runProgram(
            state.copy(
                ip = if (paramValue(memory, ip + 1, modes[0]) == 0)
                    paramValue(memory, ip + 2, modes[1])
                else ip + 3
            )
        )
        Opcode.LESS_THAN -> runProgram(
            state.copy(
                memory = memory.withValue(
                    memory[ip + 3],
                    if (paramValue(memory, ip + 1, modes[0])
                        < paramValue(memory, ip + 2, modes[1])
                    ) 1 else 0
                ),
                ip = ip + 4
            )
        )
        Opcode.EQUALS -> runProgram(
            state.copy(
                memory = memory.withValue(
                    memory[ip + 3],
                    if (paramValue(memory, ip + 1, modes[0])
                        == paramValue(memory, ip + 2, modes[1])
                    ) 1 else 0
                ),
                ip = ip + 4
            )
        )
        Opcode.HALT -> state.copy(state = MachineState.HALTED, ip = ip + 1)
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

private enum class MachineState {
    RUNNING,
    YIELDING_ON_MISSING_INPUT,
    HALTED
}

private fun runTests() {
    val program1 = intArrayOf(3, 15, 3, 16, 1002, 16, 10, 16, 1, 16, 15, 15, 4, 15, 99, 0, 0)
    assertEquals(43210, program1.runAmplifierChain(listOf(4, 3, 2, 1, 0)))

    val program2 =
        intArrayOf(3, 23, 3, 24, 1002, 24, 10, 24, 1002, 23, -1, 23, 101, 5, 23, 23, 1, 24, 23, 23, 4, 23, 99, 0, 0)
    assertEquals(54321, program2.runAmplifierChain(listOf(0, 1, 2, 3, 4)))

    val program3 = intArrayOf(
        3, 31, 3, 32, 1002, 32, 10, 32, 1001, 31, -2, 31, 1007, 31, 0, 33,
        1002, 33, 7, 33, 1, 33, 31, 31, 1, 32, 31, 31, 4, 31, 99, 0, 0, 0
    )
    assertEquals(65210, program3.runAmplifierChain(listOf(1, 0, 4, 3, 2)))

    assertEquals(emptySet(), emptySet<Int>().permutations())
    assertEquals(setOf(listOf(1)), setOf(1).permutations())
    assertEquals(setOf(listOf(1, 2), listOf(2, 1)), setOf(1, 2).permutations())
    assertEquals(
        setOf(
            listOf(1, 2, 3), listOf(1, 3, 2),
            listOf(2, 1, 3), listOf(2, 3, 1),
            listOf(3, 1, 2), listOf(3, 2, 1)
        ), setOf(1, 2, 3).permutations()
    )

    val loop1 = intArrayOf(
        3, 26, 1001, 26, -4, 26, 3, 27, 1002, 27, 2, 27, 1, 27, 26,
        27, 4, 27, 1001, 28, -1, 28, 1005, 28, 6, 99, 0, 0, 5
    )
    assertEquals(139629729, loop1.runAmplifierLoop(listOf(9, 8, 7, 6, 5)))
    val loop2 = intArrayOf(
        3, 52, 1001, 52, -5, 52, 3, 53, 1, 52, 56, 54, 1007, 54, 5, 55, 1005, 55, 26, 1001, 54,
        -5, 54, 1105, 1, 12, 1, 53, 54, 53, 1008, 54, 0, 55, 1001, 55, 1, 55, 2, 53, 55, 53, 4,
        53, 1001, 56, -1, 56, 1005, 56, 6, 99, 0, 0, 0, 0, 10
    )
    assertEquals(18216, loop2.runAmplifierLoop(listOf(9, 7, 8, 5, 6)))
}
