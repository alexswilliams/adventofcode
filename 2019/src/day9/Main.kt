package day9

import common.RunState
import common.fromClasspathFileToLongProgram
import common.runProgram
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


private fun runTests() {
    val quine = longArrayOf(109, 1, 204, -1, 1001, 100, 1, 100, 1008, 100, 16, 101, 1006, 101, 0, 99)
    assertEquals(quine.toList(), runProgram(RunState(quine)).outputs)

    val longNumberGenerator = longArrayOf(104, 1125899906842624, 99)
    assertEquals(listOf(1125899906842624), runProgram(RunState(longNumberGenerator)).outputs)

    val sixteenDigitNumberGenerator = longArrayOf(1102, 34915192, 34915192, 7, 4, 7, 99, 0)
    assertEquals(listOf(1219070632396864), runProgram(RunState(sixteenDigitNumberGenerator)).outputs)
}
