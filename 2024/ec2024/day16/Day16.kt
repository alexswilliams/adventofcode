package ec2024.day16

import common.*
import java.util.HashMap.*

private val examples = loadFilesToLines("ec2024/day16", "example1.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day16", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day16.assertCorrect()
    benchmark { part1(puzzles[0]) } // 29.3µs
    benchmark(100) { part2(puzzles[1]) } // 24.4ms
    benchmark(50) { part3(puzzles[2]) } // 59.9ms
}

internal object Day16 : Challenge {
    override fun assertCorrect() {
        check(">.- -.- ^,-", "P1 Example") { part1(examples[0]) }
        check("<.* *:* ^_< >:*", "P1 Puzzle") { part1(puzzles[0]) }

        check(280014668134L, "P2 Example") { part2(examples[0]) }
        check(139028598832L, "P2 Puzzle") { part2(puzzles[1]) }

        check("627 128", "P3 Example") { part3(examples[1]) }
        check("601 55", "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String =
    parse(input) { line, range -> line.substring(range) }
        .joinToString(" ") { (wheel, step) -> wheel[(100 * step) % wheel.size] }

private fun part2(input: List<String>): Long {
    val wheels = parse(input) { line, range -> "${line[range.first]}${line[range.last]}" }
        .map { (wheel, step) -> wheel.indices.map { i -> wheel[(i * step) % wheel.size] } }

    val target = 202420242024L
    val cycleLength = lcm(wheels.map { it.size })
    val cycleCount = (target / cycleLength)
    val remainderLength = (target % cycleLength).toInt()

    fun faceAt(iteration: Int): String = wheels.joinToString("") { wheel -> wheel[iteration % wheel.size] }
    fun valueOfFacesAtIteration(iteration: Int): Int = faceAt(iteration).frequency2().sumOf { (_, freq) -> (freq - 2).coerceAtLeast(0) }

    val valueOfRemainder = (1..remainderLength).sumOf { iteration -> valueOfFacesAtIteration(iteration) }
    val valueAfterRemainder = (remainderLength + 1..cycleLength).sumOf { iteration -> valueOfFacesAtIteration(iteration) }
    return valueOfRemainder * (cycleCount + 1) + valueAfterRemainder * cycleCount
}

private fun part3(input: List<String>): String {
    val parsed = parse(input) { line, range -> "${line[range.first]}${line[range.last]}" }
    val wheels = parsed.map { (wheel, _) -> wheel.toTypedArray() }.toTypedArray()
    val sizes = parsed.map { (wheel, _) -> wheel.size }.toIntArray()
    val steps = parsed.map { (_, step) -> step }.toIntArray()
    val start = parsed.indices.map { 0 } to 0

    fun facesAt(pos: Iterable<Int>, wheels: Array<Array<String>>): String = buildString(2 * wheels.size) { pos.forEachIndexed { w, i -> append(wheels[w][i]) } }
    val valueCache = newHashMap<List<Int>, Int>(lcm(sizes.asIterable()) * (sizes.size + 1))
    fun valueOfFaces(pos: List<Int>): Int = valueCache.getOrPut(pos) { facesAt(pos, wheels).frequency2().sumOf { (_, freq) -> (freq - 2).coerceAtLeast(0) } }
    fun advance(pos: List<Int>, offset: Int) = pos.mapIndexed { wheel, i -> (i + steps[wheel] + offset) % sizes[wheel] }

    var states = listOf(Triple(start.first, 0, 0))
    repeat(256) { spin ->
        states = states.flatMapTo(ArrayList(states.size * 3)) { (pos, min, max) ->
            listOf(
                advance(pos, -1).let { valueOfFaces(it).let { coins -> Triple(it, min + coins, max + coins) } },
                advance(pos, 0).let { valueOfFaces(it).let { coins -> Triple(it, min + coins, max + coins) } },
                advance(pos, +1).let { valueOfFaces(it).let { coins -> Triple(it, min + coins, max + coins) } }
            )
        }.groupByTo(newHashMap(spin * 2 + 1)) { it.first }
            .map { (pos, matches) -> Triple(pos, matches.minOf { (_, mins, _) -> mins }, matches.maxOf { (_, _, maxes) -> maxes }) }
    }

    return "${states.maxOf { (_, _, maxes) -> maxes }} ${states.minOf { (_, mins, _) -> mins }}"
}

private fun parse(input: List<String>, face: (String, IntRange) -> String) =
    input.first().splitToInts(",").let { steps ->
        input.drop(2).map { line ->
            steps.indices.map { i -> i * 4..i * 4 + 2 }.map { range -> if (line.indices fullyContains range) face(line, range) else null }
        }.transpose().map { it.filterNotNullOrBlank() }.zip(steps)
    }
