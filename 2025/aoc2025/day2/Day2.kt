package aoc2025.day2

import common.*
import java.util.regex.*

private val example = loadFiles("aoc2025/day2", "example.txt").single()
private val puzzle = loadFiles("aoc2025/day2", "input.txt").single()

internal fun main() {
    Day2.assertCorrect()
    benchmark(10) { part1(puzzle) } // 258.6ms
    benchmark(10) { part2(puzzle) } // 366.2ms
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check(1227775554, "P1 Example") { part1(example) }
        check(30608905813, "P1 Puzzle") { part1(puzzle) } // 30608905835 too high

        check(4174379265, "P2 Example") { part2(example) }
        check(31898925685, "P2 Puzzle") { part2(puzzle) }
    }
}


// You can halve the time by not using a regex and splitting the string exactly in half (.substring(0,length+1/2)), but i like the symmetry between the two parts.
// It could get even faster if you only iterate on first-halves of the string, but I'm currently at work and can't justify spending time on it :P
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
