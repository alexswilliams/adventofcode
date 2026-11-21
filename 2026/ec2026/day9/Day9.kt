package ec2026.day9

import common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.*

private val example = loadFilesToLines("ec2026/day9", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("ec2026/day9", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day9.assertCorrect()
    benchmark { part1(puzzle[0]) } // 124.6µs
    benchmark { part2(puzzle[1]) } // 297.5µs parallel, 327.5µs serial
    benchmark { part3(puzzle[2]) } // 3.1ms parallel, 20.2ms serial
}

internal object Day9 : Challenge {
    override fun assertCorrect() {
        check(27, "P1 Example") { part1(example[0]) }
        check(225, "P1 Puzzle") { part1(puzzle[0]) }

        check(42, "P2 Example") { part2(example[1]) }
        check(15467, "P2 Puzzle") { part2(puzzle[1]) }

        check(868547595577477, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: List<String>): Int {
    return input.sumOf { line ->
        val (_, growth, limit) = line.matchingAsIntList(Regex("id=(\\d+), growth=(\\d+), limit=(\\d+)"))!!
        if (limit == 1) return 1
        val branches = IntArray(growth + 1).apply { this[0] = 1 }
        var numberOfBranchPoints = 0
        var seconds = 0
        while (branches.sumOfIndexed { index, e -> index * e } + numberOfBranchPoints < limit) {
            seconds++
            (branches.lastIndex downTo 1)
                .forEach { newLength -> branches[newLength] = branches[newLength - 1] }
            branches[0] = 0
            if (branches.last() > 0) {
                numberOfBranchPoints += branches.last()
                branches[growth / 2] += branches.last() * 2
                branches[1] += branches.last()
                branches[branches.lastIndex] = 0
            }
        }
        seconds
    }
}


private fun part2(input: List<String>): Int =
    runBlocking(Dispatchers.Default) {
        input.map { async { growTree(it) } }.awaitAll()
            .sum()
    }
//    input.sumOf { growTree(it) }

private fun growTree(line: String): Int {
    val (_, growth, limit) = line.matchingAsIntList(Regex("id=(\\d+), growth=(\\d+), limit=(\\d+)"))!!
    if (limit == 1) return 1
    val branches = IntArray(growth + 1).apply { this[0] = 1 }
    var seconds = 0
    var numberOfBranchPoints = 0
    while (numberOfBranchPoints + 1 < limit) {
        val longestBranch = branches.indexOfLast { it > 0 }
        val secondsToSkip = growth - longestBranch
        seconds += secondsToSkip
        (branches.lastIndex downTo secondsToSkip)
            .forEach { newLength -> branches[newLength] = branches[newLength - secondsToSkip] }
        branches.fill(0, 0, secondsToSkip)
        numberOfBranchPoints += branches.last()
        branches[growth / 2] += branches.last() * 2
        branches[1] += branches.last()
        branches[branches.lastIndex] = 0
    }

    return seconds
}


private fun part3(input: List<String>): Long =
    runBlocking(Dispatchers.Default) {
        input.map { async { growTreeSparse(it) } }.awaitAll()
            .sum()
    }
//    input.sumOf { growTreeSparse(it) }

private fun growTreeSparse(line: String): Long {
    val (_, growth, limit) = line.matchingAsLongList(Regex("id=(\\d+), growth=(\\d+), limit=(\\d+)"))!!
    if (limit == 1L) return 1L
    var branches = TreeMap<Long, Long>().apply { this[0] = 1L }
    var numberOfBranchPoints = 0L
    var seconds = 0L
    while (numberOfBranchPoints + 1 < limit) {
        val longestBranch = branches.lastKey()
        val secondsToSkip = growth - longestBranch
        seconds += secondsToSkip
        branches = branches.mapKeysTo(TreeMap()) { (length) -> length + secondsToSkip }
        numberOfBranchPoints += branches.lastEntry().value
        branches.compute(growth / 2) { _, existing -> (existing ?: 0L) + branches.lastEntry().value * 2 }
        branches.compute(1) { _, existing -> (existing ?: 0L) + branches.lastEntry().value }
        branches.remove(branches.lastKey())
    }
    return seconds
}
