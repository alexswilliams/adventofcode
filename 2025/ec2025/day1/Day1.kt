package ec2025.day1

import common.*

private val examples = loadFilesToLines("ec2025/day1", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day1", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    DayX.assertCorrect()
    benchmark { part1(puzzles[0]) }
    benchmark { part2(puzzles[1]) }
    benchmark(100) { part3(puzzles[2]) }
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
    val names = input.first().split(",")
    val instructions = input.last().split(",").map { it.first() to it.drop(1).toInt() }
    var pos = 0
    for ((direction, distance) in instructions) {
        pos = (if (direction == 'L') (pos - distance) else (pos + distance)).coerceIn(0, instructions.lastIndex)
    }
    return names[pos]
}

private fun part2(input: List<String>): String {
    val names = input.first().split(",")
    val instructions = input.last().split(",").map { it.first() to it.drop(1).toInt() }
    var pos = 0
    for ((direction, distance) in instructions) {
        pos = ((if (direction == 'L') (pos - distance) else (pos + distance)) + names.size * 100) % names.size
    }
    return names[pos]
}

private fun part3(input: List<String>): String {
    val names = input.first().split(",").toMutableList()
    val instructions = input.last().split(",").map { it.first() to it.drop(1).toInt() }
    for ((direction, distance) in instructions) {
        val top = names[0]
        val position = ((if (direction == 'L') -distance else +distance) + names.size * 100) % names.size
        names[0] = names[position]
        names[position] = top
    }
    return names[0]
}
