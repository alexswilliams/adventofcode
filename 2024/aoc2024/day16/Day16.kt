package aoc2024.day16

import common.*
import kotlinx.collections.immutable.*
import kotlin.math.*

private val examples = loadFilesToGrids("aoc2024/day16", "example1.txt", "example2.txt")
private val puzzle = loadFilesToGrids("aoc2024/day16", "input.txt").single()

internal fun main() {
    Day16.assertCorrect()
    benchmark { part1(puzzle) } // 1.2ms
    benchmark(100) { part2(puzzle) } // 9.2ms
}

internal object Day16 : Challenge {
    override fun assertCorrect() {
        check(7036, "P1 Example") { part1(examples[0]) }
        check(11048, "P1 Example") { part1(examples[1]) }
        check(75416, "P1 Puzzle") { part1(puzzle) }

        check(45, "P2 Example") { part2(examples[0]) }
        check(64, "P2 Example") { part2(examples[1]) }
        check(476, "P2 Puzzle") { part2(puzzle) }
    }
}

private fun part1(grid: Grid): Int = aStarLowestScore(grid)
private fun part2(grid: Grid): Int = aStarAllShortestPathsTileSetSize(grid)


private fun aStarLowestScore(grid: Grid): Int {
    fillDeadEnds(grid)
    val start = grid.locationOf('S')
    val end = grid.locationOf('E')

    data class Work(val pos: Location1616, val direction: Facing)

    val visited = Array(Facing.entries.size) { Array(grid.height) { IntArray(grid.width) { Int.MAX_VALUE } } }
        .apply { this[Facing.EAST.ordinal][start.row()][start.col()] = 0 }
    val work = TreeQueue<Work> { it.pos.manhattanTo(end) }
        .apply { offer(Work(start, Facing.EAST), 0) }

    while (true) {
        val u = work.poll() ?: error("S to E not connected by maze")
        val scoreAtU = visited[u.direction.ordinal].at(u.pos)
        if (u.pos.advance(u.direction) == end)
            return scoreAtU + 1

        fun maybeAddState(nextDirection: Facing, nextPosition: Location1616, newScore: Int) {
            if (grid.at(u.pos.advance(nextDirection)) != '#' && newScore < visited[nextDirection.ordinal].at(nextPosition)) {
                visited[nextDirection.ordinal][nextPosition.row()][nextPosition.col()] = newScore
                // set lookups needed to reposition on this problem are slower than handling the occasional duplicates, so just offer
                work.offer(Work(nextPosition, nextDirection), newScore)
            }
        }
        maybeAddState(u.direction, u.pos.advance(u.direction), scoreAtU + 1)
        maybeAddState(u.direction.turnLeft(), u.pos, scoreAtU + 1000)
        maybeAddState(u.direction.turnRight(), u.pos, scoreAtU + 1000)
    }
}

private fun aStarAllShortestPathsTileSetSize(grid: Grid): Int {
    fillDeadEnds(grid)
    val start = grid.locationOf('S')
    val end = grid.locationOf('E')

    data class Work(val pos: Location1616, val direction: Facing, val path: PersistentList<Location1616>, val score: Int)

    val visited = Array(Facing.entries.size) { Array(grid.height) { IntArray(grid.width) { Int.MAX_VALUE } } }
        .apply { this[Facing.EAST.ordinal][start.row()][start.col()] = 0 }
    val work = TreeQueue<Work> { it.pos.manhattanTo(end) }
        .apply { offer(Work(start, Facing.EAST, persistentListOf(start), 0), weight = 0) }

    var bestScore = Int.MAX_VALUE
    val shortestPathTiles = mutableSetOf<Location1616>()

    while (true) {
        val u = work.poll() ?: return shortestPathTiles.size
        if (u.pos.advance(u.direction) == end) {
            if (bestScore == Int.MAX_VALUE) bestScore = u.score + 1
            if (bestScore == u.score + 1) shortestPathTiles.addAll(u.path.plus(u.pos.advance(u.direction)))
            else return shortestPathTiles.size
        }

        fun maybeAddState(nextDir: Facing, nextPos: Location1616, newScore: Int) {
            if (grid.at(u.pos.advance(nextDir)) != '#' && newScore <= visited[nextDir.ordinal].at(nextPos)) {
                visited[nextDir.ordinal].set(nextPos, newScore)
                // set lookups needed to reposition on this problem are slower than handling the occasional duplicates, so just offer
                work.offer(Work(nextPos, nextDir, if (nextPos == u.pos) u.path else u.path.plus(nextPos), newScore), newScore)
            }
        }
        if (u.score + 1 > bestScore) continue
        maybeAddState(u.direction, u.pos.advance(u.direction), u.score + 1)
        if (u.score + 1000 > bestScore) continue
        maybeAddState(u.direction.turnLeft(), u.pos, u.score + 1000)
        maybeAddState(u.direction.turnRight(), u.pos, u.score + 1000)
    }
}

private enum class Facing {
    NORTH, SOUTH, EAST, WEST;

    fun turnRight() = when (this) {
        NORTH -> EAST
        EAST -> SOUTH
        SOUTH -> WEST
        WEST -> NORTH
    }

    fun turnLeft() = when (this) {
        NORTH -> WEST
        WEST -> SOUTH
        SOUTH -> EAST
        EAST -> NORTH
    }
}

private fun Location1616.manhattanTo(other: Location1616): Location1616 = abs(row() - other.row()) + abs(col() - other.col())

private fun Location1616.advance(direction: Facing) =
    when (direction) {
        Facing.NORTH -> this.minusRow()
        Facing.EAST -> this.plusCol()
        Facing.SOUTH -> this.plusRow()
        Facing.WEST -> this.minusCol()
    }
