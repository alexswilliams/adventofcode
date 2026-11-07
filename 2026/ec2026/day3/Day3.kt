package ec2026.day3

import common.Challenge
import common.benchmark
import common.loadFilesToLines

private val example = loadFilesToLines("ec2026/day3", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToLines("ec2026/day3", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day3.assertCorrect()
    benchmark { part1(puzzle[0]) } // 11.1µs
    benchmark { part2(puzzle[1]) } // 211.8µs
    benchmark(10) { part3(puzzle[2]) } // 402.9ms
}

internal object Day3 : Challenge {
    override fun assertCorrect() {
        check(1478, "P1 Example") { part1(example[0]) }
        check(12285, "P1 Puzzle") { part1(puzzle[0]) }

        check(1161463, "P2 Example") { part2(example[1]) }
        check(23624130397, "P2 Puzzle") { part2(puzzle[1]) }

        check(50987, "P3 Example") { part3(example[2]) }
        check(389573, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: List<String>): Long = input
    .sumOf { line ->
        val (bases, multipliers) = line.substring(4).split(' ', limit = 2)
        bases.reversed()
            .zip(multipliers.reversed())
            .foldIndexed(0L) { index, acc, (base, multiplier) ->
                acc + Math.powExact(base.digitToInt(), index) * multiplier.digitToInt()
            }
    }


private fun part2(input: List<String>): Long = input
    .sumOf { line ->
        val (bases, multipliers) = line.substring(4).split(' ', limit = 2)
        bases.reversed().repeat(multipliers.length / bases.length + 1)
            .zip(multipliers.reversed())
            .foldIndexed(0L) { index, acc, (base, multiplier) ->
                acc + Math.powExact(base.digitToInt().toLong(), index) * multiplier.digitToInt().toLong()
            }
    }


private fun part3(input: List<String>): Long = input
    .sumOf { line ->
        val (bases, multipliers) = line.substring(5).split(' ', limit = 2)
        val declaredVote = bases.reversed().repeat(multipliers.length / bases.length + 1)
            .zip(multipliers.reversed())
            .foldIndexed(0L) { index, acc, (base, multiplier) ->
                acc + Math.powExact(base.digitToInt().toLong(), index) * multiplier.digitToInt().toLong()
            }
        // very naive but fine:
        if ((0..<multipliers.toLong()).any { candidate ->
                val newVote: Long = bases.reversed().repeat(candidate.toString().length / bases.length + 1)
                    .zip(candidate.toString().reversed())
                    .foldIndexed(0L) { index, acc, (base, multiplier) ->
                        acc + Math.powExact(base.digitToInt().toLong(), index) * multiplier.digitToInt().toLong()
                    }
                newVote == declaredVote
            }) 0 else declaredVote
    }
