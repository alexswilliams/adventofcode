package ec2026.day8

import common.*

private val example = loadFilesToGrids("ec2026/day8", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToGrids("ec2026/day8", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzle[0]) } // 27.0µs
    benchmark { part2(puzzle[1]) } // 70.6µs
    benchmark(100) { part3(puzzle[2]) } // 15.6ms
}

internal object Day8 : Challenge {
    override fun assertCorrect() {
        check(32, "P1 Example") { part1(example[0]) }
        check(176, "P1 Puzzle") { part1(puzzle[0]) }

        check(187, "P2 Example") { part2(example[1]) }
        check(1040, "P2 Puzzle") { part2(puzzle[1]) }

        check(899_889, "P3 Example") { part3(example[2]) }
        check(899_227, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: Grid): Int =
    input.countLitTrees(midLeftToTopRight(input), bottomLeftToMidRight(input)) { it.plusCol().plusRow() }

private fun part2(input: Grid): Int =
    input.countLitTrees(midLeftToTopRight(input), bottomLeftToMidRight(input)) { it.plusCol().plusRow() } +
            input.countLitTrees(topLeftToMidRight(input), midLeftToBottomRight(input)) { it.minusCol().plusRow() } +
            input.countLitTrees(topRightToBottomRight(input), topLeftToBottomLeft(input)) { it.minusCol(2) } +
            input.countLitTrees(bottomLeftToMidRight(input), midLeftToTopRight(input)) { it.minusCol().minusRow() } +
            input.countLitTrees(midLeftToBottomRight(input), topLeftToMidRight(input)) { it.minusRow().plusCol() } +
            input.countLitTrees(topLeftToBottomLeft(input), topRightToBottomRight(input)) { it.plusCol(2) }

private fun Grid.countLitTrees(startLocations: Sequence<Location1616>, endLocations: Sequence<Location1616>, translate: (Location1616) -> Location1616): Int =
    startLocations.zip(endLocations).sumOf { (start, end) ->
        var loc = start
        var shadow = 0
        var lit = 0
        while (true) {
            val tree = this.at(loc).digitToInt()
            if (tree > shadow) lit++
            shadow = max(shadow - 1, tree - 1)
            if (loc == end) break;
            loc = translate(loc)
        }
        lit
    }


private fun part3(input: Grid): Int {
    val grid = input.toDigitGridOr0()

    val round1Bounds = midLeftToTopRight(input).zip(bottomLeftToMidRight(input)).toList()
    val round2Bounds = topLeftToMidRight(input).zip(midLeftToBottomRight(input)).toList()
    val round3Bounds = topRightToBottomRight(input).zip(topLeftToBottomLeft(input)).toList()
    val round4Bounds = bottomLeftToMidRight(input).zip(midLeftToTopRight(input)).toList()
    val round5Bounds = midLeftToBottomRight(input).zip(topLeftToMidRight(input)).toList()
    val round6Bounds = topLeftToBottomLeft(input).zip(topRightToBottomRight(input)).toList()

    val rounds = listOf(
        { grid.growTrees(round1Bounds) { it[0] = it[0].plusCol().plusRow() } },
        { grid.growTrees(round2Bounds) { it[0] = it[0].minusCol().plusRow() } },
        { grid.growTrees(round3Bounds) { it[0] = it[0].minusCol(2) } },
        { grid.growTrees(round4Bounds) { it[0] = it[0].minusCol().minusRow() } },
        { grid.growTrees(round5Bounds) { it[0] = it[0].minusRow().plusCol() } },
        { grid.growTrees(round6Bounds) { it[0] = it[0].plusCol(2) } },
    )

    var energyRequired = 0L
    var treesFinished = 0L
    rounds.cyclicSequence()
        .takeWhile { treesFinished < 100_000 }
        .forEach { round ->
            val result = round()
            energyRequired += (result and 0xffffffff)
            treesFinished += (result ushr 32)
        }
    return energyRequired.toInt()
}

private val locArr = IntArray(1)
private fun DigitGrid.growTrees(bounds: List<Pair<Location1616, Location1616>>, translate: (IntArray) -> Unit): Long {
    var lit = 0L
    var treesFinished = 0L
    bounds.forEach { (start, end) ->
        locArr[0] = start
        var shadow = 0
        while (true) {
            val tree = this.at(locArr[0])
            if (tree > shadow) {
                if (tree > 0) lit++
                if (tree == 9) {
                    treesFinished++
                    this.set(locArr[0], -2)
                } else this.set(locArr[0], tree + 1)
            } else if (tree < 1) this.set(locArr[0], tree + 1)
            shadow = max(shadow - 1, tree - 1)
            if (locArr[0] == end) break
            translate(locArr)
        }

    }
    return (treesFinished shl 32) or lit
}


private fun bottomLeftToMidRight(input: Grid) = generateSequence(input.lastIndex by16 (input.width / 4)) { prev ->
    when {
        prev.col() < 3 * input.width / 4 -> prev.plusCol(2)
        prev.row() > input.height / 2 -> prev.minusRow().plusCol()
        else -> null
    }
}

private fun topLeftToMidRight(input: Grid) = generateSequence(0 by16 (input.width / 4)) { prev ->
    when {
        prev.col() < 3 * input.width / 4 -> prev.plusCol(2)
        prev.row() < input.height / 2 -> prev.plusRow().plusCol()
        else -> null
    }
}

private fun midLeftToTopRight(input: Grid) = generateSequence((input.height / 2) by16 0) { prev ->
    when {
        prev.row() > 0 -> prev.minusRow().plusCol()
        prev.col() < 3 * input.width / 4 -> prev.plusCol(2)
        else -> null
    }
}

private fun midLeftToBottomRight(input: Grid) = generateSequence((input.height / 2) by16 0) { prev ->
    when {
        prev.row() < input.lastIndex -> prev.plusRow().plusCol()
        prev.col() < 3 * input.width / 4 -> prev.plusCol(2)
        else -> null
    }
}

private fun topRightToBottomRight(input: Grid) = generateSequence(0 by16 (3 * input.width / 4)) { prev ->
    when {
        prev.row() < input.height / 2 -> prev.plusRow().plusCol()
        prev.row() < input.lastIndex -> prev.plusRow().minusCol()
        else -> null
    }
}

private fun topLeftToBottomLeft(input: Grid) = generateSequence(0 by16 (input.width / 4)) { prev ->
    when {
        prev.row() < input.height / 2 -> prev.plusRow().minusCol()
        prev.row() < input.lastIndex -> prev.plusRow().plusCol()
        else -> null
    }
}

