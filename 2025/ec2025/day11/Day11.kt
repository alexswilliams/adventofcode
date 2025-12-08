package ec2025.day11

import common.*

private val examples = loadFilesToLines("ec2025/day11", "example1.txt", "example2.txt")
private val puzzles = loadFilesToLines("ec2025/day11", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day11.assertCorrect()
    benchmark { part1(puzzles[0]) } // 5.2µs
    benchmark(10) { part2(puzzles[1]) } // 229.4ms
    benchmark(10) { part3(puzzles[2]) } // 28.3µs
}

internal object Day11 : Challenge {
    override fun assertCorrect() {
        check(109, "P1 Example") { part1(examples[0]) }
        check(275, "P1 Puzzle") { part1(puzzles[0]) }

        check(11, "P2 Example 1") { part2(examples[0]) }
        check(1579, "P2 Example 2") { part2(examples[1]) }
        check(2617509, "P2 Puzzle") { part2(puzzles[1]) }

        check(136117075560122, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): Int {
    val columns = input.map(String::toInt).toIntArray()
    var changed: Boolean
    var round = 0
    do {
        changed = false
        round++
        (0..<columns.lastIndex).forEach { i ->
            if (columns[i] > columns[i + 1]) {
                columns[i]--
                columns[i + 1]++
                changed = true
            }
        }
    } while (changed)
    round--
    do {
        round++
        (0..<columns.lastIndex).forEach { i ->
            if (columns[i] < columns[i + 1]) {
                columns[i]++
                columns[i + 1]--
            }
        }
    } while (round != 10)
    return columns.sumOfIndexed { index, e -> (index + 1) * e }
}

private fun part2(input: List<String>): Long {
    val columns = input.map(String::toLong).toLongArray()
    // There is a way of skipping large sections of the input by chunking
    // the columns into groups that occur in descending order, and projecting
    // to where they would interact with each other, then "bulk-moving"
    // the ducks within each group, and then merge adjacent groups - iterate
    // until all columns are in ascending order and have stabilised to their
    // average.  In practice, it's much less thinking to test every row.
    var changed: Boolean
    var round = 0
    do {
        changed = false
        round++
        (0..<columns.lastIndex).forEach { i ->
            if (columns[i] > columns[i + 1]) {
                columns[i]--
                columns[i + 1]++
                changed = true
            }
        }
        println("Round $round: ${columns.contentToString()}")
    } while (changed)
    round--

    // Each duck gets shifted one place per turn until the debt of ducks on the left
    // is satisfied by the glut of ducks on the right.  Net, this looks like the
    // largest column donating to the smallest column; so the number of rounds remaining
    // will be the number of ducks that need to shift until all the indebted columns have
    // reached the mean number of ducks.
    val mean = columns.sum() / columns.size
    return round + columns.filter { it < mean }.sumOf { mean - it }
}

private fun part3(input: List<String>): Long =
    // input is already in ascending order, so "phase 1" will pass immediately
    part2(input)
