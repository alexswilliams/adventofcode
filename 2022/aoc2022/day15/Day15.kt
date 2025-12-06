package aoc2022.day15

import common.*
import kotlinx.coroutines.*
import kotlin.math.*

private val example = loadFilesToLines("aoc2022/day15", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day15", "input.txt").single()

internal fun main() {
    Day15.assertCorrect()
    benchmark { part1(puzzle, 2_000_000) } // 33.2µs
    benchmark(10) { part2(puzzle, 4_000_000) } // 155.8ms
}

internal object Day15 : Challenge {
    override fun assertCorrect() {
        check(26, "P1 Example") { part1(example, rowOfInterest = 10) }
        check(4665948, "P1 Puzzle") { part1(puzzle, rowOfInterest = 2_000_000) }

        check(56000011, "P2 Example") { part2(example, maxSize = 20) }
        check(13543690671045, "P2 Puzzle") { part2(puzzle, maxSize = 4_000_000) }
    }
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
    return runBlocking(Dispatchers.Default) {
        (1..128).let { workers -> workers.map { (it - 1) * maxSize / workers.size..it * maxSize / workers.size } }
            .map { rowRange ->
                async {
                    rowRange.forEach { row ->
                        val ranges = sensorRangesForRow(sensors, row).clampTo(0, maxSize)
                        if (ranges.size > 1) {
                            val beaconsOnRow = sensors.filter { sensor -> sensor.yB == row }.map { it.xB }.toSet()
                            val gaps = ranges.allValuesMissingBefore(maxSize).minus(beaconsOnRow)
                            if (gaps.isNotEmpty()) {
                                return@async row + gaps.single() * 4_000_000L
                            }
                        }
                    }
                    return@async 0L
                }
            }.awaitAll()
    }.first { it > 0 }
}

private fun List<IntRange>.allValuesMissingBefore(maxInclusive: Int): List<Int> {
    tailrec fun itr(range: List<IntRange>, soFar: List<Int>, startAt: Int = 0): List<Int> {
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
    }.mergeAdjacentIntRanges()

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
