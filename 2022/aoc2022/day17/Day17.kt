package aoc2022.day17

import common.*
import kotlin.experimental.*
import kotlin.math.*
import kotlin.test.*

private val exampleInput = "aoc2022/day17/example.txt".fromClasspathFile()
private val puzzleInput = "aoc2022/day17/input.txt".fromClasspathFile()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 3068
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 1514285714288L

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 3133, took 680µs

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 1547953216393, took 2.2ms
}


private fun part1(input: String) = playTetris(input, 2022).first - 1 // (1 for the ground row)

private fun part2(input: String): Long {
    // The number of rows the form a cycle is not directly linked to the number of blocks it took - it varies with the input pattern (so the
    // relationship will be different between the example input and the puzzle input.)
    // But, you don't need to eliminate every cycle in order to make the problem tractable - just... most of them.  Which relaxes the constraints on
    // finding the cycle considerably - you don't need to know the full cycle, just:
    //  - how long it is, and
    //  - a position somewhere inside the first repetition
    //
    // So e.g. in a tower where the contents of each row happens to conveniently be the english alphabet:
    //  0 1 2 3  4 5 6 7 8 9 10 1 2 3 4 5 6 7  8 9 201 2 3 4  5 6 7 8 9 301  2 3 4 ...
    //  W X Y Z  a b c d e f g  a b c d e f g  a b c d e f g  a b c d e f g  a b c ... c d e f g  a b  Q R S T U V
    //  =======                 =============                 =============        ...==========       ===========
    //    |      =============       |         =============        |        ======...     |      ===       |
    //  preamble    rep 1          rep 2           rep 3          rep 4      rep 5 ...   rep n    tail   partials
    // There is a pre-amble (which accounts for the base of the tower not forming part of the sequence), followed by a number of repetitions of each
    // row, then a tail (which is a partial repetition), followed by "partials", which are rows which are still "under construction".
    //
    // The algorithm below does the following:
    //  1 Play tetris for an arbitrary but tractable number of blocks - enough to form a few cycles in the output
    //  2 Guess how long the length of "partials" is - so that we can exclude this from the search
    //  3 Look from the end of the tail backwards by a small amount (e.g. 5 rows) - this is the pattern that anchors the cycle
    //    - in this case, the run length is 5 rows, giving the pattern "e f g  a b"
    //  4 Walk backwards from before the pattern, looking for positions where the pattern repeats - record all these positions in a set.
    //  5 Count the gaps between the ordered members of the positions set - if there is a unique stride length, then we've found the cycle
    //    - If there are multiple stride lengths, it's possible that there are red herrings that matched, meaning that the pattern was too short - so
    //      repeat the search with a longer run length until there is a single common stride distance between each member of the set.
    //  6 Find the first two times the pattern appears (the smallest two elements of the set), in this example would be indexes 8 and 15
    //  7 Play tetris again, but request that the game reports on how many blocks (rocks) need to drop before each of the first two "checkpoint" rows
    //    are reached.
    //  8 From this you now have:
    //    - a maximum estimate for the size of the rows pre-amble (the first member of the position set)
    //    - row cycle size: (difference between first and second member of position set)
    //    - a maximum estimate for the size of the blocks pre-amble (the first reported checkpoint block)
    //    - block cycle size: (difference between block counts, as reported by the previous game of tetris)
    //    You can use these to calculate how many repeats would take place within 10e12 dropped rocks (`repeatCount * blockStride` below)
    //  9 Play the game a final time, requesting only the number of blocks that make up the preamble and the span between the last cycle and 10e12
    // 10 Add on the size of the omitted repeated section and return.

    val (highestRow, tower) = playTetris(input, 4000) // An arbitrarily large tower - number chosen with trial and error
    val endOfMatchRange = highestRow - 50 // trim the top (by a finger-in-air amount), so that partially complete rows are ignored
    var runLength = 5 // also chosen arbitrarily, but wants to be small, so that `byteArraysMatch` can afford to be naïve
    val matches = mutableListOf<Int>()

    // Run down from the top of the tower looking for sub-arrays that match the highest 5 rows; record their (top-most) positions in `matches`
    // This runs down from the top (as opposed to starting at the bottom) as there may be a preamble at the bottom before a cycle is set up.
    var searchFrom = endOfMatchRange - 2 * runLength
    while (searchFrom > 0) {
        if (byteArraysMatch(tower, endOfMatchRange - runLength, searchFrom, runLength))
            matches.add(searchFrom + runLength)
        searchFrom--
    }
    // `matches` now contains an anchor into each repeated pattern, but might also contain noise (e.g. if another part of the tower coincidentally
    // contained the 5 rows that were being scanned for,) so increase the size of the pattern until the distance between each match becomes constant.
    while (matches.zipWithNext().map { it.first - it.second }.distinct().size != 1) {
        runLength++ // +=1 avoids accidentally striding over the actual repeated length, as opposed to e.g. *=2 which might go from many gaps to none.
        matches.removeAll { byteArraysMatch(tower, endOfMatchRange - runLength, it - runLength, runLength) }
    }

    // The `playTetris` method accepts a set of rows to "checkpoint" - it will return a corresponding set of turns at which a block met or
    // passed that row.  This sets the higher row checkpoint to be the number of dropped blocks - this is definitely the upper bound on the block
    // count, but probably only a small overestimate on what's actually required (e.g. /1.5 drops enough blocks, but /2 does not.)
    val rowsToCheckpoint = matches.sorted().take(2)
    val (_, _, blocksToReachCheckpoints) = playTetris(input, rowsToCheckpoint[1], rowsToCheckpoint.toSet())
    if (blocksToReachCheckpoints!!.size != 2) throw Exception("Not enough blocks were dropped to reach both row checkpoints")

    // One more round - this time with the correct number of blocks omitted such that the repeated section in the middle never happens.  This gives
    // the combined size of the head and tail of the tower, which can then be added to the (calculated) size of the repeated section.
    val (blocksToReachRepeat, blocksToReachSecondRepeat) = blocksToReachCheckpoints.sorted()
    val blockStride = blocksToReachSecondRepeat - blocksToReachRepeat // for the example, this is 35; for my input this is 1710
    val repeatCount = (1_000_000_000_000L - blocksToReachRepeat) / blockStride
    val blocksBetweenEndOfRepeatAndTarget = (1_000_000_000_000L - repeatCount * blockStride - blocksToReachRepeat).toInt()
    val (allRowsWithoutRepeat) = playTetris(input, blocksToReachRepeat + blocksBetweenEndOfRepeatAndTarget)

    // Now re-inflate the number of rows generated in the final game with the missing repeated section.
    val rowStride = rowsToCheckpoint[1] - rowsToCheckpoint[0] // for the example this is 53; for my input this is 2647
    return (rowStride * repeatCount) + allRowsWithoutRepeat
}

