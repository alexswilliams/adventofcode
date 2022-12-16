package day16

import common.*
import java.util.*
import kotlin.test.*

private val exampleInput = "day16/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day16/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 1651
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 1707

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 1940

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
//    part2(puzzleInput).also { println("Part 2: $it") } //
}

private fun part1(input: List<String>): Int {
    val valves = parseInput(input)
    val targetValves = valves.values.filter { it.rate > 0 }.map { it.id }
    val timeToReachCache = (targetValves + "AA")
        .associateWith { from -> targetValves.associateWith { target -> timeToReach(target, from, valves) } }

    fun maxFlowFromVisitingDownstreamValves(thisValve: String, elapsed: Int, flowPerMinute: Int, valvesOpen: List<String>): Int {
        if (valvesOpen.size == targetValves.size)
            return ((30 - elapsed) * flowPerMinute)

        val pathsDistancesFromValve = timeToReachCache[thisValve]!!
        return targetValves.filter { it !in valvesOpen }.maxOf { nextValve ->
            val movementTime = pathsDistancesFromValve[nextValve]!!
            if (movementTime + 1 + elapsed >= 30)
                (30 - elapsed) * flowPerMinute
            else
                flowPerMinute * (movementTime + 1) + maxFlowFromVisitingDownstreamValves(
                    nextValve,
                    elapsed = elapsed + movementTime + 1,
                    flowPerMinute = flowPerMinute + valves[nextValve]!!.rate,
                    valvesOpen = valvesOpen.plus(nextValve)
                )
        }
    }
    return maxFlowFromVisitingDownstreamValves("AA", 0, 0, emptyList())
}

private fun part2(input: List<String>): Int {
    return 0
}


private val inputPattern = Regex("Valve ([A-Z]{2}) has flow rate=([0-9]+); tunnels? leads? to valves? ([, A-Z]+)")
private fun parseInput(input: List<String>): Map<String, Valve> = input
    .mapMatching(inputPattern)
    .map { (id, rate, links) -> Valve(id, rate.toInt(), links.split(", ")) }
    .associateBy { it.id }


private fun timeToReach(nextValve: String, from: String, withGraph: Map<String, Valve>): Int {
    fun distanceByDijkstra(valves: Map<String, Valve>, start: String, target: String): Int {
        val smallestDistance = buildMap {
            valves.keys.forEach { id -> put(id, Int.MAX_VALUE) }
            put(start, 0)
        }.toMutableMap()
        val priorityQueue = PriorityQueue<String> { s1, s2 -> smallestDistance[s1]!!.compareTo(smallestDistance[s2]!!) }
            .apply { valves.keys.forEach(::offer) }

        while (true) {
            val u = priorityQueue.poll()
            if (u == target) return smallestDistance[u]!!
            val distanceViaU = smallestDistance[u]!! + 1
            for (n in valves[u]!!.others) {
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


private data class Valve(val id: String, val rate: Int, val others: List<String>)
