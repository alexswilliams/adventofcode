package ec2026.day3

import common.Challenge
import common.benchmark
import common.loadFilesToLines

private val example = loadFilesToLines("ec2026/day3", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToLines("ec2026/day3", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day3.assertCorrect()
    benchmark { part1(puzzle[0]) } // 7.0µs
    benchmark { part2(puzzle[1]) } // 159.3µs
    benchmark(100) { part3(puzzle[2]) } // 29.3ms
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
        val (standard, vote) = line.substring(4).split(' ', limit = 2)
        decimalVote(preCalculateBases(vote, standard), vote)
    }

private fun part2(input: List<String>): Long = part1(input)

private fun part3(input: List<String>): Long = input
    .sumOf { line ->
        val (standard, vote) = line.substring(5).split(' ', limit = 2)
        val bases = preCalculateBases(vote, standard)
        val declaredVote = decimalVote(bases, vote)
        if (((vote.toLong() - 1) downTo 2) // lb 0 is certain; lb 1 if standard is > 0; lb 2 if standard AND vote > 0
                .any { declaredVote == decimalVote(bases, it.toString()) }
        ) 0 else declaredVote
    }


private fun decimalVote(bases: LongArray, vote: String): Long =
    vote.indices.fold(0L) { acc, power ->
        val coefficient = vote[vote.lastIndex - power].digitToInt()
        val base = bases[vote.lastIndex - power + (bases.size - vote.length)]
        acc + coefficient * base
    }

private fun preCalculateBases(vote: String, standard: String): LongArray =
    LongArray(vote.length) { index ->
        val power = (vote.lastIndex - index)
        val base = standard[(standard.lastIndex - power.mod(standard.length))].digitToInt().toLong()
        Math.powExact(base, power)
    }