// Something far more clever could have happened here - e.g. rolling hashing could have reduced this from O(n*m) to O(n), but the calling method above
// arranges for m (runLength) to be initially small enough that this is ~O(n) across the entire search.
private fun byteArraysMatch(tower: ByteArray, startOfPatternToMatch: Int, searchFrom: Int, runLength: Int): Boolean {
    if (startOfPatternToMatch < 0 || searchFrom < 0) return false
    var i = -1
    while (++i < runLength) if (tower[startOfPatternToMatch + i] != tower[searchFrom + i]) return false
    return true
}


private val shapes = arrayOf(
    byteArrayOf(0b0011110),
    byteArrayOf(0b0001000, 0b0011100, 0b0001000),
    byteArrayOf(0b0000100, 0b0000100, 0b0011100),
    byteArrayOf(0b0010000, 0b0010000, 0b0010000, 0b0010000),
    byteArrayOf(0b0011000, 0b0011000)
)

private fun playTetris(input: String, stopAfterBlocks: Int, checkpoints: Set<Int> = emptySet()): Triple<Int, ByteArray, Set<Int>?> {
    val jets = input.cyclicIterator()
    val tower = ByteArray(4 * stopAfterBlocks + 4)

    var turn = 0
    var rowAboveTopOfHighestShape = 0
    var towerRowOfLowestRowInShape = 3
    var horizontalOffset = 0
    val uncheckedCheckpoints = if (checkpoints.isEmpty()) null else checkpoints.toMutableSet() // this nullability is to avoid heap allocations
    val blocksForCheckpoints = if (checkpoints.isEmpty()) null else mutableSetOf<Int>()
    while (turn <= stopAfterBlocks) {
        val shape = shapes[turn % 5]
        val shiftDirection = jets.next()
        if (tryShift(shape, tower, horizontalOffset, towerRowOfLowestRowInShape, shiftDirection))
            horizontalOffset += (if (shiftDirection == '<') -1 else 1)
        if (tryDescend(shape, tower, horizontalOffset, towerRowOfLowestRowInShape)) {
            towerRowOfLowestRowInShape--
        } else {
            turn++
            addShapeToTower(shape, tower, horizontalOffset, towerRowOfLowestRowInShape)
            val newRowAboveTopOfHighestShape = max(shape.size + towerRowOfLowestRowInShape, rowAboveTopOfHighestShape)
            if (!uncheckedCheckpoints.isNullOrEmpty()) {
                if (uncheckedCheckpoints.any { it in (rowAboveTopOfHighestShape + 1)..newRowAboveTopOfHighestShape }) {
                    blocksForCheckpoints?.add(turn)
                    uncheckedCheckpoints.removeAll { it in (rowAboveTopOfHighestShape + 1)..newRowAboveTopOfHighestShape }
                }
            }
            rowAboveTopOfHighestShape = newRowAboveTopOfHighestShape
            towerRowOfLowestRowInShape = rowAboveTopOfHighestShape + 3
            horizontalOffset = 0
        }
    }
    return Triple(rowAboveTopOfHighestShape - 1, tower, blocksForCheckpoints)
}

