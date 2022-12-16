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
    part2(puzzleInput, maxSize = 4_000_000).also { println("Part 2: $it") } // 13543690671045
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
    (0..maxSize)
        // all rows in this range take part in sensor data at some point, so can't filter any out
        .forEach { row ->
            val ranges = sensorRangesForRow(sensors, row).clampTo(0, maxSize)
            if (ranges.size > 1) {
                val beaconsOnRow = sensors.filter { sensor -> sensor.yB == row }.map { it.xB }.toSet()
                val gaps = ranges.allValuesMissingBetween(0, maxSize).minus(beaconsOnRow)
                if (gaps.isNotEmpty()) {
                    return row + gaps.single() * 4_000_000L
                }
            }
        }
    throw Exception("Pattern had no gaps")
}

private fun List<IntRange>.allValuesMissingBetween(minInclusive: Int, maxInclusive: Int): List<Int> {
    tailrec fun itr(range: List<IntRange>, soFar: List<Int>, startAt: Int = minInclusive): List<Int> {
        val iInAny = this.find { startAt in it }
        return when {
            startAt >= maxInclusive -> soFar
            iInAny != null -> itr(range, soFar, iInAny.last + 1)
            else -> itr(range, soFar + startAt, startAt + 1)
        }
    }
    return itr(this.sortedBy { it.first }, emptyList())
}


private fun sensorRangesForRow(sensors: List<SensorData>, row: Int): List<IntRange> =
    buildList(sensors.size + 1) {
        for (s in sensors) {
            if (sensorAffectsRow(s, row)) {
                val horizontalOffsetFromCentreOfSensor = s.distanceToBeacon - (s.yS - row).absoluteValue
                val xMin = s.xS - horizontalOffsetFromCentreOfSensor
                val xMax = s.xS + horizontalOffsetFromCentreOfSensor
                this.add(xMin..xMax)
            }
        }
    }.mergeAdjacent()

private fun sensorAffectsRow(s: SensorData, row: Int) = (s.yS == row
        || s.yS < row && s.yS + s.distanceToBeacon >= row
        || s.yS > row && s.yS - s.distanceToBeacon <= row)


private object InputParsing {
    private val inputPattern = Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)")
    fun parseAllSensors(input: List<String>): List<SensorData> = input
        .mapMatching(inputPattern)
        .mapTo(ArrayList(input.size + 1)) { (xS, yS, xB, yB) -> SensorData(xS.toInt(), yS.toInt(), xB.toInt(), yB.toInt()) }
}

private data class SensorData(val xS: Int, val yS: Int, val xB: Int, val yB: Int) {
    val distanceToBeacon = (xB - xS).absoluteValue + (yB - yS).absoluteValue
}
