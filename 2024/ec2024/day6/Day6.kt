package ec2024.day6

import common.*

private val examples = loadFilesToLines("ec2024/day6", "example.txt")
private val puzzles = loadFilesToLines("ec2024/day6", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzles[0]) } // 50µs
    benchmark { part2(puzzles[1]) } // 309µs
    benchmark { part3(puzzles[2]) } // 1.11ms
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check("RRB@", "P1 Example") { part1(examples[0]) }
        check("RRNSCGXRQCQV@", "P1 Puzzle") { part1(puzzles[0]) }

        check("RB@", "P2 Example") { part2(examples[0]) }
        check("RSQRTQVRQL@", "P2 Puzzle") { part2(puzzles[1]) }

        check("RB@", "P3 Example") { part3(examples[0]) }
        check("RLBDDLXKBLZS@", "P3 Puzzle") { part3(puzzles[2]) }
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
