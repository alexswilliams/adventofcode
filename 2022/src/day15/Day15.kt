package day15

import common.*
import kotlin.math.*
import kotlin.test.*

private val exampleInput = "day15/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day15/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 26
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 56000011L

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput, rowOfInterest = 10))
    part1(puzzleInput, rowOfInterest = 2_000_000).also { println("Part 1: $it") } // 4665948

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput, maxSize = 20))
    part2(puzzleInput, maxSize = 4_000_000).also { println("Part 2: $it") } //
}

private fun part1(input: List<String>, rowOfInterest: Int): Int {
    val sensors = InputParsing.parseAllSensors(input)
    val beaconsOnRow = sensors
        .filter { sensor -> sensor.yB == rowOfInterest }
        .map { it.xB }.toSet()
    return sensorRangesForRow(sensors, rowOfInterest)
        .sumOf { range -> range.size - beaconsOnRow.count { x -> x in range } }
}

private fun part2(input: List<String>, maxSize: Int): Long {
    val sensors = InputParsing.parseAllSensors(input)
    (0..maxSize).forEach { row ->
        val ranges = sensorRangesForRow(sensors, row).clampTo(0, maxSize).sortedBy { it.first }
        if (ranges.size > 1 && ranges[0] != 0..maxSize) {
            val beaconsOnRow = sensors.filter { sensor -> sensor.yB == row }.map { it.xB }.toSet()
            val gaps = ranges.allValuesMissingBetween(0, maxSize).minus(beaconsOnRow)
            return row + gaps.single() * 4_000_000L
        }
    }
    throw Exception("Pattern had no gaps")
}

private fun List<IntRange>.allValuesMissingBetween(minInclusive: Int, maxInclusive: Int): List<Int> {
    tailrec fun itr(soFar: List<Int>, startAt: Int = minInclusive): List<Int> {
        return when {
            startAt >= maxInclusive -> soFar
            this.any { startAt in it } -> itr(soFar, this.first { startAt in it }.last + 1)
            else -> itr(soFar + startAt, startAt + 1)
        }
    }
    return itr(emptyList())
}


private fun sensorRangesForRow(sensors: List<SensorData>, row: Int): List<IntRange> = sensors
    .filter { sensor ->
        sensor.yS == row
                || sensor.yS < row && sensor.yS + sensor.distanceToBeacon >= row
                || sensor.yS > row && sensor.yS - sensor.distanceToBeacon <= row
    }.map {
        val horizontalOffsetFromCentreOfSensor = it.distanceToBeacon - (it.yS - row).absoluteValue
        val xMin = it.xS - horizontalOffsetFromCentreOfSensor
        val xMax = it.xS + horizontalOffsetFromCentreOfSensor
        xMin..xMax
    }.simplifyAdjacent()


private object InputParsing {
    private val inputPattern = Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)")
    fun parseAllSensors(input: List<String>): List<SensorData> = input
        .mapMatching(inputPattern)
        .map { (xS, yS, xB, yB) -> SensorData(xS.toInt(), yS.toInt(), xB.toInt(), yB.toInt()) }
}

private data class SensorData(val xS: Int, val yS: Int, val xB: Int, val yB: Int) {
    val distanceToBeacon = (xB - xS).absoluteValue + (yB - yS).absoluteValue
}
