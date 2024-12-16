package aoc2024.day16

import common.*
import kotlinx.collections.immutable.*
import kotlin.math.*

private val examples = loadFilesToGrids("aoc2024/day16", "example1.txt", "example2.txt")
private val puzzle = loadFilesToGrids("aoc2024/day16", "input.txt").single()

internal fun main() {
    Day16.assertCorrect()
    benchmark(100) { part1(puzzle) } // 4.3ms
    benchmark(10) { part2(puzzle) } // 28.7ms
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

private fun Location1616.advance(direction: Facing) =
    when (direction) {
        Facing.NORTH -> this.minusRow()
        Facing.EAST -> this.plusCol()
        Facing.SOUTH -> this.plusRow()
        Facing.WEST -> this.minusCol()
    }

private fun part1(grid: Grid): Int = aStarLowestScore(grid)

private fun part2(grid: Grid): Int = aStarAllShortestPaths(grid).flatten().distinct().size

private fun pack(pos: Location1616, direction: Facing) = pos * 4 + direction.ordinal

private fun aStarLowestScore(grid: Grid): Int {
    fillDeadEnds(grid)
    val start = grid.locationOf('S')
    val end = grid.locationOf('E')

    data class Work(val pos: Location1616, val direction: Facing, val path: PersistentList<Location1616>)

    val visited = mutableMapOf(pack(start, Facing.EAST) to 0)
    val work = TreeQueue<Work> { abs(it.pos.row() - end.row()) + abs(it.pos.col() - end.col()) }
    work.offer(Work(start, Facing.EAST, persistentListOf(start)), weight = 0)

    while (true) {
        val u = work.poll() ?: error("S to E not connected")
        val scoreAtU = visited[pack(u.pos, u.direction)]!!

        val ahead = u.pos.advance(u.direction)
        if (ahead == end) return scoreAtU + 1

        if (grid.at(ahead) != '#') {
            val newScore = scoreAtU + 1
            val oldScoreAtAhead = visited[pack(ahead, u.direction)] ?: Int.MAX_VALUE
            if (newScore < oldScoreAtAhead) {
                visited[pack(ahead, u.direction)] = newScore
                work.offerOrReposition(Work(ahead, u.direction, u.path.plus(ahead)), oldScoreAtAhead, newScore)
            }
        }
        val left = u.pos.advance(u.direction.turnLeft())
        if (grid.at(left) != '#') {
            val newScore = scoreAtU + 1000
            val oldScoreAtUButTurnedLeft = visited[pack(u.pos, u.direction.turnLeft())] ?: Int.MAX_VALUE
            if (newScore < oldScoreAtUButTurnedLeft) {
                visited[pack(u.pos, u.direction.turnLeft())] = newScore
                work.offerOrReposition(Work(u.pos, u.direction.turnLeft(), u.path), oldScoreAtUButTurnedLeft, newScore)
            }
        }
        val right = u.pos.advance(u.direction.turnRight())
        if (grid.at(right) != '#') {
            val newScore = scoreAtU + 1000
            val oldScoreAtUButTurnedRight = visited[pack(u.pos, u.direction.turnRight())] ?: Int.MAX_VALUE
            if (newScore < oldScoreAtUButTurnedRight) {
                visited[pack(u.pos, u.direction.turnRight())] = newScore
                work.offerOrReposition(Work(u.pos, u.direction.turnRight(), u.path), oldScoreAtUButTurnedRight, newScore)
            }
        }
    }
}

private fun aStarAllShortestPaths(grid: Grid): List<List<Location1616>> {
    fillDeadEnds(grid)
    val bestScore = aStarLowestScore(grid)
    val start = grid.locationOf('S')
    val end = grid.locationOf('E')

    data class Work(val pos: Location1616, val direction: Facing, val path: PersistentList<Location1616>, val score: Int)

    val visited = mutableMapOf(pack(start, Facing.EAST) to 0)
    val work = TreeQueue<Work> { abs(it.pos.row() - end.row()) + abs(it.pos.col() - end.col()) }
    work.offer(Work(start, Facing.EAST, persistentListOf(start), 0), weight = 0)

    val shortestPaths = mutableListOf<List<Location1616>>()

    while (true) {
        val u = work.poll() ?: return shortestPaths

        val ahead = u.pos.advance(u.direction)
        if (ahead == end) {
            if (bestScore == u.score + 1)
                shortestPaths.add(u.path.plus(ahead))
            continue
        }

        if (grid.at(ahead) != '#') {
            val newScore = u.score + 1
            val oldScore = visited[pack(ahead, u.direction)] ?: Int.MAX_VALUE
            if (newScore <= bestScore && newScore <= oldScore) {
                visited[pack(ahead, u.direction)] = newScore
                work.offer(Work(ahead, u.direction, u.path.plus(ahead), newScore), newScore)
            }
        }
        val newScore = u.score + 1000
        if (newScore > bestScore) continue
        val left = u.pos.advance(u.direction.turnLeft())
        if (grid.at(left) != '#') {
            val oldScore = visited[pack(u.pos, u.direction.turnLeft())] ?: Int.MAX_VALUE
            if (newScore <= oldScore) {
                visited[pack(u.pos, u.direction.turnLeft())] = newScore
                work.offer(Work(u.pos, u.direction.turnLeft(), u.path, newScore), newScore)
            }
        }
        val right = u.pos.advance(u.direction.turnRight())
        if (grid.at(right) != '#') {
            val oldScore = visited[pack(u.pos, u.direction.turnRight())] ?: Int.MAX_VALUE
            if (newScore <= oldScore) {
                visited[pack(u.pos, u.direction.turnRight())] = newScore
                work.offer(Work(u.pos, u.direction.turnRight(), u.path, newScore), newScore)
            }
        }
    }
}
