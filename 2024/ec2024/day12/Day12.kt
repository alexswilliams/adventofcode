package ec2024.day12

import common.*

private val examples = loadFilesToLines("ec2024/day12", "example.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day12", "input.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day12.assertCorrect()
    benchmark { part1(puzzles[0]) } // 11.8µs
    benchmark { part2(puzzles[1]) } // 80.4µs
    benchmark { part3(puzzles[2]) } // 212µs
}

internal object Day12 : Challenge {
    override fun assertCorrect() {
        check(13, "P1 Example") { part1(examples[0]) }
        check(188, "P1 Puzzle") { part1(puzzles[0]) }

        check(20230, "P2 Puzzle") { part2(puzzles[1]) }

        check(8, "P3 Example") { part3(examples[1]) }
        check(734277, "P3 Puzzle") { part3(puzzles[2]) }
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
