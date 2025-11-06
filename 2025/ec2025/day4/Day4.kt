package ec2025.day4

import common.*
import kotlin.math.*

private val examples = loadFilesToLines("ec2025/day4", "example1a.txt", "example1b.txt", "example2a.txt", "example2b.txt", "example3a.txt", "example3b.txt")
private val puzzles = loadFilesToLines("ec2025/day4", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzles[0]) } // 9.4µs
    benchmark { part2(puzzles[1]) } // 8.7µs
    benchmark(100) { part3(puzzles[2]) } // 105.7µs
}

internal object Day4 : Challenge {
    override fun assertCorrect() {
        check(32400, "P1 Example A") { part1(examples[0]) }
        check(15888, "P1 Example B") { part1(examples[1]) }
        check(11376, "P1 Puzzle") { part1(puzzles[0]) }

        check(625000000000L, "P2 Example A") { part2(examples[2]) }
        check(1274509803922L, "P2 Example B") { part2(examples[3]) }
        check(1852631578948L, "P2 Puzzle") { part2(puzzles[1]) }

        check(400L, "P3 Example A") { part3(examples[4]) }
        check(6818L, "P3 Example B") { part3(examples[5]) }
        check(215679229687L, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): Int = input.map { it.toInt() }.let { (it.first().toFloat() / it.last().toFloat() * 2025.0f).toInt() }

const val target = 10_000_000_000_000L
private fun part2(input: List<String>): Long =
    input.map { it.toLong() }.let { target.toDouble() / (it.first().toDouble() / it.last().toDouble()) }.let { ceil(it).toLong() }

private fun part3(input: List<String>): Long {
    val head = 0 to input.first().toInt()
    val tail = input.last().toInt() to 0
    val middle = input.drop(1).dropLast(1).map { spindle -> spindle.splitToInts("|").let { it[0] to it[1] } }
    val gears = listOf(head) + middle + listOf(tail)
    return (gears.zipWithNext()
        .map { (gearA, gearB) -> gearA.second.toDouble() / gearB.first.toDouble() }
        .reduce(Double::times) * 100.00).toLong()
}

