@file:Suppress("Destructure")

package aoc2023.day19

import common.*

private val examples = loadFilesToLines("aoc2023/day19", "example.txt")
private val puzzles = loadFilesToLines("aoc2023/day19", "input.txt")

internal fun main() {
    Day19.assertCorrect()
    benchmark { part1(puzzles[0]) } // 554µs
    benchmark { part2(puzzles[0]) }
}

internal object Day19 : Challenge {
    override fun assertCorrect() {
        check(19114, "P1 Example") { part1(examples[0]) }
        check(389114, "P1 Puzzle") { part1(puzzles[0]) }

        check(0, "P2 Example") { part2(examples[0]) }
        check(0, "P2 Puzzle") { part2(puzzles[0]) }
    }
}

private data class Part(val x: Int, val m: Int, val a: Int, val s: Int)
private data class Condition(val text: String, val predicate: (Part) -> Boolean, val sink: String)
private data class Rule(val name: String, val defaultSink: String, val conditions: List<Condition>)

private fun part1(input: List<String>): Int {
    val parts = input.takeLastWhile { it.isNotBlank() }.mapMatching(Regex("\\{x=(\\d+),m=(\\d+),a=(\\d+),s=(\\d+)}")).map { (x, m, a, s) -> Part(x.toInt(), m.toInt(), a.toInt(), s.toInt()) }
    val ruleset = input.takeWhile { it.isNotBlank() }.map {
        Rule(
            name = it.substringBefore('{'),
            defaultSink = it.substringAfterLast(',').dropLast(1),
            conditions = it.substringAfter('{').substringBeforeLast(',').split(',').map { c ->
                val amount = c.drop(2).substringBeforeLast(':').toInt()
                Condition(
                    text = c,
                    predicate = when (c.take(2)) {
                        "x<" -> { part -> part.x < amount }
                        "x>" -> { part -> part.x > amount }
                        "m<" -> { part -> part.m < amount }
                        "m>" -> { part -> part.m > amount }
                        "a<" -> { part -> part.a < amount }
                        "a>" -> { part -> part.a > amount }
                        "s<" -> { part -> part.s < amount }
                        "s>" -> { part -> part.s > amount }
                        else -> error("Invalid Rule: $c")
                    },
                    sink = c.substringAfterLast(':')
                )
            })
    }.associateBy { it.name }
    return parts.filter { part -> isAccepted(ruleset, part) }.sumOf { it.x + it.m + it.a + it.s }
}

private fun isAccepted(ruleset: Map<String, Rule>, part: Part): Boolean {
    var rule = ruleset["in"]!!
    do {
        val next = rule.conditions.firstOrNull { it.predicate(part) }?.sink ?: rule.defaultSink
        when (next) {
            "R" -> return false
            "A" -> return true
            else -> rule = ruleset[next]!!
        }
    } while (true)
}

private fun part2(input: List<String>): Long = 0
