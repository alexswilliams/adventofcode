package ec2026.day5

import common.*

private val example = loadFilesToGrids("ec2026/day5", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToGrids("ec2026/day5", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzle[0]) } // 280.1µs
    benchmark { part2(puzzle[1]) } // 654.6µs
    benchmark { part3(puzzle[2]) } // 1.3ms
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
        val newLandSize = expandLand(input)
        if (newLandSize == landSize) return newLandSize
        landSize = newLandSize
    }
}

private fun expandLand(input: Grid): Int {
    val existingLand = input.allLocationOf('#')
    val potentialNewLand = existingLand.flatMapTo(HashSet()) { loc ->
        neighboursOf8(loc, input)
            .filter { input.at(it) != '#' }
    }
    val newLand = potentialNewLand.filter { loc ->
        neighboursOf8(loc, input)
            .filter { input.at(it) == '#' || input.at(it) == '1' }
            .size >= 5
    }
    newLand.forEach { input.set(it, '#') }
    return input.countLocationsOf('#')
}


private fun part2(input: Grid): Int {
    part1(input)
    var sandCount = addSand(input)
    while (true) {
        val newSandCount = expandSand(input)
        if (newSandCount == sandCount) return newSandCount
        sandCount = newSandCount
    }
}

private fun addSand(input: Grid): Int {
    val sand = input.allLocationOf('#').filter { loc ->
        loc.row() == 0 ||
                loc.row() == input.height - 1 ||
                loc.col() == 0 ||
                loc.col() == input.width - 1 ||
                neighboursOf8(loc, input).any { input.at(it) != '#' }
    }
    sand.forEach { input.set(it, '=') }
    return input.countLocationsOf('=')
}

private fun expandSand(input: Grid): Int {
    val sand = input.allLocationOf('#').filter { loc ->
        neighboursOf8(loc, input)
            .count { input.at(it) == '=' } >= 5
    }
    sand.forEach { input.set(it, '=') }
    return input.countLocationsOf('=')
}


private fun part3(input: Grid): Int {
    part2(input)
    addTrees(input)
    repeat(2) { expandTrees(input) }
    return input.countLocationsOf('@')
}

private val TREE_TEST_LOCATIONS =
    cartesianProductOf(0..4, 0..4).map { (r, c) -> r by16 c }
        .subtract(setOf(0 by16 0, 0 by16 4, 4 by16 0, 4 by16 4))
        .map { it.minusRow(2).minusCol(2) }

private fun addTrees(input: Grid) {
    val treeSpots = input.allLocationOf('#')
        .filterNot { it.row() < 2 || it.row() > input.height - 3 || it.col() < 2 || it.col() > input.width - 3 }
    treeSpots.filter { loc ->
        TREE_TEST_LOCATIONS.all { input.at(it.plus(loc)) == '#' }
    }.forEach { input.set(it, '@') }
}

private fun expandTrees(input: Grid) {
    input.allLocationOf('#').filter { loc ->
        val n = neighboursOf8(loc, input)
        n.none { input.at(it) == '=' } && n.count { input.at(it) == '@' } >= 4
    }.forEach { input.set(it, '@') }
}


fun neighboursOf8(loc: Location1616, grid: Grid): List<Location1616> = buildList(8) {
    if (loc.row() > 0) add(loc.minusSingleRow())
    if (loc.col() > 0) add(loc.minusSingleCol())
    if (loc.row() > 0 && loc.col() > 0) add(loc.minusSingleCol().minusSingleRow())
    if (loc.row() > 0 && loc.col() < grid.width - 1) add(loc.plusSingleCol().minusSingleRow())
    if (loc.col() < grid.width - 1) add(loc.plusSingleCol())
    if (loc.row() < grid.height - 1) add(loc.plusSingleRow())
    if (loc.col() < grid.width - 1 && loc.row() < grid.height - 1) add(loc.plusSingleRow().plusSingleCol())
    if (loc.col() > 0 && loc.row() < grid.height - 1) add(loc.minusSingleCol().plusSingleRow())
}
