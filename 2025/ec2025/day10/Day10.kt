package ec2025.day10

import common.*

private val examples = loadFilesToGrids(
    "ec2025/day10",
    "example1.txt", "example2.txt", "example3a.txt", "example3b.txt", "example3c.txt", "example3d.txt", "example3e.txt"
)
private val puzzles = loadFilesToGrids("ec2025/day10", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day10.assertCorrect()
    benchmark { part1(puzzles[0]) } // 133.9µs
    benchmark(100) { part2(puzzles[1]) } // 16.5ms
    benchmark(10) { part3(puzzles[2]) } // 182.6ms
}

internal object Day10 : Challenge {
    override fun assertCorrect() {
        check(27, "P1 Example") { part1(examples[0], 3) }
        check(155, "P1 Puzzle") { part1(puzzles[0]) }

        check(27, "P2 Example") { part2(examples[1], 3) }
        check(1733, "P2 Puzzle") { part2(puzzles[1]) }

        check(15, "P3 Example A") { part3(examples[2]) }
        check(8, "P3 Example B") { part3(examples[3]) }
        check(44, "P3 Example C") { part3(examples[4]) }
        check(4406, "P3 Example D") { part3(examples[5]) }
        check(13033988838L, "P3 Example E") { part3(examples[6]) }
        check(1115173637703L, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: Grid, moves: Int = 4): Int {
    val dragon = input.locationOf('D')
    val allSheep = input.allLocationOf('S').toSet()

    val allMoves = (1..moves).fold(setOf(dragon)) { acc, _ -> acc + acc.flatMap { input.dragonMovesFrom(it) } }
    val eatenSheep = allMoves intersect allSheep
    return eatenSheep.size
}

private fun part2(input: Grid, rounds: Int = 20): Int {
    val hides = input.allLocationOf('#').toSet()
    var sheepBefore = input.allLocationOf('S')
    var dragons = setOf(input.locationOf('D'))
    var eaten = 0
    repeat(times = rounds) {
        dragons = dragons.flatMapTo(HashSet()) { input.dragonMovesFrom(it) }
        val sheepAfterEatenForFirstTime = sheepBefore - (dragons - hides)
        val sheepAfterSheepMovement = sheepAfterEatenForFirstTime.mapNotNull { if (it.row() == input.height - 1) null else it.plusRow() }
        val sheepAfterEatenForSecondTime = sheepAfterSheepMovement - (dragons - hides)
        eaten += sheepBefore.size - sheepAfterEatenForFirstTime.size + sheepAfterSheepMovement.size - sheepAfterEatenForSecondTime.size
        sheepBefore = sheepAfterEatenForSecondTime
    }
    return eaten
}

private fun part3(input: Grid): Long {
    val dragon = input.locationOf('D')
    val sheep = input.allLocationOf('S')
    val hides = input.allLocationOf('#')
    val bottomEscapeHeights = input.transpose().mapIndexed { colIndex, col -> (col.size - col.takeLastWhile { it == '#' }.size) by16 colIndex }

    val cache = mutableMapOf<Pair<List<Location1616>, Location1616>, Long>()
    fun movesBeneath(state: Pair<List<Location1616>, Location1616>): Long {
        cache[state]?.let { return it }
        val (sheep, dragon) = state
        if (sheep.isEmpty()) return 1L

        val allDoableSheepMoves = possibleSheepStates(sheep, if (dragon in hides) null else dragon)
            .ifEmpty { listOf(sheep) }
            .filter { it.none { shp -> shp in bottomEscapeHeights } }
        // if no move was possible, this will be a list of size 1 containing the current state;
        // 0 indicates that all moves resulted in all remaining sheep escaping
        if (allDoableSheepMoves.isEmpty()) return 0L.also { cache[state] = 0 }

        val allDragonMoves = input.dragonMovesFrom(dragon)
        return allDoableSheepMoves.sumOf { sheepAfterMove ->
            allDragonMoves.sumOf { dragonAfterMove ->
                val sheepAfterEaten =
                    if (dragonAfterMove in hides || dragonAfterMove !in sheepAfterMove) sheepAfterMove
                    else sheepAfterMove.minus(dragonAfterMove)
                // no need to check previous states for loops, the dragon always moves and the sheep are only blocked by the dragon, so there is never stalemate
                movesBeneath(sheepAfterEaten to dragonAfterMove)
            }
        }.also { cache[state] = it }
    }
    return movesBeneath(sheep to dragon)
}

private fun possibleSheepStates(sheep: List<Location1616>, dragon: Location1616?): List<List<Location1616>> =
    sheep.mapNotNull { shp ->
        if (shp.plusRow() == dragon) null
        else (buildList(sheep.size) {
            sheep.forEach { add(if (it == shp) shp.plusRow() else it) }
        })
    }

private fun Grid.dragonMovesFrom(centre: Location1616): List<Location1616> {
    val x = centre.col()
    val y = centre.row()
    return listOf(
        y - 1 by16 x - 2, y - 2 by16 x - 1, y - 2 by16 x + 1, y - 1 by16 x + 2,
        y + 1 by16 x - 2, y + 2 by16 x - 1, y + 2 by16 x + 1, y + 1 by16 x + 2,
    ).filter { it isWithin this }
}
