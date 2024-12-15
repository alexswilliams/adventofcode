package aoc2024.day15

import common.*

private val examples = loadFilesToLines("aoc2024/day15", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToLines("aoc2024/day15", "input.txt").single()

internal fun main() {
    Day15.assertCorrect()
    benchmark { part1(puzzle) } // 698µs
    benchmark { part2(puzzle) } // 274µs
}

internal object Day15 : Challenge {
    override fun assertCorrect() {
        check(2028, "P1 Example 1") { part1(examples[0]) }
        check(10092, "P1 Example 2") { part1(examples[1]) }
        check(1552879, "P1 Puzzle") { part1(puzzle) }

        check(618, "P2 Example 3") { part2(examples[2]) }
        check(9021, "P2 Example 2") { part2(examples[1]) }
        check(1561175, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val (grid, instructions) = input.partitionOnLineBreak({ it.asArrayOfCharArrays() }) { it.joinToString("") }
    var robot = grid.locationOf('@')
    for (cmd in instructions) {
        val move: (Location1616) -> Location1616 = when (cmd) {
            '^' -> Location1616::minusRow
            'v' -> Location1616::plusRow
            '<' -> Location1616::minusCol
            else -> Location1616::plusCol
        }
        var shifter = move(robot)
        while (grid.at(shifter) == 'O') shifter = move(shifter)
        robot = grid.shiftSmallBoxes(shifter, move(robot), robot)
    }
    return grid.allLocationOf('O').sumOf { it.row() * 100 + it.col() }
}

private fun Grid.shiftSmallBoxes(shifter: Location1616, newRobot: Location1616, oldRobot: Location1616): Location1616 =
    if (at(shifter) == '#') oldRobot
    else newRobot.also {
        moveRobot(newRobot, oldRobot)
        if (shifter != newRobot) this[shifter.row()][shifter.col()] = 'O'
    }


private fun part2(input: List<String>): Int {
    val (grid, instructions) = input.partitionOnLineBreak({ widen(it.asArrayOfCharArrays()) }) { it.joinToString("") }
    var robot = grid.locationOf('@')
    for (cmd in instructions)
        robot = grid.shiftBigBoxes(robot, cmd)
    return grid.allLocationOf('[').sumOf { it.row() * 100 + it.col() }
}


private fun Grid.shiftBigBoxes(oldRobot: Location1616, direction: Char): Location1616 {
    val newRobot = when (direction) {
        '^' -> oldRobot.minusRow()
        'v' -> oldRobot.plusRow()
        '<' -> oldRobot.minusCol()
        else -> oldRobot.plusCol()
    }
    if (at(newRobot) == '#') return oldRobot
    if (at(newRobot) == '.') return newRobot.also { moveRobot(newRobot, oldRobot) }
    return when (direction) {
        '<' -> shiftRobotIfPossible(newRobot, oldRobot, ::canPushLeft, ::pushLeft)
        '>' -> shiftRobotIfPossible(newRobot, oldRobot, ::canPushRight, ::pushRight)
        '^' -> shiftRobotIfPossible(newRobot, oldRobot, ::canPushVertically, ::pushVertically, step = -1)
        else -> shiftRobotIfPossible(newRobot, oldRobot, ::canPushVertically, ::pushVertically, step = 1)
    }
}

private fun Grid.shiftRobotIfPossible(newRobot: Int, oldRobot: Location1616, canPush: (Location1616, Int) -> Boolean, push: (Location1616, Int) -> Unit, step: Int = 0): Int {
    val boxLeft = if (at(newRobot) == '[') newRobot else newRobot.minusCol()
    if (canPush(boxLeft, step)) {
        push(boxLeft, step)
        moveRobot(newRobot, oldRobot)
        return newRobot
    } else return oldRobot
}

private tailrec fun Grid.canPushLeft(boxLeft: Location1616, step: Int = -1): Boolean {
    if (at(boxLeft.minusCol()) == '.') return true
    if (at(boxLeft.minusCol()) == '#') return false
    return canPushLeft(boxLeft.minusCol(2))
}

private tailrec fun Grid.canPushRight(boxLeft: Location1616, step: Int = 1): Boolean {
    if (at(boxLeft.plusCol(2)) == '.') return true
    if (at(boxLeft.plusCol(2)) == '#') return false
    return canPushRight(boxLeft.plusCol(2))
}

private fun Grid.pushLeft(boxLeft: Location1616, step: Int = -1) {
    if (at(boxLeft.minusCol()) != '.')
        pushLeft(boxLeft.minusCol(2))
    val row = this[boxLeft.row()]
    row[boxLeft.col() - 1] = '['
    row[boxLeft.col()] = ']'
    row[boxLeft.col() + 1] = '.'
}

private fun Grid.pushRight(boxLeft: Location1616, step: Int = 1) {
    if (at(boxLeft.plusCol(2)) != '.')
        pushRight(boxLeft.plusCol(2))
    val row = this[boxLeft.row()]
    row[boxLeft.col() + 2] = ']'
    row[boxLeft.col() + 1] = '['
    row[boxLeft.col()] = '.'
}

private fun Grid.canPushVertically(boxLeft: Location1616, step: Int = 1): Boolean {
    val left = at(boxLeft.plusRow(step))
    val right = at(boxLeft.plusRow(step).plusCol())
    if (left == '.' && right == '.') return true
    if (left == '#' || right == '#') return false
    if (left == '[') return canPushVertically(boxLeft.plusRow(step), step)
    return (left == '.' || canPushVertically(boxLeft.plusRow(step).minusCol(), step)) &&
            (right == '.' || canPushVertically(boxLeft.plusRow(step).plusCol(), step))
}

private fun Grid.pushVertically(boxLeft: Location1616, step: Int = 1) {
    val belowLeft = at(boxLeft.plusRow(step))
    val belowRight = at(boxLeft.plusRow(step).plusCol())
    if (belowLeft != '.' || belowRight != '.') {
        if (belowLeft == '[') pushVertically(boxLeft.plusRow(step), step)
        else {
            if (belowLeft != '.') pushVertically(boxLeft.plusRow(step).minusCol(), step)
            if (belowRight != '.') pushVertically(boxLeft.plusRow(step).plusCol(), step)
        }
    }
    this[boxLeft.row() + step][boxLeft.col()] = '['
    this[boxLeft.row() + step][boxLeft.col() + 1] = ']'
    this[boxLeft.row()][boxLeft.col()] = '.'
    this[boxLeft.row()][boxLeft.col() + 1] = '.'
}

private fun Grid.moveRobot(newRobot: Int, oldRobot: Location1616) {
    this[newRobot.row()][newRobot.col()] = '@'
    this[oldRobot.row()][oldRobot.col()] = '.'
}

private fun widen(original: Grid): Grid = Array(original.height) { row ->
    CharArray(original.width * 2) { col ->
        when (val before = original[row][col / 2]) {
            '@' -> if (col and 1 == 0) '@' else '.'
            'O' -> if (col and 1 == 0) '[' else ']'
            else -> before
        }
    }
}
