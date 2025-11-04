package ec2025.day1

import common.*

private val examples = loadFilesToLines("ec2025/day1", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day1", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    DayX.assertCorrect()
    benchmark(100) { part1(puzzles[0]) } // 31.2µs
    benchmark(100) { part2(puzzles[1]) } // 31.3µs
    benchmark(100) { part3(puzzles[2]) } // 39.3µs
}

internal object DayX : Challenge {
    override fun assertCorrect() {
        check("Fyrryn", "P1 Example") { part1(examples[0]) }
        check("Lorcyth", "P1 Puzzle") { part1(puzzles[0]) }

        check("Elarzris", "P2 Example") { part2(examples[1]) }
        check("Vornrovan", "P2 Puzzle") { part2(puzzles[1]) }

        check("Drakzyph", "P3 Example") { part3(examples[2]) }
        check("Elthal", "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String {
    val names = input[0].split(",")
    val pos = parseMoves(input).fold(0) { acc, (dir, dist) -> rotate(dir, acc, dist).coerceIn(0, names.lastIndex) }
    return names[pos]
}

private fun part2(input: List<String>): String {
    val names = input[0].split(",")
    val pos = parseMoves(input).fold(0) { acc, (dir, dist) -> ((rotate(dir, acc, dist)) + names.size * dist) % names.size }
    return names[pos]
}

private fun part3(input: List<String>): String {
    val names = input[0].split(",") as MutableList<String>
    for ((dir, dist) in parseMoves(input))
        names.swap(0, ((rotate(dir, 0, dist)) + names.size * dist) % names.size)
    return names[0]
}


private fun parseMoves(input: List<String>): List<Pair<Char, Int>> =
    input.last().split(",").map { it[0] to it.toIntFromIndex(1) }

private fun rotate(dir: Char, acc: Int, dist: Int): Int =
    if (dir == 'L') (acc - dist) else (acc + dist)

