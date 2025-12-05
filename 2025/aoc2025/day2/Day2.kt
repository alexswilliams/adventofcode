package aoc2025.day2

import common.*
import java.util.regex.*

private val example = loadFiles("aoc2025/day2", "example.txt").single()
private val puzzle = loadFiles("aoc2025/day2", "input.txt").single()

internal fun main() {
    Day2.assertCorrect()
    Day2.assertSlowSameAsFast()
    benchmark(10) { part1regex(puzzle) } // 258.6ms
    benchmark { part1(puzzle) } // 20.3µs
    benchmark(10) { part2regex(puzzle) } // 366.2ms
    benchmark { part2(puzzle) } // 51.2µs
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check(1227775554, "P1 Example") { part1(example) }
        check(30608905813, "P1 Puzzle") { part1(puzzle) }

        check(4174379265, "P2 Example") { part2(example) }
        check(31898925685, "P2 Puzzle") { part2(puzzle) }
    }

    fun assertSlowSameAsFast() {
        check(1227775554, "P1 Example") { part1regex(example) }
        check(30608905813, "P1 Puzzle") { part1regex(puzzle) }

        check(4174379265, "P2 Example") { part2regex(example) }
        check(31898925685, "P2 Puzzle") { part2regex(puzzle) }
    }
}

private fun part1regex(input: String): Long =
    input.split(',')
        .flatMap { rangeString ->
            rangeString.split('-').let { (lo, hi) ->
                if (lo.length % 2 == 1 && lo.length == hi.length) emptyList()
                else LongRange(lo.toLong(), hi.toLong())
                    .filter { Pattern.matches("(.+)\\1", it.toString()) }
            }
        }.sum()

private fun part2regex(input: String): Long =
    input.split(',')
        .flatMap { rangeString ->
            rangeString.split('-').let { (lo, hi) ->
                LongRange(lo.toLong(), hi.toLong())
                    .filter { Pattern.matches("(.+)\\1+", it.toString()) }
            }
        }.sum()


private fun part1(input: String): Long =
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
                    val prefix = (max - min + 1) * (min + max) / 2
                    prefix * 10L.pow(prefixLen) + prefix
                }
        }

private fun part2(input: String): Long =
    input.split(',').map { it.split('-') }
        .sumOf { (lo, hi) ->
            // Split ranges spanning different length numbers into those different lengths, e.g. 95-110 will be done as 95-99 and 100-110 as though they're two separate ranges
            (lo.length..hi.length)
                .sumOf { targetLength ->
                    // The only patterns that could repeat completely are those whose length is a divisor of the total length - e.g. if your target
                    // length is 8, you don't need to check repeating patterns of length 3, just of 1, 2 and 4.
                    val factorLengths = factorsOfExcl(targetLength)
                    val sumsByPrefixLength = factorLengths.associateWith { prefixLen ->
                        val repeats = targetLength / prefixLen
                        val multiplier = 10L.pow(prefixLen)
                        // Build a range that's initially e.g. 111..999, and then shrink it to fit the actual range
                        val start = if (targetLength == lo.length) lo.substring(0, prefixLen).toLong() else 10L.pow(prefixLen - 1)
                        val end = if (targetLength == hi.length) hi.substring(0, prefixLen).toLong() else multiplier - 1
                        // It's possible for the first or last value to be in-range for the first $prefixLen digits, but out-of-range when extended to the full length;
                        // if these are found then contract the range by 1 as needed.
                        val startTail = (2..repeats).fold(0L) { acc, _ -> acc * multiplier + start }
                        val endTail = (2..repeats).fold(0L) { acc, _ -> acc * multiplier + end }
                        val startOutOfRange = (targetLength == lo.length && lo.toLongFromIndex(prefixLen) > startTail)
                        val endOutOfRange = (targetLength == hi.length && hi.toLongFromIndex(prefixLen) < endTail)
                        val min = start + if (startOutOfRange) 1 else 0
                        val max = end - if (endOutOfRange) 1 else 0
                        // Instead of building every number using shifting and then summing, you can do the summing and THEN do the shifting - this means
                        // the numbers to be summed are contiguous, and so the sum-of-natural-numbers formula can be used (i.e. n(n+1)/2, but shifted by a fixed amount)
                        if (min > max) 0
                        else ((max - min + 1) * (min + max) / 2)
                            .let { prefix -> (1..repeats).fold(0L) { acc, _ -> acc * multiplier + prefix } }
                    }
                    // Each of these prefix lengths might contain unique numbers, or they might contain numbers repeated in other prefix lengths, for example
                    // 1 could lead to 111111, 11 could also lead to 111111 and 111 could also lead to 111111.  However, 12 leading to 121212 could never feature
                    // in the set of 3-long prefixes.  Theory: for prefix lengths A < B where A divides B, the set of all numbers built from A-length prefixes
                    // will be completely contained within the set of numbers built from B-length prefixes, and so you can subtract the sum of the smaller set from
                    // the total, to remove this double-counting.
                    factorLengths.sumOf { prefixLen ->
                        val sumOfThisPrefix = sumsByPrefixLength[prefixLen]!!
                        sumOfThisPrefix - factorLengths
                            .filter { it < prefixLen && (it == 1 || gcd(prefixLen, it) != 1) }
                            .sumOf { sumsByPrefixLength[it]!! }
                    }
                }
        }

private val FACTORS = Array(10) { i -> (1..(i / 2)).filter { i % it == 0 } }
private fun factorsOfExcl(i: Int): List<Int> =
    if (i <= FACTORS.lastIndex) FACTORS[i] else (1..(i / 2)).filter { i % it == 0 }
