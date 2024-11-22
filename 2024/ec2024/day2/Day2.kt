package ec2024.day2

import common.*
import kotlin.test.*

private const val rootFolder = "ec2024/day2"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val exampleBInput = "$rootFolder/example1B.txt".fromClasspathFileToLines()
private val exampleCInput = "$rootFolder/example1C.txt".fromClasspathFileToLines()
private val exampleDInput = "$rootFolder/example1D.txt".fromClasspathFileToLines()
private val example2Input = "$rootFolder/example2.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day2.assertPart1Correct()
    Day2.assertPart2Correct()
    Day2.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 42µs
    benchmark(50) { part2(puzzle2Input) } // 11ms
    benchmark(50) { part3(puzzle3Input) } // 37ms
}

internal object Day2 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(4, it) }
        part1(exampleBInput).also { println("[Example] Part 1B: $it") }.also { assertEquals(3, it) }
        part1(exampleCInput).also { println("[Example] Part 1C: $it") }.also { assertEquals(2, it) }
        part1(exampleDInput).also { println("[Example] Part 1D: $it") }.also { assertEquals(3, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(36, it) }
    }

    override fun assertPart2Correct() {
        part2(example2Input).also { println("[Example] Part 2: $it") }.also { assertEquals(37, it) }
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(5198, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(10, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(11222, it) }
    }
}


private fun part1(input: List<String>): Int {
    val runes = input[0].removePrefix("WORDS:").split(',')
    return input[2].split(' ').sumOf { word -> runes.sumOf { word.countOccurrences(it) } }
}

private fun part2(input: List<String>): Int {
    val runes = input[0].removePrefix("WORDS:").split(',')
    return input.drop(2).splitOnSpaces().sumOf { line ->
        line.sumOf { word ->
            val results = mutableSetOf<Int>()
            runes.forEach { runeWord ->
                findPositions(word, runeWord, results::add)
                findPositions(word, runeWord.reversed(), results::add)
            }
            results.size
        }
    }
}

private fun part3(input: List<String>): Int {
    val runes = input[0].removePrefix("WORDS:").split(',')
    val longestRuneLength = runes.maxOf { it.length }
    val grid = input.drop(2)

    val scalePositions = mutableSetOf<Long>()

    grid.forEachIndexed { row, line ->
        val wrappedLine = line + line.substring(0, longestRuneLength)
        val reversedLine = line.reversed()
        val wrappedReversedLine = reversedLine + reversedLine.substring(0, longestRuneLength)
        runes.forEach { runeWord -> findPositions(wrappedLine, runeWord) { col -> scalePositions.add(row by col % line.length) } }
        runes.forEach { runeWord -> findPositions(wrappedReversedLine, runeWord) { col -> scalePositions.add(row by line.lastIndex - (col % line.length)) } }
    }
    grid.transposeToStrings().forEachIndexed { col, line ->
        val reversedLine = line.reversed()
        runes.forEach { runeWord -> findPositions(line, runeWord) { row -> scalePositions.add(row by col) } }
        runes.forEach { runeWord -> findPositions(reversedLine, runeWord) { row -> scalePositions.add(line.lastIndex - row by col) } }
    }
    return scalePositions.count()
}

private fun findPositions(line: String, runeWord: String, onFound: (Int) -> Unit) {
    line.indices.forEach { index ->
        if (line.startsWith(runeWord, index)) {
            runeWord.indices.forEach { runeIndex -> onFound(runeIndex + index) }
        }
    }
}
