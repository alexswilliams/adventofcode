package ec2026.day4

import common.*
import java.util.*

private val example = loadFilesToLines("ec2026/day4", "example1.txt", "example2.txt", "example3a.txt", "example3b.txt", "example3c.txt")
private val puzzle = loadFilesToLines("ec2026/day4", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzle[0]) } // 25.2µs
    benchmark { part2(puzzle[1]) } // 761.9µs
    benchmark { part3(puzzle[2]) } // 3.6ms
}

internal object Day4 : Challenge {
    override fun assertCorrect() {
        check(27598, "P1 Example") { part1(example[0]) }
        check(179794, "P1 Puzzle") { part1(puzzle[0]) }

        check(6470, "P2 Example") { part2(example[1]) }
        check(629872470, "P2 Puzzle") { part2(puzzle[1]) }

        check(2998, "P3 Example A") { part3(example[2]) }
        check(3004, "P3 Example B") { part3(example[3]) }
        check(6232480, "P3 Example C") { part3(example[4]) }
        check(6579299, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private val INPUT_PATTERN: Regex = Regex("id=(\\d+) quest=(\\d+) part=(\\d+) time=(\\d+)")

private fun part1(input: List<String>): Long = input
    .mapMatching(INPUT_PATTERN)
    .groupingBy { (id, _, _, _) -> id }.summingLong { (_, _, _, time) -> time.toLong() }
    .values.sortedBy { value -> value }
    .foldIndexed(0L) { index, acc, time -> acc + (index + 1) * time }


private data class Scores(val completedParts: Int, val time: Long)

private fun part2(input: List<String>): Long = input
    .mapMatching(INPUT_PATTERN)
    .groupingBy { (id, _, _, _) -> id }
    .aggregate { _, accumulator: Scores?, (_, _, _, time), first ->
        if (first) Scores(1, time.toLong())
        else accumulator!!.copy(completedParts = accumulator.completedParts + 1, time = accumulator.time + time.toLong())
    }
    .values.sortedWith(Comparator.comparingInt<Scores> { it.completedParts }.reversed().thenComparingLong { it.time })
    .foldIndexed(0L) { index, acc, it -> acc + (index + 1) * it.time }


private data class Tally(val id: Int, val points: Long)
private data class Rank(val rank: Int, val points: Long)
private data class Timing(val id: Int, val time: Long)
private data class Part(val quest: Int, val part: Int) : Comparable<Part> {
    override fun compareTo(other: Part): Int = compareValuesBy(this, other, { it.quest }, { it.part })
}

private fun part3(input: List<String>): Long {
    val dataByDay = input.asSequence().mapMatching(INPUT_PATTERN)
        .groupByTo(
            TreeMap(),
            { (_, quest, part, _) -> Part(quest.toInt(), part.toInt()) })
        { (id, _, _, time) -> Timing(id.toInt(), time.toLong()) }

    return dataByDay.asSequence().flatMap { (day, inputsForPart) ->
        val participantsForPart = inputsForPart.mapTo(HashSet()) { it.id }
        val sortedSumOfTimes = (previousParts(day) + day).asSequence()
            .flatMap { dataByDay[it]!! }
            .filter { it.id in participantsForPart }
            .groupingBy { it.id }.summingLong { it.time }
            .entries.sortedBy { it.value }
        tallies(sortedSumOfTimes, pointsForPart(day.part))
    }.groupingBy { it.id }.summingLong { it.points }
        .values.sortedDescending()
        .let { list -> ranks(list) }
        .fold(0L) { acc, it -> acc + it.rank * it.points }
}

private fun previousParts(day: Part): List<Part> = (1..<day.part).map { day.copy(part = it) }

private fun ranks(list: List<Long>): List<Rank> {
    var rankToAward = 1
    return list.mapIndexed { index, points ->
        if (index == 0 || list[index - 1] != points)
            rankToAward = index + 1
        Rank(rankToAward, points)
    }
}

private fun tallies(sortedSumOfTimes: List<Map.Entry<Int, Long>>, pointsForPart: Long): List<Tally> {
    var pointsToAward = 0L
    return sortedSumOfTimes.asSequence().mapIndexed { index, (id, time) ->
        if (index == 0 || sortedSumOfTimes[index - 1].value != time)
            pointsToAward = pointsForPart - index
        Tally(id, pointsToAward)
    }.takeWhile { it.points > 0 }.toList()
}

fun pointsForPart(part: Int): Long = when (part) {
    1 -> 50
    2 -> 100
    3 -> 150
    else -> throw RuntimeException("Invalid part $part")
}

