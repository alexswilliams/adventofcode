package aoc2024.day9

import common.*
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.*

private val example = loadFiles("aoc2024/day9", "example.txt").single().toCharArray()
private val puzzle = loadFiles("aoc2024/day9", "input.txt").single().toCharArray()

internal fun main() {
    Day9.assertCorrect()
    benchmark { part1(puzzle) } // 316µs
    benchmark { part2(puzzle) } // 9.3ms
}

internal object Day9 : Challenge {
    override fun assertCorrect() {
        check(60, "P1 Example 0") { part1("12345".toCharArray()) }
        check(1928, "P1 Example 1") { part1(example) }
        check(6310675819476L, "P1 Puzzle") { part1(puzzle) }

        check(2858, "P2 Example") { part2(example) }
        check(6335972980679L, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(input: CharArray): Long {
    val (unmovedFiles, freeSpaceMap) = parseInputToFilesAndFreeSpace(input)
    var checksumForRelocatedFiles = 0L

    while (freeSpaceMap.isNotEmpty() && freeSpaceMap.first().startIndex < unmovedFiles.peek().startIndex) {
        val gap = freeSpaceMap.removeFirst()
        val lastFile = unmovedFiles.pop()
        checksumForRelocatedFiles += checksum(lastFile.id, gap.startIndex, min(gap.length, lastFile.length))

        if (gap.length < lastFile.length)
            unmovedFiles.push(File(lastFile.id, lastFile.startIndex, lastFile.length - gap.length))
        else if (lastFile.length < gap.length)
            freeSpaceMap.addFirst(FreeSpace(gap.startIndex + lastFile.length, gap.length - lastFile.length))
    }

    return checksumForRelocatedFiles + unmovedFiles.sumOf { it.checksum() }
}

private fun part2(input: CharArray): Long {
    val (originalFiles, freeSpaceMap) = parseInputToFilesAndFreeSpace(input)
    var checksumForRelocatedFiles = 0L
    var checksumForConsideredButUnchangedFiles = 0L

    while (originalFiles.isNotEmpty() && freeSpaceMap.isNotEmpty() && originalFiles.peek().startIndex > freeSpaceMap.first().startIndex) {
        val file = originalFiles.pop()
        val indexOfGap = findFirstGapForWholeFile(freeSpaceMap, file)
        if (indexOfGap >= 0) {
            val gap = freeSpaceMap[indexOfGap]
            checksumForRelocatedFiles += checksum(file.id, gap.startIndex, file.length)
            if (gap.length == file.length) freeSpaceMap.removeAt(indexOfGap)
            else freeSpaceMap[indexOfGap] = FreeSpace(gap.startIndex + file.length, gap.length - file.length)
        } else
            checksumForConsideredButUnchangedFiles += file.checksum()
    }

    return checksumForRelocatedFiles + checksumForConsideredButUnchangedFiles + originalFiles.sumOf { it.checksum() }
}


private data class File(val id: Int, val startIndex: Int, val length: Int) {
    fun checksum() = checksum(id, startIndex, length)
}

private data class FreeSpace(val startIndex: Int, val length: Int)


private fun checksum(id: Int, startIndex: Int, length: Int): Long = id.toLong() * (startIndex * length + (length * (length - 1) / 2))

private fun findFirstGapForWholeFile(freeSpaceMap: List<FreeSpace>, file: File): Int =
    freeSpaceMap.indexOfFirst { it.length >= file.length && it.startIndex < file.startIndex }

private fun parseInputToFilesAndFreeSpace(input: CharArray): Pair<Stack<File>, ArrayDeque<FreeSpace>> {
    val unmovedFiles = Stack<File>()
    val freeSpaceMap = ArrayDeque<FreeSpace>(input.size / 2 + 1)

    var startIndex = 0
    for (id in 0..input.size / 2) {
        val fileLength = input[id * 2].digitToInt()
        if (fileLength > 0) unmovedFiles.push(File(id, startIndex, fileLength))
        startIndex += fileLength
        if (id * 2 + 1 <= input.lastIndex) {
            val gapLength = input[id * 2 + 1].digitToInt()
            if (gapLength > 0) freeSpaceMap.addLast(FreeSpace(startIndex, gapLength))
            startIndex += gapLength
        }
    }
    return Pair(unmovedFiles, freeSpaceMap)
}
