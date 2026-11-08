package ec2026.day5

import common.*

private val example = loadFilesToGrids("ec2026/day5", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToGrids("ec2026/day5", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzle[0]) } // 339.0µs
    benchmark { part2(puzzle[1]) } // 1.2ms
    benchmark { part3(puzzle[2]) } // 2.3ms
}

internal object Day5 : Challenge {
    override fun assertCorrect() {
        check(137, "P1 Example") { part1(example[0]) }
        check(1149, "P1 Puzzle") { part1(puzzle[0]) }

        check(69, "P2 Example") { part2(example[1]) }
        check(1285, "P2 Puzzle") { part2(puzzle[1]) }

        check(14, "P3 Example") { part3(example[2]) }
        check(9342, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: Grid): Int {
    input.set((input.height / 2) by16 (input.width / 2), '#')
    var landSize = 1
    while (true) {
        expandLand(input)
        val newLandSize = input.allLocationOf('#').size
        if (newLandSize == landSize)
            return newLandSize
        landSize = newLandSize
    }
}

private fun expandLand(input: Grid) {
    val existingLand = input.allLocationOf('#')
    val potentialNewLand = existingLand.flatMapTo(HashSet()) { loc ->
        neighbours(loc, input.height, input.width)
            .filter { input.at(it) != '#' }
    }
    val newLand = potentialNewLand.filter { loc ->
        neighbours(loc, input.height, input.width)
            .filter { input.at(it) == '#' || input.at(it) == '1' }
            .size >= 5
    }
    newLand.forEach { input.set(it, '#') }
}

fun neighbours(loc: Location1616, height: Int, width: Int): List<Location1616> = buildList {
    if (loc.row() > 0) add(loc.minusSingleRow())
    if (loc.col() > 0) add(loc.minusSingleCol())
    if (loc.row() > 0 && loc.col() > 0) add(loc.minusSingleCol().minusSingleRow())
    if (loc.row() > 0 && loc.col() < width - 1) add(loc.plusSingleCol().minusSingleRow())
    if (loc.col() < width - 1) add(loc.plusSingleCol())
    if (loc.row() < height - 1) add(loc.plusSingleRow())
    if (loc.col() < width - 1 && loc.row() < height - 1) add(loc.plusSingleRow().plusSingleCol())
    if (loc.col() > 0 && loc.row() < height - 1) add(loc.minusSingleCol().plusSingleRow())
}


private fun part2(input: Grid): Int {
    part1(input)
    var sandCount = addSand(input)
    while (true) {
        val newSandCount = expandSand(input)
        if (newSandCount == sandCount)
            return newSandCount
        sandCount = newSandCount
    }
}

private fun addSand(input: Grid): Int {
    val sand = input.allLocationOf('#').filter { loc ->
        loc.row() == 0 ||
                loc.row() == input.height - 1 ||
                loc.col() == 0 ||
                loc.col() == input.width - 1 ||
                neighbours(loc, input.height, input.width).any { input.at(it) != '#' }
    }
    sand.forEach { input.set(it, '=') }
    return input.allLocationOf('=').size
}

private fun expandSand(input: Grid): Int {
    val sand = input.allLocationOf('#').filter { loc ->
        neighbours(loc, input.height, input.width)
            .filter { input.at(it) == '=' }
            .size >= 5
    }
    sand.forEach { input.set(it, '=') }
    return input.allLocationOf('=').size
}


private fun part3(input: Grid): Int {
    part2(input)
    val primaryTreeSpots = input.allLocationOf('#')
        .filterNot { it.row() < 2 || it.row() > input.height - 3 || it.col() < 2 || it.col() > input.width - 3 }

    val primaryTrees = primaryTreeSpots.filter { loc ->
        val subGrid = input.subGrid(loc.row() - 2, loc.col() - 2, 5, 5)
        subGrid.allLocations().subtract(setOf(0 by16 0, 0 by16 4, 4 by16 0, 4 by16 4))
            .all { subGrid.at(it) == '#' }
    }
    primaryTrees.forEach { input.set(it, '@') }

    repeat(2) {
        val newTrees = input.allLocationOf('#').filter { loc ->
            val n = neighbours(loc, input.height, input.width)
            n.none { input.at(it) == '=' } && n.filter { input.at(it) == '@' }.size >= 4
        }
        newTrees.forEach { input.set(it, '@') }
    }

    return input.allLocationOf('@').size
}
