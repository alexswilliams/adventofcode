package aoc2022.day13

import common.*

private val example = loadFilesToLines("aoc2022/day13", "example.txt").single().map { it.toCharArray() }
private val puzzle = loadFilesToLines("aoc2022/day13", "input.txt").single().map { it.toCharArray() }

internal fun main() {
    Day13.assertCorrect()
    benchmark { part1(puzzle) } // 22.4µs
    benchmark { part2(puzzle) } // 85.2µs
}

internal object Day13 : Challenge {
    override fun assertCorrect() {
        check(13, "P1 Example") { part1(example) }
        check(5503, "P1 Puzzle") { part1(puzzle) }

        check(140, "P2 Example") { part2(example) }
        check(20952, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<CharArray>) = input
    .chunked(3)
    .mapIndexed { index, it -> if (packetComparator(it[0], it[1]) < 0) index + 1 else 0 }
    .sum()

private val beacon2 = "[[2]]".toCharArray()
private val beacon6 = "[[6]]".toCharArray()
private fun part2(input: List<CharArray>) = input
    .filterNot { it.isEmpty() }
    .plus(listOf(beacon2, beacon6))
    .sortedWith(::packetComparator)
    .let { (it.indexOf(beacon2) + 1) * (it.indexOf(beacon6) + 1) }


private fun packetComparator(sL: CharArray, sR: CharArray): Int {
    var leftIndex = 0
    var rightIndex = 0
    var openL = 0 // extra bracket pairs only wrap a single token, so they can be tracked with just a count
    var closeL = 0
    var openR = 0
    var closeR = 0
    while (true) {
        val left = if (openL > 0) '[' else if (closeL > 0 && sL[leftIndex] !in '0'..'9') ']' else sL[leftIndex]
        val right = if (openR > 0) '[' else if (closeR > 0 && sR[rightIndex] !in '0'..'9') ']' else sR[rightIndex]
        when {
            left in '0'..'9' && right in '0'..'9' -> {
                val lInt = consumeNumber(sL, leftIndex + 1, left).also { leftIndex = it and 0xffff } shr 16
                val rInt = consumeNumber(sR, rightIndex + 1, right).also { rightIndex = it and 0xffff } shr 16
                if (lInt != rInt) return lInt - rInt
            }

            left == right -> {
                if (openL > 0) openL-- else if (closeL > 0) closeL-- else leftIndex++
                if (openR > 0) openR-- else if (closeR > 0) closeR-- else rightIndex++
            }

            left == '[' && right in '0'..'9' -> openR++.also { closeR++ }
            right == '[' && left in '0'..'9' -> openL++.also { closeL++ }

            left == ']' || right == ']' -> return if (left == ']') -1 else 1
        }
    }
}

// Consumes a ONE- or TWO-digit number (which is all the puzzle seems to present) into the upper 2 bytes of the return value.
// The index to resume from is given in the lower 2 bytes.  Could be generalised by turning the `if` into `while` with the same condition and
// accumulating with x = x * 10 + v
// Assumes that none of the lines are > 64k characters long, and none of the values are > 99
private fun consumeNumber(s: CharArray, nextIndex: Int, firstDigit: Char): Int {
    val next = s[nextIndex]
    if (next in '0'..'9')
        return (firstDigit.code shl 24) + (next.code shl 16) + nextIndex + 1
    return (firstDigit.code shl 16) + nextIndex
}

