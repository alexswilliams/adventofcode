package aoc2025.day2

import common.*
import java.util.regex.*

private val example = loadFiles("aoc2025/day2", "example.txt").single()
private val puzzle = loadFiles("aoc2025/day2", "input.txt").single()

internal fun main() {
    Day2.assertCorrect()
    benchmark(10) { part1(puzzle) } // 258.6ms
    benchmark { part1fast(puzzle) } // 34.4µs
    benchmark(10) { part2(puzzle) } // 366.2ms
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check(1227775554, "P1 Example") { part1(example) }
        check(1227775554, "P1 Example") { part1fast(example) }
        check(30608905813, "P1 Puzzle") { part1(puzzle) }
        check(30608905813, "P1 Puzzle") { part1fast(puzzle) }

        check(4174379265, "P2 Example") { part2(example) }
        check(31898925685, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1fast(input: String): Long =
    input.split(',').map { it.split('-') }
        .sumOf { (lo, hi) ->
            (lo.length..hi.length)
                .filter { it % 2 == 0 }.map { it / 2 }
                .sumOf { prefixLen ->
                    // E.g. for overall length 4, this gives prefixes of 10..99, which when duplicated produces 1010, 1111, 1212, 1313 ... 9898, 9999.
                    // If overall length matches the lowest id length, you can skip every prefix before the lowest id's first half; end is similarly truncated.
                    // If the second half of the lowest/highest id falls outside the duplicate range, the prefix range can be shrunk by 1 to exclude it.
                    val start = if (prefixLen * 2 == lo.length) lo.substring(0, prefixLen).toLong() else 10L.pow(prefixLen - 1)
                    val end = if (prefixLen * 2 == hi.length) hi.substring(0, prefixLen).toLong() else 10L.pow(prefixLen) - 1
                    val startOutOfRange = (prefixLen * 2 == lo.length && lo.toLongFromIndex(prefixLen) > start)
                    val endOutOfRange = (prefixLen * 2 == hi.length && hi.toLongFromIndex(prefixLen) < end)
                    LongRange(
                        start + if (startOutOfRange) 1 else 0,
                        end - if (endOutOfRange) 1 else 0
                    ).sumOf { it * 10L.pow(prefixLen) + it }
                }
        }

private fun part1(input: String): Long =
    input.split(',')
        .flatMap { rangeString ->
            rangeString.split('-').let { (lo, hi) ->
                if (lo.length % 2 == 1 && lo.length == hi.length) emptyList()
                else LongRange(lo.toLong(), hi.toLong())
                    .filter { Pattern.matches("(.+)\\1", it.toString()) }
            }
        }.sum()

private fun part2(input: String): Long =
    input.split(',')
        .flatMap { rangeString ->
            rangeString.split('-').let { (lo, hi) ->
                LongRange(lo.toLong(), hi.toLong())
                    .filter { Pattern.matches("(.+)\\1+", it.toString()) }
            }
        }.sum()
