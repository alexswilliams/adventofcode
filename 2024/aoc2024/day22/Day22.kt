package aoc2024.day22

import common.*

private val examples = loadFilesToLines("aoc2024/day22", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("aoc2024/day22", "input.txt").single()

internal fun main() {
    Day22.assertCorrect()
    benchmark(100) { part1(puzzle) } // 9.2ms
    benchmark(100) { part2(puzzle) } // 31.2ms
}

internal object Day22 : Challenge {
    override fun assertCorrect() {
        check(37327623, "P1 Example") { part1(examples[0]) }
        check(15613157363, "P1 Puzzle") { part1(puzzle) }

        check(23, "P2 Example") { part2(examples[1]) }
        check(1784, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Long =
    input.sumOf { seed ->
        (1..2000).fold(seed.toLong()) { acc, _ -> evolve(acc) }
    }

private fun part2(input: List<String>): Int {
    val bananasForSequence = IntArray(HASH_MOD)
    var maxSeen = Int.MIN_VALUE
    fun buyBananas(sequenceHash: Int, price: Int) {
        bananasForSequence[sequenceHash] = (bananasForSequence[sequenceHash] + price).also { if (it > maxSeen) maxSeen = it }
    }

    input.forEach { seed ->
        val seenThisRun = BooleanArray(HASH_MOD)
        val differences = CyclicBuffer4(first4Differences(seed.toLong()))
        var lastSecret = evolve(seed.toLong(), 4)

        seenThisRun[differences.hash] = true
        buyBananas(differences.hash, priceOf(lastSecret))
        repeat(1996) {
            lastSecret = evolve(lastSecret).also { secret ->
                differences.push(priceOf(secret) - priceOf(lastSecret))
                if (!seenThisRun[differences.hash]) {
                    seenThisRun[differences.hash] = true
                    buyBananas(differences.hash, priceOf(secret))
                }
            }
        }
    }
    return maxSeen
}

private fun priceOf(lastSecret: Long) = (lastSecret % 10).toInt()

private fun first4Differences(seed: Long): IntArray =
    IntArray(4) { (evolve(seed, it + 1) % 10 - evolve(seed, it) % 10).toInt() }

private fun evolve(i: Long): Long {
    val a = i shl 6 xor i and 0x00ff_ffff
    val b = a shr 5 xor a and 0x00ff_ffff
    return b shl 11 xor b and 0x00ff_ffff
}

private tailrec fun evolve(i: Long, times: Int): Long =
    if (times == 0) i
    else evolve(evolve(i), times - 1)


private const val HASH_MOD = 19 * 19 * 19 * 19

private class CyclicBuffer4(initialValues: IntArray) {
    var hash = initialValues.indices.fold(0) { acc, i -> acc * 19 + (initialValues[i] + 9) } // -9..9 has 19 values not 18, 0 is a real value too!
        private set

    fun push(i: Int) {
        hash = (hash * 19 + i + 9) % HASH_MOD
    }
}
