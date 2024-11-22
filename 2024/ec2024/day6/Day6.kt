package ec2024.day6

import common.*
import kotlin.test.*

private const val rootFolder = "ec2024/day6"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day6.assertPart1Correct()
    Day6.assertPart2Correct()
    Day6.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 50µs
    benchmark { part2(puzzle2Input) } // 309µs
    benchmark { part3(puzzle3Input) } // 1.11ms
}

internal object Day6 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals("RRB@", it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals("RRNSCGXRQCQV@", it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals("RB@", it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals("RSQRTQVRQL@", it) }
    }

    override fun assertPart3Correct() {
        part3(exampleInput).also { println("[Example] Part 3: $it") }.also { assertEquals("RB@", it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals("RLBDDLXKBLZS@", it) }
    }
}


private fun part1(input: List<String>) = findBestApple(input, useFirstLetter = false)
private fun part2(input: List<String>) = findBestApple(input, useFirstLetter = true)
private fun part3(input: List<String>) = part2(input)


private fun findBestApple(input: List<String>, useFirstLetter: Boolean): String {
    val (branchParents, appleParents) = parseTree(input)
    tailrec fun pathToRoot(current: String?, pathSoFar: String = "@"): String {
        if (current == null) return pathSoFar
        return pathToRoot(
            branchParents[current],
            if (useFirstLetter) current.first() + pathSoFar else current + pathSoFar
        )
    }
    return appleParents
        .map(::pathToRoot)
        .groupBy { it.length }
        .firstNotNullOf { if (it.value.size == 1) it.value.single() else null }
}

private fun parseTree(input: List<String>): Pair<Map<String, String>, Set<String>> {
    val branchParents = HashMap<String, String>(20000)
    val appleParents = mutableSetOf<String>()
    input.forEach { line ->
        val parent = line.substringBefore(':')
        if (parent != "ANT" && parent != "BUG") {
            line.substring(parent.length + 1).split(',').forEach { it ->
                if (it == "@") appleParents.add(parent)
                else if (it != "BUG" && it != "ANT") branchParents.put(it, parent)
            }
        }
    }
    return Pair(branchParents, appleParents)
}
