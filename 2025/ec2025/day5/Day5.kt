package ec2025.day5

import common.*
import java.util.stream.*

private val examples = loadFilesToLines("ec2025/day5", "example1.txt", "example2.txt", "example3a.txt", "example3b.txt")
private val puzzles = loadFilesToLines("ec2025/day5", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzles[0]) } // 10.0µs
    benchmark { part2(puzzles[1]) } // 349.1µs
    benchmark(100) { part3(puzzles[2]) } // 3.2ms
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
    buildSpine(input[0].splitMappingRanges(",", 3) { s, start, _ -> s.toIntFromIndex(start) })
        .quality()

private fun part2(input: List<String>): Long =
    input.map { it.splitMappingRanges(",", it.indexOf(':') + 1) { s, start, _ -> s.toIntFromIndex(start) } }
        .map { buildSpine(it).joinToString("") { segment -> segment.centre.toString() }.toLong() }
        .stream().collect(Collectors.summarizingLong { it })
        .let { it.max - it.min }

private fun part3(input: List<String>): Int =
    input.map { line ->
        line.split(':')
            .let { it[0].toInt() to buildSpine(it[1].splitToInts(",")) }
            .let { (id, spine) -> Sword(id = id, quality = spine.quality(), segmentValues = spine.map { it.numericValue() }) }
    }.sorted().mapIndexed { index, sword -> (index + 1) * sword.id }.sum()


private data class Sword(val id: Int, val quality: Long, val segmentValues: List<Int>) : Comparable<Sword> {
    override fun compareTo(other: Sword): Int {
        other.quality.compareTo(quality).also { if (it != 0) return it }
        other.segmentValues.forEachIndexed { index, v -> v.compareTo(segmentValues[index]).also { if (it != 0) return it } }
        return other.id.compareTo(id)
    }
}

private fun List<Segment>.quality(): Long = joinToString("") { it.centre.toString() }.toLong()
private data class Segment(val left: Int?, val centre: Int, val right: Int?) {
    fun numericValue(): Int = "${left ?: ""}${centre}${right ?: ""}".toInt()
}

private fun buildSpine(numbers: List<Int>): List<Segment> {
    val spine = mutableListOf(Segment(null, numbers.first(), null))
    nextNumber@ for (number in numbers.drop(1)) {
        spine.forEachIndexed { index, segment ->
            if (segment.left == null && number < segment.centre) {
                spine[index] = segment.copy(left = number)
                continue@nextNumber
            }
            if (segment.right == null && number > segment.centre) {
                spine[index] = segment.copy(right = number)
                continue@nextNumber
            }
        }
        spine.add(Segment(null, number, null))
    }
    return spine
}
