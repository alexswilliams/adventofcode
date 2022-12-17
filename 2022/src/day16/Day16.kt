package day16

import common.*
import java.util.*
import kotlin.test.*
import kotlin.time.*

private val exampleInput = "day16/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day16/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 1651
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 1707

fun main() {
//    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
//    part1(puzzleInput).also { println("Part 1: $it") } // 1940

//    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
//    part2(puzzleInput).also { println("Part 2: $it") } // 2469

//    repeat(20) { part1(puzzleInput) }
    println(measureTime { repeat(20) { part1(puzzleInput) } }.div(20))
//    repeat(20) { part2(puzzleInput) }
//    println(measureTime { repeat(20) { part2(puzzleInput) } }.div(20))
}

private fun part1(input: List<String>): Int {
    val valves = parseInput(input)
    val targetValves = valves.values.filter { it.rate > 0 }.map { it.ref }.toSet()
    val refOfAA = valves.values.first { it.id == "AA" }.ref
    val timeToReachCache = buildDistanceCache(targetValves, refOfAA, valves)

    return maxFlowPossibleForValveSet(
        startAt = refOfAA,
        valves = valves,
        timeLimit = 30,
        targetValves = targetValves,
        timeToReachCache = timeToReachCache
    )
}

private fun part2(input: List<String>): Int {
    val valves = parseInput(input)
    val targetValves = valves.values.filter { it.rate > 0 }.map { it.ref }.toSet()
    val refOfAA = valves.values.first { it.id == "AA" }.ref
    val timeToReachCache = buildDistanceCache(targetValves, refOfAA, valves)

    val allCombinations = allCombinationsOf(targetValves)
    return allCombinations
        .associateWith { maxFlowPossibleForValveSet(valves, 26, it, timeToReachCache, refOfAA) }
        .let { it.mapValues { (set, maxFlow) -> maxFlow + it[targetValves.minus(set)]!! } }
        .values.max()
}


private fun <T> allCombinationsOf(inputs: Set<T>): Set<Set<T>> {
    return buildSet {
        this.add(emptySet()) // length 0
        repeat((1 until inputs.size).count()) {
            this.toList().forEach { prefix -> inputs.filter { it !in prefix }.forEach { suffix -> this.add(prefix + suffix) } }
        }
        this.add(inputs)
    }
}


private fun maxFlowPossibleForValveSet(
    valves: Map<Long, Valve>,
    timeLimit: Int,
    targetValves: Set<Long>,
    timeToReachCache: Map<Long, Map<Long, Int>>, // it[from][to]=distance
    startAt: Long
): Int {
    fun maxFlowFromVisitingDownstreamValves(thisValve: Long, elapsed: Int, flowPerMinute: Int, valvesOpen: List<Long>): Int {
        if (valvesOpen.size == targetValves.size)
            return ((timeLimit - elapsed) * flowPerMinute)

        val pathsDistancesFromValve = timeToReachCache[thisValve]!!
        return targetValves.filter { it !in valvesOpen }.maxOf { nextValve ->
            val movementTime = pathsDistancesFromValve[nextValve]!!
            if (movementTime + 1 + elapsed >= timeLimit)
                (timeLimit - elapsed) * flowPerMinute
            else
                flowPerMinute * (movementTime + 1) + maxFlowFromVisitingDownstreamValves(
                    nextValve,
                    elapsed = elapsed + movementTime + 1,
                    flowPerMinute = flowPerMinute + valves[nextValve]!!.rate,
                    valvesOpen = valvesOpen.plus(nextValve)
                )
        }
    }
    return maxFlowFromVisitingDownstreamValves(startAt, 0, 0, emptyList())
}


private fun buildDistanceCache(targetValves: Set<Long>, refOfAA: Long, valves: Map<Long, Valve>) =
    (targetValves + refOfAA).associateWith { from -> targetValves.associateWith { target -> timeToReach(target, from, valves) } }

private fun timeToReach(nextValve: Long, from: Long, withGraph: Map<Long, Valve>): Int {
    fun distanceByDijkstra(valves: Map<Long, Valve>, start: Long, target: Long): Int {
        val smallestDistance = buildMap {
            valves.keys.forEach { id -> put(id, Int.MAX_VALUE) }
            put(start, 0)
        }.toMutableMap()
        val priorityQueue = PriorityQueue<Long> { s1, s2 -> smallestDistance[s1]!!.compareTo(smallestDistance[s2]!!) }
            .apply { valves.keys.forEach(::offer) }

        while (true) {
            val u = priorityQueue.poll()
            if (u == target) return smallestDistance[u]!!
            val distanceViaU = smallestDistance[u]!! + 1
            for (n in valves[u]!!.otherRefs) {
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
private fun parseInput(input: List<String>): Map<Long, Valve> = input
    .mapMatching(inputPattern)
    .mapIndexed { index, (id, rate, links) -> Valve(id, 1L shl (index + 1), rate.toInt(), links.split(", ")) }
    .associateBy { it.ref }
    .apply { values.forEach { valve -> valve.otherRefs.addAll(valve.others.map { id -> values.first { it.id == id }.ref }) } }

private data class Valve(val id: String, val ref: Long, val rate: Int, val others: List<String>, val otherRefs: MutableList<Long> = mutableListOf())
