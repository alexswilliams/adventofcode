@file:Suppress("Destructure")

package aoc2023.day19

import common.*

private val example = loadFilesToLines("aoc2023/day19", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2023/day19", "input.txt").single()

internal fun main() {
    Day19.assertCorrect()
    benchmark { part1(puzzle) } // 203µs
    benchmark { part2(puzzle) } // 142µs
}

internal object Day19 : Challenge {
    override fun assertCorrect() {
        check(19114, "P1 Example") { part1(example) }
        check(389114, "P1 Puzzle") { part1(puzzle) }

        check(167409079868000L, "P2 Example") { part2(example) }
        check(125051049836302L, "P2 Puzzle") { part2(puzzle) }
    }
}

private data class Part(val x: Int, val m: Int, val a: Int, val s: Int)
private data class Condition(val predicate: (Part) -> Boolean, val sink: String)
private data class Rule(val name: String, val defaultSink: String, val conditions: List<Condition>)

private fun part1(input: List<String>): Int {
    val parts = input.asSequence().dropWhile { it.isNotBlank() }.drop(1)
        .map { line ->
            val x = line.toIntFromIndex(3)
            val mIndex = line.indexOf('=', 6)
            val m = line.toIntFromIndex(mIndex + 1)
            val aIndex = line.indexOf('=', mIndex + 4)
            val a = line.toIntFromIndex(aIndex + 1)
            val sIndex = line.indexOf('=', aIndex + 4)
            val s = line.toIntFromIndex(sIndex + 1)
            Part(x, m, a, s)
        }
    val ruleset = input.asSequence().takeWhile { it.isNotBlank() }.map { rule ->
        val name = rule.substringBefore('{')
        val defaultSink = rule.substring(rule.lastIndexOf(',') + 1, rule.length - 1)
        Rule(
            name = name,
            defaultSink = defaultSink,
            conditions = rule.substring(name.length + 1, rule.length - defaultSink.length - 2)
                .splitMappingRanges(",") { it, start, end ->
                    val amount = it.toIntFromIndex(start + 2)
                    Condition(
                        predicate = when (it.substring(start, start + 2)) {
                            "x<" -> { part -> part.x < amount }
                            "x>" -> { part -> part.x > amount }
                            "m<" -> { part -> part.m < amount }
                            "m>" -> { part -> part.m > amount }
                            "a<" -> { part -> part.a < amount }
                            "a>" -> { part -> part.a > amount }
                            "s<" -> { part -> part.s < amount }
                            "s>" -> { part -> part.s > amount }
                            else -> error("Invalid Rule: ${it.substring(start, end + 1)}")
                        },
                        sink = it.substring(it.lastIndexOf(':', end) + 1, end + 1))
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

private fun part2(input: List<String>): Long =
    validRanges(parseRuleset(input), "in", 1..4000, 1..4000, 1..4000, 1..4000)
        .sumOf { 1L * it.x.size * it.m.size * it.a.size * it.s.size }


private data class Rule2(val name: String, val defaultSink: String, val conditions: List<Condition2>)
private data class Condition2(val field: Char, val sign: Char, val number: Int, val targetRule: String)
private data class XmasRange(val x: IntRange, val m: IntRange, val a: IntRange, val s: IntRange)

private fun parseRuleset(input: List<String>): Map<String, Rule2> =
    input.asSequence()
        .takeWhile { it.isNotBlank() }
        .map { rule ->
            val name = rule.substringBefore('{')
            val defaultSink = rule.substring(rule.lastIndexOf(',') + 1, rule.length - 1)
            val conditions = rule.substring(name.length + 1, rule.length - defaultSink.length - 2)
                .splitMappingRanges(",") { it, start, end -> Condition2(it[start], it[start + 1], it.toIntFromIndex(start + 2), it.substring(it.lastIndexOf(':', end) + 1, end + 1)) }
            Rule2(name, defaultSink, conditions)
        }.associateBy { it.name }

private fun constrain(i: IntRange, c: Condition2, fieldName: Char): IntRange =
    if (c.field != fieldName) i
    else if (c.sign == '>') IntRange(i.first.coerceAtLeast(c.number + 1), i.last)
    else IntRange(i.first, i.last.coerceAtMost(c.number - 1))

private fun constrainOpposite(i: IntRange, c: Condition2, fieldName: Char): IntRange =
    if (c.field != fieldName) i
    else if (c.sign == '<') IntRange(i.first.coerceAtLeast(c.number), i.last)
    else IntRange(i.first, i.last.coerceAtMost(c.number))

private fun validRanges(ruleset: Map<String, Rule2>, ruleName: String, x: IntRange, m: IntRange, a: IntRange, s: IntRange, soFar: MutableList<XmasRange> = mutableListOf()): List<XmasRange> {
    if (ruleName == "A") return soFar.apply { add(XmasRange(x, m, a, s)) }
    if (x.isEmpty() || m.isEmpty() || a.isEmpty() || s.isEmpty()) return soFar
    val rule = ruleset[ruleName] ?: error("Rule invalid: $ruleName")

    var xRemaining = x
    var mRemaining = m
    var aRemaining = a
    var sRemaining = s
    rule.conditions.forEach { c ->
        val xC = constrain(xRemaining, c, 'x')
        val mC = constrain(mRemaining, c, 'm')
        val aC = constrain(aRemaining, c, 'a')
        val sC = constrain(sRemaining, c, 's')
        xRemaining = constrainOpposite(xRemaining, c, 'x')
        mRemaining = constrainOpposite(mRemaining, c, 'm')
        aRemaining = constrainOpposite(aRemaining, c, 'a')
        sRemaining = constrainOpposite(sRemaining, c, 's')
        if (c.targetRule != "R") validRanges(ruleset, c.targetRule, xC, mC, aC, sC, soFar)
    }
    if (rule.defaultSink != "R") validRanges(ruleset, rule.defaultSink, xRemaining, mRemaining, aRemaining, sRemaining, soFar)
    return soFar
}
