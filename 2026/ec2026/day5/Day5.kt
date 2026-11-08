package ec2026.day5

import common.*

private val example = loadFilesToGrids("ec2026/day5", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToGrids("ec2026/day5", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day5.assertCorrect()
    benchmark { part1(puzzle[0]) } // 989ns
    benchmark { part2(puzzle[1]) } // 1.1µs
    benchmark { part3(puzzle[2]) } // 2.3µs
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


private fun part1(input: Grid, landLocations: MutableSet<Location1616> = HashSet()): Int {
    input.set((input.height / 2) by16 (input.width / 2), '#')
    landLocations.add((input.height / 2) by16 (input.width / 2))
    var landSize = 1
    while (true) {
        val newLandSize = expandLand(input, landLocations)
        if (newLandSize == landSize) return newLandSize
        landSize = newLandSize
    }
}

private fun expandLand(input: Grid, land: MutableSet<Location1616>): Int {
    val neighbourArray = IntArray(8)
    val potentialNewLand = land.flatMapTo(HashSet()) { loc ->
        neighboursOf8(loc, input, neighbourArray)
            .filter { it != -1 && input.at(it) != '#' }
    }
    val newLand = potentialNewLand.filter { loc ->
        neighboursOf8(loc, input, neighbourArray)
            .count { it != -1 && (input.at(it) == '#' || input.at(it) == '1') } >= 5
    }
    newLand.forEach { input.set(it, '#') }
    land.addAll(newLand)
    return land.size
}


private fun part2(input: Grid, land: MutableSet<Location1616> = HashSet()): Int {
    part1(input, land)
    var sandCount = addSand(input, land)
    while (true) {
        val newSandCount = expandSand(input, land)
        if (newSandCount == 0) return sandCount
        sandCount += newSandCount
    }
}

private fun addSand(input: Grid, land: MutableSet<Location1616>): Int {
    val neighbourArray = IntArray(8)
    val newSand = land.filter { loc ->
        loc.row() == 0 ||
                loc.row() == input.height - 1 ||
                loc.col() == 0 ||
                loc.col() == input.width - 1 ||
                neighboursOf8(loc, input, neighbourArray).any { it != -1 && input.at(it) != '#' }
    }
    newSand.forEach { input.set(it, '=') }
    land.removeAll(newSand)
    return newSand.size
}

private fun expandSand(input: Grid, land: MutableSet<Location1616>): Int {
    val neighbourArray = IntArray(8)
    val newSand = land.filter { loc ->
        neighboursOf8(loc, input, neighbourArray)
            .count { it != -1 && input.at(it) == '=' } >= 5
    }
    newSand.forEach { input.set(it, '=') }
    land.removeAll(newSand)
    return newSand.size
}


private fun part3(input: Grid): Int {
    val land = HashSet<Location1616>()
    part2(input, land)
    var treeCount: Int = addTrees(input, land)
    repeat(2) { treeCount += expandTrees(input, land) }
    return treeCount
}

private val TREE_TEST_LOCATIONS =
    cartesianProductOf(0..4, 0..4).map { (r, c) -> r by16 c }
        .subtract(setOf(0 by16 0, 0 by16 4, 4 by16 0, 4 by16 4))
        .map { it.minusRow(2).minusCol(2) }

private fun addTrees(input: Grid, land: MutableSet<Location1616>): Int {
    val newTrees = land
        .filter { loc ->
            loc.row() >= 2 && loc.row() <= input.height - 3 && loc.col() >= 2 && loc.col() <= input.width - 3 &&
            TREE_TEST_LOCATIONS.all { input.at(it + loc) == '#' }
        }
    newTrees.forEach { input.set(it, '@') }
    land.removeAll(newTrees)
    return newTrees.size
}

private fun expandTrees(input: Grid, land: MutableSet<Location1616>): Int {
    val neighbourArray = IntArray(8)
    val newTrees = land.filter { loc ->
        val n = neighboursOf8(loc, input, neighbourArray)
        n.none { it != -1 && input.at(it) == '=' } && n.count { it != -1 && input.at(it) == '@' } >= 4
    }
    newTrees.forEach { input.set(it, '@') }
    land.removeAll(newTrees)
    return newTrees.size
}


fun neighboursOf8(loc: Location1616, grid: Grid, results: IntArray = IntArray(8)): IntArray {
    results[0] = if (loc.row() > 0) loc.minusSingleRow() else -1
    results[1] = if (loc.col() > 0) loc.minusSingleCol() else -1
    results[2] = if (loc.row() > 0 && loc.col() > 0) loc.minusSingleCol().minusSingleRow() else -1
    results[3] = if (loc.row() > 0 && loc.col() < grid.width - 1) loc.plusSingleCol().minusSingleRow() else -1
    results[4] = if (loc.col() < grid.width - 1) loc.plusSingleCol() else -1
    results[5] = if (loc.row() < grid.height - 1) loc.plusSingleRow() else -1
    results[6] = if (loc.col() < grid.width - 1 && loc.row() < grid.height - 1) loc.plusSingleRow().plusSingleCol() else -1
    results[7] = if (loc.col() > 0 && loc.row() < grid.height - 1) loc.minusSingleCol().plusSingleRow() else -1
    return results
}
