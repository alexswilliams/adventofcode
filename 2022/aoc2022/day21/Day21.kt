package aoc2022.day21

import common.*

private val example = loadFilesToLines("aoc2022/day21", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day21", "input.txt").single()

internal fun main() {
    Day21.assertCorrect()
    benchmark { part1(puzzle) } // 442.5µs
    benchmark { part2(puzzle) } // 538.5µs
}

internal object Day21 : Challenge {
    override fun assertCorrect() {
        check(152, "P1 Example") { part1(example) }
        check(51928383302238, "P1 Puzzle") { part1(puzzle) }

        check(301, "P2 Example") { part2(example) }
        check(3305669217840, "P2 Puzzle") { part2(puzzle) }
    }
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

