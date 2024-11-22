package ec2024.day11

import common.*
import kotlin.test.*

private const val rootFolder = "ec2024/day11"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day11.assertPart1Correct()
    Day11.assertPart2Correct()
    Day11.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 20.2µs
    benchmark { part2(puzzle2Input) } // 149µs
    benchmark(10) { part3(puzzle3Input) } // 43ms
}

internal object Day11 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(8, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(44, it) }
    }

    override fun assertPart2Correct() {
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(256061, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(268815, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(1403038233378L, it) }
    }
}


private fun part1(input: List<String>) = colonySizeFor("A", parseInput(input), 4)
private fun part2(input: List<String>) = colonySizeFor("Z", parseInput(input), 10)
private fun part3(input: List<String>) = with(parseInput(input)) {
    keys.map { start -> colonySizeFor(start, this, 20) }
}.run { max() - min() }


private fun parseInput(input: List<String>) =
    input.associate { it.substringBefore(':') to it.substringAfter(':').split(',') }

private fun colonySizeFor(start: String, ruleset: Map<String, List<String>>, days: Int): Long {
    var population = mapOf(start to 1L)
    (1..days).forEach { day ->
        population = population.flatMap { (category, multiplier) -> ruleset[category]!!.map { it to multiplier } }
            .groupBy({ it.first }) { it.second }
            .mapValues { it.value.sum() }
    }
    return population.values.sum()
}
