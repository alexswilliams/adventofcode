package ec2024.dayX

import common.*
import kotlin.test.*

private const val rootFolder = "ec2024/dayX"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example2Input = "$rootFolder/example2.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    DayX.assertPart1Correct()
    DayX.assertPart2Correct()
    DayX.assertPart3Correct()
    benchmark { part1(puzzleInput) } //
    benchmark { part2(puzzle2Input) } //
    benchmark { part3(puzzle3Input) } //
}

internal object DayX : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(0, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(0, it) }
    }

    override fun assertPart2Correct() {
        part2(example2Input).also { println("[Example] Part 2: $it") }.also { assertEquals(0, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(0, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(0, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(0, it) }
    }
}


private fun part1(input: List<String>): Int = 0

private fun part2(input: List<String>): Int = 0

private fun part3(input: List<String>): Int = 0
