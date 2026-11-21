package ec2026.day10

import common.*

private val example = loadFilesToGrids("ec2026/day10", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToGrids("ec2026/day10", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day10.assertCorrect()
    benchmark { part1(puzzle[0]) } // 77.8µs
    benchmark(10) { part2(puzzle[1]) } // 110.5ms
    benchmark(10) { part3(puzzle[2]) } // 359.5µs
}

internal object Day10 : Challenge {
    override fun assertCorrect() {
        check(29, "P1 Example") { part1(example[0]) }
        check(66, "P1 Puzzle") { part1(puzzle[0]) }

        check(53, "P2 Example (10 collisions)") { part2(example[1], 10) }
        check(50_000, "P2 Example (10,000 collisions)") { part2(example[1]) }
        check(132_934, "P2 Puzzle") { part2(puzzle[1]) }

        check(13_314_695_352_588L, "P3 Example") { part3(example[2]) }
        check(14_433_333_332_860, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: Grid): Int {
    val ghostShapes = listOf('>', '<', '^', 'v')
    val initialGhostLocations = input.allLocationsWhere { _, _, char -> char in ghostShapes }
    var ghosts = initialGhostLocations.map { input.at(it) to it }

    var collisions = 0

    repeat(10) {
        ghosts = ghosts.map { (char, loc) ->
            val originalNextLocation = when (char) {
                '<' -> loc.minusCol()
                '>' -> loc.plusCol()
                '^' -> loc.minusRow()
                'v' -> loc.plusRow()
                else -> throw IllegalStateException()
            }
            val wouldMoveIntoWall = input.at(originalNextLocation) == '#'
            if (wouldMoveIntoWall)
                collisions++
            val nextLocation = if (wouldMoveIntoWall) loc else originalNextLocation
            val nextDirection = if (!wouldMoveIntoWall) char else when (char) {
                '<' -> '^'
                '>' -> 'v'
                '^' -> '>'
                'v' -> '<'
                else -> throw IllegalStateException()
            }
            nextDirection to nextLocation
        }

        val ghostLocations = ghosts.map { it.second }
        ghosts = ghosts.map { (char, loc) ->
            val multipleGhostsShareThisSpace = ghostLocations.count { it == loc } > 1
            val nextDirection = if (multipleGhostsShareThisSpace) when (char) {
                '<' -> '>'
                '>' -> '<'
                '^' -> 'v'
                'v' -> '^'
                else -> throw IllegalStateException()
            } else char

            if (multipleGhostsShareThisSpace)
                collisions++

            nextDirection to loc
        }
    }
    return collisions
}

private fun part2(input: Grid, limit: Long = 10_000): Long {
    data class Ghost(val char: Char, val loc: Location1616, val opacity: Long)

    val ghostShapes = listOf('>', '<', '^', 'v')
    val initialGhostLocations = input.allLocationsWhere { _, _, char -> char in ghostShapes }
    var ghosts = initialGhostLocations.map { Ghost(input.at(it), it, limit) }

    var rounds = 0L

    while (ghosts.isNotEmpty()) {
        rounds++

        ghosts = ghosts.map { (char, loc, opacity) ->
            val originalNextLocation = when (char) {
                '<' -> loc.minusCol()
                '>' -> loc.plusCol()
                '^' -> loc.minusRow()
                'v' -> loc.plusRow()
                else -> throw IllegalStateException()
            }
            val wouldMoveIntoWall = input.at(originalNextLocation) == '#'
            val nextLocation = if (wouldMoveIntoWall) loc else originalNextLocation
            val nextDirection = if (!wouldMoveIntoWall) char else when (char) {
                '<' -> '^'
                '>' -> 'v'
                '^' -> '>'
                'v' -> '<'
                else -> throw IllegalStateException()
            }
            Ghost(nextDirection, nextLocation, if (wouldMoveIntoWall) opacity - 1 else opacity)
        }.filter { it.opacity > 0 }

        ghosts = ghosts.map { (char, loc, opacity) ->
            val multipleGhostsShareThisSpace = ghosts.count { it.loc == loc } > 1
            val nextDirection = if (multipleGhostsShareThisSpace) when (char) {
                '<' -> '>'
                '>' -> '<'
                '^' -> 'v'
                'v' -> '^'
                else -> throw IllegalStateException()
            } else char

            Ghost(nextDirection, loc, if (multipleGhostsShareThisSpace) opacity - 1 else opacity)
        }.filter { it.opacity > 0 }
    }
    return rounds
}


private fun part3(input: Grid, limit: Long = 1_000_000_000_000L): Long {
    data class Ghost(val char: Char, val loc: Location1616, val opacity: Long)
    data class GhostInPattern(val char: Char, val loc: Location1616)

    val ghostShapes = listOf('>', '<', '^', 'v')
    val initialGhostLocations = input.allLocationsWhere { _, _, char -> char in ghostShapes }
    var ghosts = initialGhostLocations.map { Ghost(input.at(it), it, limit) }

    var rounds = 0L
    val moveCache = LinkedHashMap<List<GhostInPattern>, Pair<Long, List<Ghost>>>()

    while (ghosts.isNotEmpty()) {
        rounds++

        val previouslySeenOnRound = moveCache.put(
            ghosts.map { GhostInPattern(it.char, it.loc) }.sortedWith(compareBy(GhostInPattern::loc, GhostInPattern::char)),
            rounds to ghosts
        )
        if (previouslySeenOnRound != null) {
            moveCache.clear()
            val (cycleStartRound, cycleStartGhosts) = previouslySeenOnRound
            val cycleLength = rounds - cycleStartRound
            val cycleDecreasePerGhost = ghosts.zip(cycleStartGhosts).map { (now, then) -> then.opacity - now.opacity }
            val cyclesRemainingPerGhost = ghosts.zip(cycleDecreasePerGhost).map { (ghost, decrease) -> ghost.opacity / decrease }
            val cyclesToSkip = cyclesRemainingPerGhost.min() - 1

            if (cyclesToSkip > 0) {
                ghosts = ghosts.zip(cycleDecreasePerGhost).map { (ghost, decrease) -> ghost.copy(opacity = ghost.opacity - decrease * cyclesToSkip) }
                rounds += cyclesToSkip * cycleLength
            }
        }


        ghosts = ghosts.map { (char, loc, opacity) ->
            val originalNextLocation = when (char) {
                '<' -> loc.minusCol()
                '>' -> loc.plusCol()
                '^' -> loc.minusRow()
                'v' -> loc.plusRow()
                else -> throw IllegalStateException()
            }
            val wouldMoveIntoWall = input.at(originalNextLocation) == '#'
            val nextLocation = if (wouldMoveIntoWall) loc else originalNextLocation
            val nextDirection = if (!wouldMoveIntoWall) char else when (char) {
                '<' -> '^'
                '>' -> 'v'
                '^' -> '>'
                'v' -> '<'
                else -> throw IllegalStateException()
            }
            if (wouldMoveIntoWall && opacity == 1L) moveCache.clear()
            Ghost(nextDirection, nextLocation, if (wouldMoveIntoWall) opacity - 1 else opacity)
        }.filter { it.opacity > 0 }

        ghosts = ghosts.map { (char, loc, opacity) ->
            val multipleGhostsShareThisSpace = ghosts.count { it.loc == loc } > 1
            val nextDirection = if (multipleGhostsShareThisSpace) when (char) {
                '<' -> '>'
                '>' -> '<'
                '^' -> 'v'
                'v' -> '^'
                else -> throw IllegalStateException()
            } else char
            if (multipleGhostsShareThisSpace && opacity == 1L) moveCache.clear()
            Ghost(nextDirection, loc, if (multipleGhostsShareThisSpace) opacity - 1 else opacity)
        }.filter { it.opacity > 0 }
    }
    return rounds
}
