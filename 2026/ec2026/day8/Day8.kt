package ec2026.day8

import common.*

private val example = loadFilesToGrids("ec2026/day8", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToGrids("ec2026/day8", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day8.assertCorrect()
    benchmark { part1(puzzle[0]) } // 27.0µs
    benchmark { part2(puzzle[1]) } // 70.6µs
    benchmark(100) { part3(puzzle[2]) } // 34.7ms
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

private fun part3(input: Grid): Int {
    val grid = input.toDigitGridOr0()
    var energyRequired = 0
    var treesFinished = 0

    val round1 = midLeftToTopRight(input).zip(bottomLeftToMidRight(input)).toList()
    val round2 = topLeftToMidRight(input).zip(midLeftToBottomRight(input)).toList()
    val round3 = topRightToBottomRight(input).zip(topLeftToBottomLeft(input)).toList()
    val round4 = bottomLeftToMidRight(input).zip(midLeftToTopRight(input)).toList()
    val round5 = midLeftToBottomRight(input).zip(topLeftToMidRight(input)).toList()
    val round6 = topLeftToBottomLeft(input).zip(topRightToBottomRight(input)).toList()

    while (true) {
        var result = grid.growTrees(round1) { it.plusCol().plusRow() }
        energyRequired += result.energyRequired
        treesFinished += result.treesFinished
        if (treesFinished >= 100_000) return energyRequired

        result = grid.growTrees(round2) { it.minusCol().plusRow() }
        energyRequired += result.energyRequired
        treesFinished += result.treesFinished
        if (treesFinished >= 100_000) return energyRequired

        result = grid.growTrees(round3) { it.minusCol(2) }
        energyRequired += result.energyRequired
        treesFinished += result.treesFinished
        if (treesFinished >= 100_000) return energyRequired

        result = grid.growTrees(round4) { it.minusCol().minusRow() }
        energyRequired += result.energyRequired
        treesFinished += result.treesFinished
        if (treesFinished >= 100_000) return energyRequired

        result = grid.growTrees(round5) { it.minusRow().plusCol() }
        energyRequired += result.energyRequired
        treesFinished += result.treesFinished
        if (treesFinished >= 100_000) return energyRequired

        result = grid.growTrees(round6) { it.plusCol(2) }
        energyRequired += result.energyRequired
        treesFinished += result.treesFinished
        if (treesFinished >= 100_000) return energyRequired
    }
}


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

private data class RoundResult(val treesFinished: Int, val energyRequired: Int)

private fun DigitGrid.growTrees(
    bounds: List<Pair<Location1616, Location1616>>, translate: (Location1616) -> Location1616
): RoundResult {
    return bounds.map { (start, end) ->
        var loc = start
        var shadow = 0
        var lit = 0
        var treesFinished = 0
        while (true) {
            val tree = this.at(loc)
            if (tree > shadow) {
                if (tree > 0) lit++
                if (tree == 9) {
                    treesFinished++
                    this.set(loc, -2)
                } else this.set(loc, tree + 1)
            } else if (tree < 1) this.set(loc, tree + 1)
            shadow = max(shadow - 1, tree - 1)
            if (loc == end) break
            loc = translate(loc)
        }
        RoundResult(treesFinished, lit)
    }.reduce { acc, result -> RoundResult(acc.treesFinished + result.treesFinished, acc.energyRequired + result.energyRequired) }
}


private fun bottomLeftToMidRight(input: Grid): Sequence<Location1616> =
    generateSequence(input.lastIndex by16 (input.width / 4)) { prev ->
        when {
            prev.col() < 3 * input.width / 4 -> prev.plusCol(2)
            prev.row() > input.height / 2 -> prev.minusRow().plusCol()
            else -> null
        }
    }

private fun topLeftToMidRight(input: Grid): Sequence<Location1616> =
    generateSequence(0 by16 (input.width / 4)) { prev ->
        when {
            prev.col() < 3 * input.width / 4 -> prev.plusCol(2)
            prev.row() < input.height / 2 -> prev.plusRow().plusCol()
            else -> null
        }
    }

private fun midLeftToTopRight(input: Grid) =
    generateSequence((input.height / 2) by16 0) { prev ->
        when {
            prev.row() > 0 -> prev.minusRow().plusCol()
            prev.col() < 3 * input.width / 4 -> prev.plusCol(2)
            else -> null
        }
    }

private fun midLeftToBottomRight(input: Grid) =
    generateSequence((input.height / 2) by16 0) { prev ->
        when {
            prev.row() < input.lastIndex -> prev.plusRow().plusCol()
            prev.col() < 3 * input.width / 4 -> prev.plusCol(2)
            else -> null
        }
    }

private fun topRightToBottomRight(input: Grid) =
    generateSequence(0 by16 (3 * input.width / 4)) { prev ->
        when {
            prev.row() < input.height / 2 -> prev.plusRow().plusCol()
            prev.row() < input.lastIndex -> prev.plusRow().minusCol()
            else -> null
        }
    }


private fun topLeftToBottomLeft(input: Grid) =
    generateSequence(0 by16 (input.width / 4)) { prev ->
        when {
            prev.row() < input.height / 2 -> prev.plusRow().minusCol()
            prev.row() < input.lastIndex -> prev.plusRow().plusCol()
            else -> null
        }
    }

