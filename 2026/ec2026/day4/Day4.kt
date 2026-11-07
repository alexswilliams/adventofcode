package ec2026.day4

import common.*

private val example = loadFilesToLines("ec2026/day4", "example1.txt", "example2.txt", "example3a.txt", "example3b.txt", "example3c.txt")
private val puzzle = loadFilesToLines("ec2026/day4", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day4.assertCorrect()
    benchmark { part1(puzzle[0]) } // 25.2µs
    benchmark { part2(puzzle[1]) } // 804.4µs
    benchmark(100) { part3(puzzle[2]) } // 11.7ms
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


private data class Input(val id: Int, val quest: Int, val part: Int, val time: Long)
private data class Part(val quest: Int, val part: Int)
private data class Tally(val id: Int, val points: Long)
private data class Rank(val rank: Int, val points: Long)

private fun part3(input: List<String>): Long {
    val data = input.mapMatching(INPUT_PATTERN)
        .map { (id, quest, part, time) -> Input(id.toInt(), quest.toInt(), part.toInt(), time.toLong()) }
    val rounds = data.map { Part(it.quest, it.part) }.distinct()
        .sortedWith(compareBy({ it.quest }, { it.part }))

    return rounds.map { it.quest }.distinct().flatMap { quest ->
        val parts = rounds.filter { it.quest == quest }.map { it.part }
        parts.flatMap { part ->
            val participantsForPart = data.filter { it.quest == quest && it.part == part }
            val participantsInPreviousParts = data.filter { prev -> prev.quest == quest && prev.part < part && prev.id in participantsForPart.map { it.id } }
            val participants = (participantsForPart + participantsInPreviousParts)
                .groupingBy { it.id }.summingLong { it.time }
                .entries.sortedBy { it.value }

            tallies(participants, pointsForPart(part))
        }
    }.groupingBy { it.id }.summingLong { it.points }
        .values.sortedByDescending { it }
        .let { list -> ranks(list) }
        .fold(0L) { acc, it -> acc + it.rank * it.points }
}

private fun ranks(list: List<Long>): List<Rank> {
    var rankToAward = 1
    return list.mapIndexed { index, points ->
        if (index == 0 || list[index - 1] != points)
            rankToAward = index + 1
        Rank(rankToAward, points)
    }
}

private fun tallies(participants: List<Map.Entry<Int, Long>>, pointsForPart: Long): List<Tally> {
    var pointsForRank = pointsForPart
    var pointsToAward = pointsForRank
    val points: List<Tally> = participants.asSequence().mapIndexed { index, (id, time) ->
        if (index > 0 && participants[index - 1].value == time) {
            Tally(id, pointsToAward)
        } else {
            pointsToAward = pointsForRank;
            Tally(id, pointsForRank)
        }.also { pointsForRank = (pointsForRank - 1).coerceAtLeast(0); }
    }.takeWhile { it.points > 0 }.toList()
    return points
}

fun pointsForPart(part: Int): Long = when (part) {
    1 -> 50
    2 -> 100
    3 -> 150
    else -> throw RuntimeException("Invalid part $part")
}

