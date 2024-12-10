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
    benchmark(100) { part2(puzzle) } // 32ms
    benchmark { part2ButFaster(puzzle) } // 527µs
}

internal object Day9 : Challenge {
    override fun assertCorrect() {
        check(60, "P1 Example 0") { part1("12345".toCharArray()) }
        check(1928, "P1 Example 1") { part1(example) }
        check(6310675819476L, "P1 Puzzle") { part1(puzzle) }

        check(2858, "P2 Example") { part2(example) }
        check(6335972980679L, "P2 Puzzle") { part2(puzzle) }
        check(2858, "P2 Example (Fast)") { part2ButFaster(example) }
        check(6335972980679L, "P2 Puzzle (Fast)") { part2ButFaster(puzzle) }
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
        val indexOfGap = freeSpaceMap.indexOfFirst { it.length >= file.length && it.startIndex < file.startIndex }
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

// The bulk of the time for part 2 above was taken by scanning through the free space queue from the start for each file.
// This version walks the free space queue and caches where it last reached
private fun part2ButFaster(input: CharArray): Long {
    val (originalFiles, freeSpaceQueue) = parseInputToFilesAndFreeSpace(input)
    val freeSpace = FreeSpaceMap(freeSpaceQueue)
    var checksumForRelocatedFiles = 0L
    var checksumForConsideredButUnchangedFiles = 0L
    while (originalFiles.isNotEmpty() && freeSpace.isNotEmpty() && originalFiles.peek().startIndex > freeSpace.earliestStartIndex()) {
        val file = originalFiles.pop()
        val gap = freeSpace.fillGapLeftOfFile(file)
        if (gap != null) checksumForRelocatedFiles += checksum(file.id, gap.startIndex, file.length)
        else checksumForConsideredButUnchangedFiles += file.checksum()
    }
    return checksumForRelocatedFiles + checksumForConsideredButUnchangedFiles + originalFiles.sumOf { it.checksum() }
}


private data class File(val id: Int, val startIndex: Int, val length: Int) {
    fun checksum() = checksum(id, startIndex, length)
}

private data class FreeSpace(val startIndex: Int, val length: Int)

private fun checksum(id: Int, startIndex: Int, length: Int): Long = id.toLong() * (startIndex * length + (length * (length - 1) / 2))

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


private class FreeSpaceMap(val map: ArrayDeque<FreeSpace>, val earliestByLength: IntArray) {
    constructor(map: ArrayDeque<FreeSpace>) : this(map, IntArray(10) { Int.MAX_VALUE }) {
        var i = -1
        while (++i < map.size && earliestByLength.drop(1).any { it == Int.MAX_VALUE }) {
            if (earliestByLength[map[i].length] == Int.MAX_VALUE) earliestByLength[map[i].length] = i
        }
    }

    private val tombstone = FreeSpace(Int.MAX_VALUE, 0)

    fun isNotEmpty() = earliestByLength.any { it < Int.MAX_VALUE }
    fun earliestStartIndex(): Int = earliestByLength.min()
    fun fillGapLeftOfFile(file: File): FreeSpace? {
        var earliestGap: FreeSpace? = null
        var length = file.length - 1
        while (++length <= 9) {
            if (earliestByLength[length] != Int.MAX_VALUE
                && map[earliestByLength[length]].startIndex < file.startIndex
                && map[earliestByLength[length]].startIndex < (earliestGap?.startIndex ?: Int.MAX_VALUE)
            ) earliestGap = map[earliestByLength[length]]
        }
        if (earliestGap == null) return null
        val index = earliestByLength[earliestGap.length]
        map[index] = tombstone

        // earliest of length now points at something invalid, so run forward to the next one of the same length
        earliestByLength[earliestGap.length] = Int.MAX_VALUE
        var j = index
        while (++j < map.size && earliestByLength[earliestGap.length] == Int.MAX_VALUE)
            if (map[j].length == earliestGap.length)
                earliestByLength[earliestGap.length] = j

        val remaining = earliestGap.length - file.length
        if (remaining > 0) {
            val newStartIndex = earliestGap.startIndex + file.length
            map[index] = FreeSpace(newStartIndex, remaining)
            // the new gap is smaller, which could be earlier than any existing gap of that length
            // if so, this is now the earliest
            if (earliestByLength[remaining] > index)
                earliestByLength[remaining] = index
        }

        return earliestGap
    }
}
