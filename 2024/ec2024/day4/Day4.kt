package ec2024.day4

import common.ThreePartChallenge
import common.benchmark
import common.fromClasspathFileToLines
import kotlin.math.absoluteValue
import kotlin.test.assertEquals

private const val rootFolder = "ec2024/day4"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day4.assertPart1Correct()
    Day4.assertPart2Correct()
    Day4.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 1.94µs
    benchmark { part2(puzzle2Input) } // 7.1µs
    benchmark(100) { part3(puzzle3Input) } // 2.2ms
}

internal object Day4 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(10, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(81, it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(10, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(894741, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(8, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(125159663, it) }
    }
}


private fun part1(input: List<String>): Int =
    input.map(String::toInt).let {
        it.sum() - it.min() * it.size
    }

private fun part2(input: List<String>): Int = part1(input)

private fun part3(input: List<String>): Int {
    input.map(String::toInt).let { list ->
        // val (lowest, highest) = list.min() to list.max()
        // the lowest point seems to be somewhere close to the median values
        val (lowest, highest) = list.sorted().subList(list.size / 2 - 1, list.size / 2 + 1)
        return (lowest..highest).minOf { target ->
            list.sumOf { (it - target).absoluteValue }
        }
    }
}
