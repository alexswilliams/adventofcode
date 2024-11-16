package ec2024.day10

import common.*
import kotlin.test.assertEquals

private const val rootFolder = "ec2024/day10"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day10.assertPart1Correct()
    Day10.assertPart2Correct()
    Day10.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 32µs
    benchmark { part2(puzzle2Input) } // 1.23ms
    benchmark { part3(puzzle3Input) } //
}

internal object Day10 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals("PTBVRCZHFLJWGMNS", it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals("FHMTJRGZSLKWCXVD", it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(1851, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(195008, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(3889, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(0, it) }
    }
}


private fun part1(input: List<String>): String {
    return squareToRuneWord(input)
}

private fun squareToRuneWord(input: List<String>): String {
    val h = input.subList(2, 6).map { it.filterNot { ch -> ch == '.' }.toSet() }
    val v = input.transposeToStrings().subList(2, 6).map { it.filterNot { ch -> ch == '.' }.toSet() }
    return (0..3).joinToString("") { row -> (0..3).map { col -> (h[row] intersect v[col]).singleOrNull() ?: '.' }.joinToString("") }
}

private fun String.basePower() = this.foldIndexed(0) { index, acc, ch -> acc + (ch - 'A' + 1) * (index + 1) }


private fun part2(input: List<String>): Int {
    val squares = input.chunked(9) { chunk -> chunk.dropLastWhile { string -> string.isBlank() } }.flatMap { strings -> strings.splitOnSpaces().transpose() }
    val words = squares.map { grid -> squareToRuneWord(grid).basePower() }
    return words.sum()
}

private fun part3(input: List<String>): Int {
    val rows = input.windowed(8, 6)
    val squares = rows.flatMap { strings -> strings.map { it.windowed(8, 6) }.transpose() }
    val partWords = squares.map { strings -> squareToRuneWordAndSolveMissingBits(strings) }
    return partWords.filterNot { '.' in it }.sumOf { it.basePower() }
}

private fun squareToRuneWordAndSolveMissingBits(input: List<String>): String {
    val h = input.subList(2, 6).map { it.filterNot { ch -> ch == '.' }.toSet() }
    val v = input.transposeToStrings().subList(2, 6).map { it.filterNot { ch -> ch == '.' }.toSet() }
    val innerWithGaps = (0..3).flatMap { row -> (0..3).map { col -> (row to col) to ((h[row] intersect v[col]).singleOrNull() ?: '.') } }.toMap()
    val inner = (0..3).map { row ->
        (0..3).map { col ->
            val existing = innerWithGaps[row to col]
            if (existing != '.') existing else {
                if ('?' in h[row]) v[col].subtract(innerWithGaps.filter { (pos, _) -> pos.second == col }.values).singleOrNull() ?: '.'
                else if ('?' in v[col]) h[row].subtract(innerWithGaps.filter { (pos, _) -> pos.first == row }.values).singleOrNull() ?: '.'
                else '.'
            }
        }.joinToString("")
    }
    println(innerWithGaps)
    println(squareToRuneWord(input))
    println(inner.joinToString(""))
    return inner.joinToString("")
}
