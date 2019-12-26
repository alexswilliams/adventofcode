package day1

import common.fromClasspathFileToLines
import kotlin.test.assertEquals


fun main() {
    runTests()

    val inputLines = "day1/input.txt".fromClasspathFileToLines()
    val moduleMasses = inputLines.map { it.toLong() }

    val fuelForModules = moduleMasses.map { massToFuel(it) }.sum()
    println("Part 1: Sum = $fuelForModules")

    val totalFuel = moduleMasses.map { totalFuelNeeded(it) }.sum()
    println("Part 2: Sum = $totalFuel")
}

private fun massToFuel(it: Long) = Math.floorDiv(it, 3) - 2

private tailrec fun totalFuelNeeded(unaccountedForMass: Long, accumulator: Long = 0L): Long =
    when (val extraFuel = massToFuel(unaccountedForMass)) {
        -2L, -1L, 0L -> accumulator
        else -> totalFuelNeeded(extraFuel, accumulator + extraFuel)
    }


private fun runTests() {
    assertEquals(2L, massToFuel(12L))
    assertEquals(2L, massToFuel(14L))
    assertEquals(654L, massToFuel(1969L))
    assertEquals(33583L, massToFuel(100756L))

    assertEquals(2L, totalFuelNeeded(14L))
    assertEquals(966L, totalFuelNeeded(1969L))
    assertEquals(50346L, totalFuelNeeded(100756L))
}
