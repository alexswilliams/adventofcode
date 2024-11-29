package ec2024.day11

import common.*

private val examples = loadFilesToLines("ec2024/day11", "example.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day11", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day11.assertCorrect()
    benchmark { part1(puzzles[0]) } // 20.2µs
    benchmark { part2(puzzles[1]) } // 149µs
    benchmark(10) { part3(puzzles[2]) } // 43ms
}

internal object Day11 : Challenge {
    override fun assertCorrect() {
        check(8, "P1 Example") { part1(examples[0]) }
        check(44, "P1 Puzzle") { part1(puzzles[0]) }

        check(256061, "P2 Puzzle") { part2(puzzles[1]) }

        check(268815, "P3 Example") { part3(examples[1]) }
        check(1403038233378L, "P3 Puzzle") { part3(puzzles[2]) }
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
    repeat(days) {
        population = population.flatMap { (category, multiplier) -> ruleset[category]!!.map { it to multiplier } }
            .groupBy({ it.first }) { it.second }
            .mapValues { it.value.sum() }
    }
    return population.values.sum()
}
