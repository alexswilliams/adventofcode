package aoc2025.day2

import common.*
import java.util.regex.*

private val example = loadFiles("aoc2025/day2", "example.txt").single()
private val puzzle = loadFiles("aoc2025/day2", "input.txt").single()

internal fun main() {
    Day2.assertCorrect()
//    benchmark(10) { part1(puzzle) } // 258.6ms
    benchmark { part1fast(puzzle) } // 20.3µs
//    benchmark(10) { part2(puzzle) } // 366.2ms
    benchmark(10) { part2fast(puzzle) } // 249.1µs
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check(1227775554, "P1 Example") { part1(example) }
        check(1227775554, "P1 Example") { part1fast(example) }
        check(30608905813, "P1 Puzzle") { part1(puzzle) }
        check(30608905813, "P1 Puzzle") { part1fast(puzzle) }

        check(4174379265, "P2 Example") { part2(example) }
        check(4174379265, "P2 Example") { part2fast(example) }
        check(31898925685, "P2 Puzzle") { part2(puzzle) }
        check(31898925685, "P2 Puzzle") { part2fast(puzzle) }
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
                    val min = start + if (startOutOfRange) 1 else 0
                    val max = end - if (endOutOfRange) 1 else 0
                    // 1+2+3+4+...+n = n(n+1)/2
                    // 11+12+13+14+...+(10+n) = (x+1)+(x+2)+...+(x+n) = nx + n(n+1)/2 = n(2x+n+1)/2 = (max-min+1)(min+max)/2 when n=max-min+1, x=min-1
                    val sum = (max - min + 1) * (min + max) / 2
                    sum * 10L.pow(prefixLen) + sum
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

private fun part2fast(input: String): Long =
    input.split(',').map { it.split('-') }
        .sumOf { (lo, hi) ->
            (lo.length..hi.length)
                .sumOf { targetLength ->
                    val factorLengths = factorsOfExcl(targetLength)
                    val sumsByPrefixLength = factorLengths.associateWith { prefixLen ->
                        val repeats = targetLength / prefixLen
                        val multiplier = 10L.pow(prefixLen)

                        val start = if (targetLength == lo.length) lo.substring(0, prefixLen).toLong() else 10L.pow(prefixLen - 1)
                        val end = if (targetLength == hi.length) hi.substring(0, prefixLen).toLong() else multiplier - 1
                        val startTail = (2..repeats).fold(0L) { acc, _ -> acc * multiplier + start }
                        val endTail = (2..repeats).fold(0L) { acc, _ -> acc * multiplier + end }
                        val startOutOfRange = (targetLength == lo.length && lo.toLongFromIndex(prefixLen) > startTail)
                        val endOutOfRange = (targetLength == hi.length && hi.toLongFromIndex(prefixLen) < endTail)
                        val min = start + if (startOutOfRange) 1 else 0
                        val max = end - if (endOutOfRange) 1 else 0

                        val sum = if (min > max) 0 else (max - min + 1) * (min + max) / 2
                        (1..repeats).fold(0L) { acc, _ -> acc * multiplier + sum }
                    }
                    factorLengths.sumOf { prefixLen ->
                        val sumOfThisPrefix = sumsByPrefixLength[prefixLen]!!
                        sumOfThisPrefix - factorLengths.takeWhile { it < prefixLen }
                            .filter { it == 1 || gcd(prefixLen, it) != 1 }
                            .sumOf { sumsByPrefixLength[it]!! }
                    }
                }
        }

private fun factorsOfExcl(i: Int): List<Int> = (1..(i / 2)).filter { i % it == 0 }

private fun part2(input: String): Long =
    input.split(',')
        .flatMap { rangeString ->
            rangeString.split('-').let { (lo, hi) ->
                LongRange(lo.toLong(), hi.toLong())
                    .filter { Pattern.matches("(.+)\\1+", it.toString()) }
            }
        }.sum()
