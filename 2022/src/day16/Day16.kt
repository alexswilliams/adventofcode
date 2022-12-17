package day16

import common.*
import common.BitSet
import java.util.*
import kotlin.test.*

private val exampleInput = "day16/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day16/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 1651
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 1707

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 1940, took 16ms

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 2469, took 2.6s
}

private fun part1(input: List<String>): Int {
    val valves = parseInput(input)
    val targetValves = valves.filter { it.rate > 0 }.map { it.ref }.toSet().asBitSet()
    val aa = valves.first { it.id == "AA" }
    val timeToReachCache = buildDistanceCache(targetValves plusItem aa.ref, valves)

    return maxFlowPossibleForValveSet(valves, timeLimit = 30, targetValves, timeToReachCache, startAt = aa.idx)
}

private fun part2(input: List<String>): Int {
    val valves = parseInput(input)
    val targetValves = valves.filter { it.rate > 0 }.map { it.ref }.toSet().asBitSet()
    val aa = valves.first { it.id == "AA" }
    val timeToReachCache = buildDistanceCache(targetValves plusItem aa.ref, valves)

    return allCombinationsOf(targetValves)
        .associateWith { maxFlowPossibleForValveSet(valves, timeLimit = 26, targets = it, timeToReachCache, startAt = aa.idx) }
        .let { it.mapValues { (set, maxFlow) -> maxFlow + it[targetValves.excluding(set)]!! } }
        .values.max()
}


private fun allCombinationsOf(inputs: BitSet): Set<BitSet> {
    val n = inputs.countOneBits()
    val size = 3 + (1 until n).sumOf { r -> factorial(n) / (factorial(r) * factorial(n - r)) }.toInt()
    return buildSet((size * 4) / 3) {
        this.add(EMPTY_BITSET) // length 0
        repeat(inputs.countOneBits()) {
            this.toList().forEach { prefix -> inputs.excluding(prefix).forEach { suffix -> this.add(prefix plusItem suffix) } }
        }
        this.add(inputs)
    }
}


private fun maxFlowPossibleForValveSet(valves: Array<Valve>, timeLimit: Int, targets: BitSet, timeToReach: Array<IntArray?>, startAt: Int): Int {
    fun maxFlowFromVisitingDownstreamValves(thisValve: Int, elapsed: Int, flowPerMinute: Int, valvesOpen: BitSet): Int {
        if (valvesOpen == targets)
            return ((timeLimit - elapsed) * flowPerMinute)

        val pathsDistancesFromValve = timeToReach[thisValve]!!
        return targets.excluding(valvesOpen).maxOf { nextValve ->
            val nextIdx = nextValve.toIndex()
            val movementTime: Int = pathsDistancesFromValve[nextIdx]

            if (movementTime + 1 + elapsed >= timeLimit)
                (timeLimit - elapsed) * flowPerMinute
            else
                flowPerMinute * (movementTime + 1) + maxFlowFromVisitingDownstreamValves(
                    nextIdx,
                    elapsed = elapsed + movementTime + 1,
                    flowPerMinute = flowPerMinute + valves[nextIdx].rate,
                    valvesOpen = valvesOpen plusItem nextValve
                )
        }
    }
    return maxFlowFromVisitingDownstreamValves(startAt, 0, 0, EMPTY_BITSET)
}


private fun buildDistanceCache(targetValves: BitSet, valves: Array<Valve>) =
    Array(valves.size) { fromIdx ->
        if (1L shl (fromIdx + 1) in targetValves)
            IntArray(valves.size) { toIdx ->
                if (1L shl (toIdx + 1) in targetValves) timeToReach(fromIdx, toIdx, valves)
                else 0
            } else null
    }

private fun timeToReach(nextValve: Int, from: Int, withGraph: Array<Valve>): Int {
    fun distanceByDijkstra(valves: Array<Valve>, start: Int, target: Int): Int {
        val smallestDistance = buildMap {
            valves.indices.forEach { idx -> put(idx, Int.MAX_VALUE) }
            put(start, 0)
        }.toMutableMap()
        val priorityQueue = PriorityQueue<Int> { s1, s2 -> smallestDistance[s1]!!.compareTo(smallestDistance[s2]!!) }
            .apply { valves.indices.forEach(::offer) }

        while (true) {
            val u = priorityQueue.poll()
            if (u == target) return smallestDistance[u]!!
            val distanceViaU = smallestDistance[u]!! + 1
            for (n in valves[u].otherIndexes) {
                if (distanceViaU < smallestDistance[n]!!) {
                    smallestDistance[n] = distanceViaU
                    priorityQueue.remove(n)
                    priorityQueue.offer(n)
                }
            }
        }
    }
    return distanceByDijkstra(withGraph, from, nextValve)
}


private val inputPattern = Regex("Valve ([A-Z]{2}) has flow rate=([0-9]+); tunnels? leads? to valves? ([, A-Z]+)")
private fun parseInput(input: List<String>): Array<Valve> = input
    .mapMatching(inputPattern)
    .let { matches ->
        matches.mapIndexed { index, (id, rate, links) ->
            Valve(
                id = id,
                ref = 1L shl (index + 1),
                idx = index,
                rate = rate.toInt(),
                otherIndexes = links.split(", ").map { needle -> matches.indexOfFirst { (otherId) -> otherId == needle } }.toSet()
            )
        }
    }.toTypedArray()

private data class Valve(val id: String, val ref: Long, val idx: Int, val rate: Int, val otherIndexes: Set<Int>)
