package ec2024.day12

import common.*
import kotlin.test.*

private const val rootFolder = "ec2024/day12"
private val exampleInput = "$rootFolder/example.txt".fromClasspathFileToLines()
private val example3Input = "$rootFolder/example3.txt".fromClasspathFileToLines()
private val puzzleInput = "$rootFolder/input.txt".fromClasspathFileToLines()
private val puzzle2Input = "$rootFolder/input2.txt".fromClasspathFileToLines()
private val puzzle3Input = "$rootFolder/input3.txt".fromClasspathFileToLines()

internal fun main() {
    Day12.assertPart1Correct()
    Day12.assertPart2Correct()
    Day12.assertPart3Correct()
    benchmark { part1(puzzleInput) } // 11.8µs
    benchmark { part2(puzzle2Input) } // 80.4µs
    benchmark { part3(puzzle3Input) } // 212µs
}

internal object Day12 : ThreePartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(13, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(188, it) }
    }

    override fun assertPart2Correct() {
        part2(puzzle2Input).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(20230, it) }
    }

    override fun assertPart3Correct() {
        part3(example3Input).also { println("[Example] Part 3: $it") }.also { assertEquals(8, it) }
        part3(puzzle3Input).also { println("[Puzzle] Part 3: $it") }.also { assertEquals(734277, it) }
    }
}


private fun part1(input: List<String>): Int =
    input.reversed().drop(1)
        .flatMapIndexed { row, line -> line.mapIndexedNotNull { col, c -> if (c == 'T') (row to col - 1) else null } }
        .sumOf { (r, c) -> findIntersectionsRankingsFor(r, c).max() }

private fun part2(input: List<String>): Int =
    input.reversed().drop(1)
        .flatMapIndexed { row, line -> line.mapIndexedNotNull { col, c -> if (c == 'T' || c == 'H') ((row to col - 1) to (if (c == 'T') 1 else 2)) else null } }
        .sumOf { (point, softness) -> findIntersectionsRankingsFor(point.first, point.second).max() * softness }

private fun part3(input: List<String>): Int =
    input.map { it.split(' ').let { s -> s[1].toInt() to s[0].toInt() } }
        .sumOf { (r, c) -> findIntersectionsRankingsFor(r - (c - c / 2), c / 2).min() }


// The entire grid right of x=y is reachable from the cannons (except 2 squares.)  Given the row and column are related
// to the power (and then offset by the segment number), 3 of those 4 variables are known, so you can work out the where
// in each arc a target cell could be.  The puzzle makes an assumption that if something is on one of the diagonals then
// choose the smallest power to reach it.
private fun findIntersectionsRankingsFor(r: Int, c: Int) =
    (1..3).mapNotNull { segment ->
        val ascendingPower = r - segment + 1
        val descendingPower = (ascendingPower + c) / 3
        when {
            r == c + segment - 1 -> ascendingPower * segment
            c >= ascendingPower + 1 && c <= 2 * ascendingPower -> ascendingPower * segment
            c >= 2 * descendingPower + 1 && r == segment + 3 * descendingPower - 1 - c -> descendingPower * segment
            else -> null
        }
    }

//private fun allPointsForProjectile(power: Int, segment: Int) =
//    ((1..power).map { t -> (segment + t - 1 to t) } +
//            ((power + 1)..(2 * power)).map { t -> (power + segment - 1) to t } +
//            ((2 * power + 1)..(3 * power + segment - 1)).mapIndexed { amountFallen, t -> (power + segment - 2 - amountFallen) to t })
