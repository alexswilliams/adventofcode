package ec2026.day11

import common.*
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

private val example = loadFilesToLines("ec2026/day11", "example1.txt", "example2.txt", "example3a.txt", "example3b.txt")
private val puzzle = loadFilesToLines("ec2026/day11", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day11.assertCorrect()
    benchmark(10) { part1(puzzle[0]) } // 6.1ms
    benchmark(1) { part2(puzzle[1]) } // 187.5ms
    benchmark(1) { part3(puzzle[2]) } // 12.7s
}

internal object Day11 : Challenge {
    override val skipTests: Boolean
        get() = false

    override fun assertCorrect() {
        check(60, "P1 Example") { part1(example[0]) }
        check(1700, "P1 Puzzle") { part1(puzzle[0]) }

        check(6, "P2 Example") { part2(example[1]) }
        check(1836, "P2 Puzzle") { part2(puzzle[1]) }

        check(854, "P3 Example A") { part3(example[2]) }
        check(9634, "P3 Example B") { part3(example[3]) }
        check(20498, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: List<String>): Int {
    val portalCount = input.first().toIntFromIndex(input.first().lastIndexOf(' ') + 1)
    val shortcuts: Map<Int, List<Pair<Int, Int>>> = input.drop(2)
        .map { it.splitToInts(" ") }
        .flatMap { (a, b) -> listOf(a - 1 to b - 1, b - 1 to a - 1) }
        .groupBy { it.first }

    val ringTeleportationCount = Math.unsignedPowExact(portalCount, 3) / 4

    val withShortcuts = (0..<portalCount).sumOf { start ->
        (0..<portalCount).sumOf { end ->
            if (start == end) return@sumOf 0
            val seen = mutableSetOf(start)
            val work = TreeQueue(start to 0)
            while (true) {
                val (distance, position) = work.pollEntry()!!
                if (position == end)
                    return@sumOf distance
                ((position + portalCount - 1) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount + 1) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                val shortcutsAtPosition = shortcuts[position].orEmpty()
                shortcutsAtPosition.forEach { (_, target) ->
                    if (seen.add(target)) work.offer(target, distance + 1)
                }
            }
            0
        }
    }

    return ringTeleportationCount - withShortcuts
}


private fun part2(input: List<String>): Int {
    val portalCount = input.first().toIntFromIndex(input.first().lastIndexOf(' ') + 1)
    val shortcuts: List<Pair<Int, Int>> = input.drop(2)
        .map { it.splitToInts(" ") }
        .flatMap { (a, b) -> listOf(a - 1 to b - 1, b - 1 to a - 1) }
        .toPersistentList()

    fun sumOfDistances(shortcutsForRound: Map<Int, List<Pair<Int, Int>>>): Int = (0..<portalCount).sumOf { start ->
        (0..<portalCount).sumOf { end ->
            if (start == end) return@sumOf 0
            val seen = BooleanArray(portalCount)
            val work = TreeQueue(start to 0)
            while (true) {
                val (distance, position) = work.pollEntry()!!
                if (position == end)
                    return@sumOf distance
                ((position + portalCount - 3) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount - 2) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount - 1) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount + 1) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount + 2) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount + 3) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                val shortcutsAtPosition = shortcutsForRound[position].orEmpty()
                shortcutsAtPosition.forEach { (_, target) ->
                    if (seen.add(target)) work.offer(target, distance + 1)
                }
            }
            0
        }
    }

    val withoutShortcuts = sumOfDistances(emptyMap())
    val extraLinkPossibilities = triangularExclusiveSequenceOf(0, portalCount - 1) { a, b -> a to b }.toList()
    val withShortcuts = runBlocking(Dispatchers.Default) {
        extraLinkPossibilities.chunked(Runtime.getRuntime().availableProcessors()).toList().map { options ->
            async {
                options.minOf { (a, b) ->
                    val newShortcuts = shortcuts.plus(listOf(a to b, b to a)).groupBy { it.first }
                    sumOfDistances(newShortcuts)
                }
            }
        }.awaitAll().min()
    }
    return withoutShortcuts - withShortcuts
}


private fun part3(input: List<String>): Int {
    val portalCount = input.first().toIntFromIndex(input.first().lastIndexOf(' ') + 1)
    val shortcuts: List<Pair<Int, Int>> = input.drop(2)
        .map { it.splitToInts(" ") }
        .flatMap { (a, b) -> listOf(a - 1 to b - 1, b - 1 to a - 1) }
        .toPersistentList()

    fun sumOfDistances(shortcutsForRound: Map<Int, List<Pair<Int, Int>>>): Int = (0..<portalCount).sumOf { start ->
        (0..<portalCount).sumOf { end ->
            if (start == end) return@sumOf 0
            val seen = BooleanArray(portalCount)
            val work = TreeQueue(start to 0)
            while (true) {
                val (distance, position) = work.pollEntry()!!
                if (position == end)
                    return@sumOf distance
                ((position + portalCount - 3) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount - 2) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount - 1) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount + 1) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount + 2) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                ((position + portalCount + 3) % portalCount).let { if (seen.add(it)) work.offer(it, distance + 1) }
                val shortcutsAtPosition = shortcutsForRound[position].orEmpty()
                shortcutsAtPosition.forEach { (_, target) ->
                    if (seen.add(target)) work.offer(target, distance + 1)
                }
            }
            0
        }
    }

    val withoutShortcuts = sumOfDistances(emptyMap())

    val extraLinkPossibilities = allPermutationsOfLinks()
    val spacing = portalCount / 8

    val withShortcuts = extraLinkPossibilities.withIndex().minOf { (index, links) ->
        (0..<spacing).minOf { offset ->
            val actualLinks = links.flatMap { (a, b) -> listOf((a * spacing + offset) to (b * spacing + offset), (b * spacing + offset) to (a * spacing + offset)) }
            val newShortcuts = shortcuts.plus(actualLinks).groupBy { it.first }
            sumOfDistances(newShortcuts)
        }
    }

    return withoutShortcuts - withShortcuts
}

private fun allPermutationsOfLinks(
    soFar: List<Pair<Int, Int>> = emptyList(),
    remaining: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6, 7),
    results: MutableList<List<Pair<Int, Int>>> = ArrayList()
): List<List<Pair<Int, Int>>> {
    if (remaining.isEmpty()) {
        results.add(soFar)
        return results
    }
    val head = remaining.first()
    val tail = remaining.tail()
    tail.forEach {
        allPermutationsOfLinks(soFar.plus(head to it), tail.minus(it), results)
    }
    return results
}

private fun BooleanArray.add(x: Int): Boolean {
    if (this[x]) return false
    this[x] = true
    return true
}
