package ec2025.day2

import common.*

private val examples = loadFilesToLines("ec2025/day2", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day2", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day2.assertCorrect()
    benchmark { part1(puzzles[0]) } // 4.9µs
    benchmark(100) { part2(puzzles[1]) } // 14.3ms
    benchmark(1) { part3(puzzles[2]) } // 1.39s
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check("[357,862]", "P1 Example") { part1(examples[0]) }
        check("[140021,700015]", "P1 Puzzle") { part1(puzzles[0]) }

        check(4076, "P2 Example") { part2(examples[1]) }
        check(1387, "P2 Puzzle") { part2(puzzles[1]) }

        check(406954, "P3 Example") { part3(examples[2]) }
        check(136830, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String {
    val a = parseInput(input)
    var r = Pair(0L, 0L)
    repeat(3) {
        r *= r
        r /= Pair(10L, 10L)
        r += a
    }
    return "[${r.first},${r.second}]"
}

private fun part2(input: List<String>): Int = filterGrid(input, 10)
private fun part3(input: List<String>): Int = filterGrid(input, 1)


private fun filterGrid(input: List<String>, step: Long): Int {
    val a = parseInput(input)
    return countCartesianProductOf(
        LongProgression.fromClosedRange(a.first, a.first + 1000, step),
        LongProgression.fromClosedRange(a.second, a.second + 1000, step)
    ) { row, col ->
        val point = Pair(row, col)
        var r = Pair(0L, 0L)
        repeat(100) {
            r *= r
            r /= Pair(100_000L, 100_000L)
            r += point
            if (r.first < -1_000_000 || r.first > 1_000_000 || r.second < -1_000_000 || r.second > 1_000_000)
                return@countCartesianProductOf false
        }
        true
    }
}

private operator fun Pair<Long, Long>.times(n: Pair<Long, Long>): Pair<Long, Long> =
    Pair(this.first * n.first - this.second * n.second, this.first * n.second + this.second * n.first)

private operator fun Pair<Long, Long>.div(n: Pair<Long, Long>): Pair<Long, Long> =
    Pair(this.first / n.first, this.second / n.second)

private operator fun Pair<Long, Long>.plus(n: Pair<Long, Long>): Pair<Long, Long> =
    Pair(this.first + n.first, this.second + n.second)

private fun parseInput(input: List<String>): Pair<Long, Long> = Pair(
    input[0].toLongFromIndex(3),
    input[0].toLongFromIndex(input[0].indexOf(',', 4) + 1)
)