private fun tryShift(
    shape: ByteArray,
    tower: ByteArray,
    horizontalOffset: Int,
    verticalOffsetOfLowestRowInShape: Int,
    shiftDirection: Char
): Boolean {
    val newHozOffset = horizontalOffset + (if (shiftDirection == '<') -1 else +1)
    var shapeRow = shape.lastIndex
    var towerRow = verticalOffsetOfLowestRowInShape
    while (shapeRow >= 0) {
        val shiftedRow = shapeRowWithOffset(shape[shapeRow], newHozOffset)
        // Representing the shapes and the tower as arrays of byte means that "does it intersect" can be done with bitwise AND, and "did it fall off
        // the edge" can be done by comparing the number of set bits in the byte before and after.
        if (((shiftedRow and tower[towerRow]) != 0.toByte()) || (shiftedRow.countOneBits() != shape[shapeRow].countOneBits()))
            return false
        shapeRow--;towerRow++
    }
    return true
}

private fun tryDescend(shape: ByteArray, tower: ByteArray, horizontalOffset: Int, verticalOffsetOfLowestRowInShape: Int): Boolean {
    if (verticalOffsetOfLowestRowInShape == 0) return false
    var shapeRow = shape.lastIndex
    var towerRow = verticalOffsetOfLowestRowInShape - 1
    while (shapeRow >= 0) {
        if ((shapeRowWithOffset(shape[shapeRow], horizontalOffset) and tower[towerRow]) != 0.toByte())
            return false
        shapeRow--;towerRow++
    }
    return true
}

private fun addShapeToTower(shape: ByteArray, tower: ByteArray, horizontalOffset: Int, verticalOffsetOfLowestRowInShape: Int) {
    var shapeRow = shape.lastIndex
    var towerRow = verticalOffsetOfLowestRowInShape
    while (shapeRow >= 0) {
        tower[towerRow] = tower[towerRow] or shapeRowWithOffset(shape[shapeRow], horizontalOffset)
        shapeRow--;towerRow++
    }
}

private fun shapeRowWithOffset(shapeByte: Byte, horizontalOffset: Int) =
    if (horizontalOffset > 0) (shapeByte.toInt() shr horizontalOffset).toByte()
    else if (horizontalOffset < 0) ((shapeByte.toInt() shl -horizontalOffset) and 0x7f).toByte() // 0x7f ensures that only 7 columns are kept
    else shapeByte
