package ec2024.day2

import common.*

private val examples = loadFilesToLines("ec2024/day2", "example.txt", "example1B.txt", "example1C.txt", "example1D.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day2", "input.txt", "input2.txt", "input3.txt")


internal fun main() {
    Day2.assertCorrect()
    benchmark { part1(puzzles[0]) } // 42µs
    benchmark(50) { part2(puzzles[1]) } // 11ms
    benchmark(50) { part3(puzzles[2]) } // 37ms
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check(4, "P1 Example") { part1(examples[0]) }
        check(3, "P1B Example") { part1(examples[1]) }
        check(2, "P1C Example") { part1(examples[2]) }
        check(3, "P1D Example") { part1(examples[3]) }
        check(36, "P1 Puzzle") { part1(puzzles[0]) }

        check(37, "P2 Example") { part2(examples[4]) }
        check(5198, "P2 Puzzle") { part2(puzzles[1]) }

        check(10, "P3 Example") { part3(examples[5]) }
        check(11222, "P3 Puzzle") { part3(puzzles[2]) }
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
        val wrappedLine = line + line.take(longestRuneLength)
        val reversedLine = line.reversed()
        val wrappedReversedLine = reversedLine + reversedLine.take(longestRuneLength)
        runes.forEach { runeWord -> findPositions(wrappedLine, runeWord) { col -> scalePositions.add(row by col % line.length) } }
        runes.forEach { runeWord -> findPositions(wrappedReversedLine, runeWord) { col -> scalePositions.add(row by line.lastIndex - (col % line.length)) } }
    }
    grid.transposeToStrings().forEachIndexed { col, line ->
        val reversedLine = line.reversed()
        runes.forEach { runeWord -> findPositions(line, runeWord) { row -> scalePositions.add(row by col) } }
        runes.forEach { runeWord -> findPositions(reversedLine, runeWord) { row -> scalePositions.add(line.lastIndex - row by col) } }
    }
    return scalePositions.size
}

private fun findPositions(line: String, runeWord: String, onFound: (Int) -> Unit) {
    line.indices.forEach { index ->
        if (line.startsWith(runeWord, index)) {
            runeWord.indices.forEach { runeIndex -> onFound(runeIndex + index) }
        }
    }
}
