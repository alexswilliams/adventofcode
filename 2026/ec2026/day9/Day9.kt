package ec2026.day9

import common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

private val example = loadFilesToLines("ec2026/day9", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("ec2026/day9", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day9.assertCorrect()
    benchmark { part1(puzzle[0]) } // 11.8µs
    benchmark { part2(puzzle[1]) } // 157.9µs
    benchmark { part3(puzzle[2]) } // 338.7µs parallel, 1.3ms serial
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
        val (growth, limit) = parseInput(line)
        if (limit == 1) return 1
        val branches = IntArray(growth + 1).apply { this[1] = 1 }
        var numberOfBranchPoints = 0
        var seconds = 1
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


private fun part2(input: List<String>): Int = input.sumOf { growTree(it) }

private fun growTree(line: String): Int {
    val (growth, limit) = parseInput(line)
    if (limit == 1) return 1
    val branches = IntArray(growth + 1).apply { this[1] = 1 }
    var seconds = 1
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

private fun parseInput(line: String): Pair<Int, Int> {
    val growth = line.toIntFromIndex(line.indexOf(',', 4) + 9)
    val limit = line.toIntFromIndex(line.lastIndexOf('=') + 1)
    return Pair(growth, limit)
}

private fun parseLongInput(line: String): Pair<Long, Long> {
    val growth = line.toLongFromIndex(line.indexOf(',', 4) + 9)
    val limit = line.toLongFromIndex(line.lastIndexOf('=') + 1)
    return Pair(growth, limit)
}


private fun part3(input: List<String>): Long =
    runBlocking(Dispatchers.Default) {
        input.map { async { growTreeSparse(it) } }.awaitAll()
            .sum()
    }
//    input.sumOf { growTreeSparse(it) }

private fun growTreeSparse(line: String): Long {
    val (growth, limit) = parseLongInput(line)
    if (limit == 1L) return 1L
    val branchLengths = LongArray(40).apply { this[0] = 1 } // size determined by trial run, so might need expanding for other people's solutions
    val branchCounts = LongArray(40).apply { this[0] = 1 }
    var numberOfBranchPoints = 0L
    var seconds = 1L
    while (numberOfBranchPoints + 1 < limit) {
        val indexOfLongestBranch = branchLengths.indexOfMax()
        val longestBranch = branchLengths[indexOfLongestBranch]
        val secondsToSkip = growth - longestBranch
        seconds += secondsToSkip
        // Grow all branches
        branchLengths.forEachIndexed { index, oldLength -> if (oldLength > 0) branchLengths[index] = oldLength + secondsToSkip }

        // Add new branch points
        val longestBranchCount = branchCounts[indexOfLongestBranch]
        numberOfBranchPoints += longestBranchCount

        // Split longest branches
        addOrAllocate(growth / 2, longestBranchCount * 2, branchCounts, branchLengths)
        addOrAllocate(1L, longestBranchCount, branchCounts, branchLengths)
        branchCounts[indexOfLongestBranch] = 0L
        branchLengths[indexOfLongestBranch] = 0L
    }
    return seconds
}

private fun addOrAllocate(length: Long, countToAdd: Long, branchCounts: LongArray, branchLengths: LongArray) {
    val existingIndex = branchLengths.indexOfFirst { it == length }
    val index = if (existingIndex == -1) branchLengths.indexOfFirst { it == 0L } else existingIndex
    branchLengths[index] = length
    branchCounts[index] += countToAdd
}

private fun LongArray.indexOfMax(): Int {
    val max = this.max()
    return this.indexOf(max)
}
