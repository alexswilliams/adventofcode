package aoc2022.day21

import aoc2022.common.*
import kotlin.test.*

private val exampleInput = "aoc2022/day21/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day21/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 152L
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 301L

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 51928383302238, took 1.54ms

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 3305669217840, took 2.51ms
}

private fun part1(input: List<String>): Long {
    val dictionary: Map<String, LineType> = parseInput(input)
    return findNumberFor(dictionary, "root")
}

private fun part2(input: List<String>): Long {
    val dictionary: Map<String, LineType> = parseInput(input)
    val rootLeft = (dictionary["root"] as Combo).left
    val rootRight = (dictionary["root"] as Combo).right

    fun usesHumn(dictionary: Map<String, LineType>, key: String): Set<String> {
        return when (val value = dictionary[key]) {
            null -> throw Exception("key $key not found")
            is Simple -> emptySet()
            is Combo ->
                if (value.left == "humn" || value.right == "humn") setOf(key, "humn")
                else (usesHumn(dictionary, value.left) + usesHumn(dictionary, value.right))
                    .let { if (it.isNotEmpty()) it.plus(key) else it }
        }
    }

    val keysUsingHumn = usesHumn(dictionary, "root")
    val branchWithHumn = if (rootLeft in keysUsingHumn) rootLeft else rootRight
    val valueToMatchAtRoot = if (rootLeft in keysUsingHumn) findNumberFor(dictionary, rootRight) else findNumberFor(dictionary, rootLeft)

    fun findHumn(dictionary: Map<String, LineType>, key: String, target: Long): Long {
        val value = dictionary[key] ?: throw Exception("key $key not found")
        if (value is Simple) throw Exception("Unexpected Simple number encountered when reversing humn")
        val cmb = value as Combo
        val leftUsesHumn = value.left in keysUsingHumn
        val otherSide = if (leftUsesHumn) findNumberFor(dictionary, value.right) else findNumberFor(dictionary, value.left)
        val thisSideKey = if (leftUsesHumn) value.left else value.right
        val newTarget = when (cmb.operation) {
            '+' -> target - otherSide
            '*' -> target / otherSide
            '-' -> if (leftUsesHumn) target + otherSide else otherSide - target
            '/' -> if (leftUsesHumn) target * otherSide else otherSide / target
            else -> throw Exception("Unexpected operation ${cmb.operation}")
        }
        return if (thisSideKey == "humn") newTarget else findHumn(dictionary, thisSideKey, newTarget)
    }

    return findHumn(dictionary, branchWithHumn, valueToMatchAtRoot)
}


private fun findNumberFor(dictionary: Map<String, LineType>, key: String): Long {
    return when (val value = dictionary[key]) {
        null -> throw Exception("key $key not found")
        is Simple -> value.value.toLong()
        is Combo -> {
            val left = findNumberFor(dictionary, value.left)
            val right = findNumberFor(dictionary, value.right)
            when (value.operation) {
                '+' -> left + right
                '-' -> left - right
                '*' -> left * right
                '/' -> left / right
                else -> throw Exception("Unknown symbol ${value.operation}")
            }
        }
    }
}

private fun parseInput(input: List<String>): Map<String, LineType> =
    input.map { it.split(':') }
        .associateBy({ (k) -> k }) { (_, v) -> if (v.trim().toIntOrNull() == null) Combo.of(v.trim()) else Simple(v.trim().toInt()) }

private sealed interface LineType
private data class Simple(val value: Int) : LineType
private data class Combo(val left: String, val right: String, val operation: Char) : LineType {
    companion object {
        fun of(line: String) = Combo(line.take(4), line.takeLast(4), line.elementAt(5))
    }
}

