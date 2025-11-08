package ec2025.day5

import common.*
import java.util.stream.*

private val examples = loadFilesToLines("ec2025/day5", "example1.txt", "example2.txt", "example3a.txt", "example3b.txt")
private val puzzles = loadFilesToLines("ec2025/day5", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzles[0]) } // 4.7µs
    benchmark { part2(puzzles[1]) } // 164.1µs
    benchmark { part3(puzzles[2]) } // 858.6µs
}

internal object Day5 : Challenge {
    override fun assertCorrect() {
        check(581078, "P1 Example") { part1(examples[0]) }
        check(6275436462, "P1 Puzzle") { part1(puzzles[0]) }

        check(77053, "P2 Example") { part2(examples[1]) }
        check(8842877958333, "P2 Puzzle") { part2(puzzles[1]) }

        check(260, "P3 Example A") { part3(examples[2]) }
        check(4, "P3 Example B") { part3(examples[3]) }
        check(30957369, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): Long =
    buildFishbone(input[0]).quality10()

private fun part2(input: List<String>): Long =
    input.map { buildFishbone(it).quality() }
        .stream().collect(Collectors.summarizingLong { it })
        .let { it.max - it.min }

private fun part3(input: List<String>): Int =
    input.map {
        with(buildFishbone(it)) {
            Sword(it.toIntFromIndex(0), quality(), map { segment -> segment.numericValue() })
        }
    }.sortedWith { a, b ->
        b.quality.compareTo(a.quality).also { if (it != 0) return@sortedWith it }
        b.segmentValues.forEachIndexed { index, v -> v.compareTo(a.segmentValues[index]).also { if (it != 0) return@sortedWith it } }
        b.id.compareTo(a.id)
    }.mapIndexed { index, sword -> (index + 1) * sword.id }.sum()


private data class Sword(val id: Int, val quality: Long, val segmentValues: List<Int>)

// The input only contains 1..10 as numbers; you can represent 10 as eg 'A' as it's a higher ascii value than '9'.  Only the example of part 1 uses a 10.
private fun List<Segment>.quality10(): Long = fold(0L) { acc, segment -> if (segment[1] == 'A') acc * 100 + 10 else acc * 10 + (segment[1] - '0') }
private fun List<Segment>.quality(): Long = fold(0L) { acc, segment -> acc * 10 + (segment[1] - '0') }
private fun Segment.numericValue(): Int = this.toIntFromIndex(0)
private typealias Segment = CharArray

private fun buildFishbone(swordSpec: String): List<Segment> {
    val numbers = parseNumbers(swordSpec)
    val fishbone = ArrayList<Segment>(25).apply { add(charArrayOf('0', numbers.first(), ' ')) }

    var startSegmentIndex = 0
    nextNumber@ for (n in 1..numbers.lastIndex) {
        val startSegment = fishbone[startSegmentIndex]
        if ((startSegment[0] != '0' || startSegment[1] == '1') && (startSegment[2] != ' ' || startSegment[1] == '9'))
            startSegmentIndex++

        for (i in startSegmentIndex..fishbone.lastIndex) {
            if (placeCharInSegment(fishbone[i], numbers[n])) continue@nextNumber
        }
        // The leading 0 means no branching is needed in the start value of toIntFromIndex
        fishbone.add(charArrayOf('0', numbers[n], ' '))
    }
    return fishbone
}

private fun parseNumbers(input: String): List<Char> =
    input.splitMappingRanges(",", input.indexOf(':') + 1) { _, start, end ->
        if (end - start == 1) 'A'
        else input[start]
    }

private fun placeCharInSegment(segment: Segment, char: Char) =
    if (segment[0] == '0' && char < segment[1]) {
        segment[0] = char
        true
    } else if (segment[2] == ' ' && char > segment[1]) {
        segment[2] = char
        true
    } else false
