package ec2024.day4

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("ec2024/day4", "example.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day4", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzles[0]) } // 1.94µs
    benchmark { part2(puzzles[1]) } // 7.1µs
    benchmark(100) { part3(puzzles[2]) } // 2.2ms
}

internal object Day4 : Challenge {
    override fun assertCorrect() {
        check(10, "P1 Example") { part1(examples[0]) }
        check(81, "P1 Puzzle") { part1(puzzles[0]) }

        check(10, "P2 Example") { part2(examples[0]) }
        check(894741, "P2 Puzzle") { part2(puzzles[1]) }

        check(8, "P3 Example") { part3(examples[1]) }
        check(125159663, "P3 Puzzle") { part3(puzzles[2]) }
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
