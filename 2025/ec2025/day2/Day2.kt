package ec2025.day2

import common.*
import kotlinx.coroutines.*
import kotlin.math.*

private val examples = loadFilesToLines("ec2025/day2", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day2", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day2.assertCorrect()
    benchmark { part1(puzzles[0]) } // 3.2µs
    benchmark { part2(puzzles[1]) } // 395.4µs
    benchmark(10) { part3(puzzles[2]) } // 17.5ms
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check("[357,862]", "P1 Example") { part1(examples[0]) }
        check("[140021,700015]", "P1 Puzzle") { part1(puzzles[0]) }

        check(4076, "P2 Example") { part2(examples[1]) }
        check(1387, "P2 Puzzle") { part2(puzzles[1]) }

        check(406954, "P3 Example") { part3(examples[2]) }
        check(136830, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String {
    val x = input[0].toLongFromIndex(3)
    val y = input[0].toLongFromIndex(input[0].indexOf(',', 4) + 1)

    var rX = 0L
    var rY = 0L
    repeat(3) {
        val tmpX = rX
        rX = (rX * rX - rY * rY) / 10L + x
        rY = (tmpX * rY) / 5L + y
    }
    return "[${rX},${rY}]"
}

private fun part2(input: List<String>): Int = filterGrid(input, 10)
private fun part3(input: List<String>): Int = filterGrid(input, 1)


private fun filterGrid(input: List<String>, step: Long): Int {
    val startX = input[0].toLongFromIndex(3)
    val startY = input[0].toLongFromIndex(input[0].indexOf(',', 4) + 1)

    return runBlocking(Dispatchers.Default) {
        LongProgression.fromClosedRange(startX, startX + 1000, step).map { x ->
            async {
                LongProgression.fromClosedRange(startY, startY + 1000, step)
                    .count { y ->
                        var rX = 0L
                        var rY = 0L
                        repeat(100) {
                            val tmpX = rX
                            rX = (rX * rX - rY * rY) / 100_000L + x
                            rY = (tmpX * rY) / 50_000L + y

                            if (rX.absoluteValue > 1_000_000 || rY.absoluteValue > 1_000_000)
                                return@count false
                        }
                        true
                    }
            }
        }.awaitAll().sum()
    }
}
