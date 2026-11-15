package ec2026.day7

import common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

private val example = loadFilesToLines("ec2026/day7", "example1.txt", "example2a.txt", "example2b.txt", "example3.txt")
private val puzzle = loadFilesToLines("ec2026/day7", "input1.txt", "input2.txt", "input3.txt")

private fun render(input: List<String>) {
    val clouds = input.mapMatching(Regex("id=(\\d+), energy=(\\d+), connections=([\\d,]+)")).map { (id, energy, connections) ->
        Input(id.toInt(), energy.toInt(), connections.splitToInts(","))
    }.associateBy { it.id }
    println("digraph {")
    clouds.forEach { (id, cloud) ->
        cloud.connections.forEach { connection ->
            print("  $id -> $connection;")
        }
        println()
    }
    println("}")
}


internal fun main() {
    render(example[3])
    Day7.assertCorrect()
    benchmark { part1(puzzle[0]) } // 110.8µs
//    benchmark(10) { part2(puzzle[1]) } // 12.4s
//    benchmark(1) { part3(puzzle[2]) } // 2m 6.7s - parallel, and I had to JAVA_TOOL_OPTIONS=-Xmx20g to get that...
}

internal object Day7 : Challenge {
    override fun assertCorrect() {
        check(102, "P1 Example") { part1(example[0]) }
        check(278, "P1 Puzzle") { part1(puzzle[0]) }

        check(1316, "P2 Example A") { part2(example[1]) }
        check(1_214_633, "P2 Example B") { part2(example[2]) }
        check(115_185_706, "P2 Puzzle") { part2(puzzle[1]) }

        check(187, "P3 Example") { part3(example[3], explosionLimit = 100) }
        check(36_426_609, "P3 Puzzle") { part3(puzzle[2]) }
    }
}

private data class Input(val id: Int, val initialEnergy: Int, val connections: List<Int>)

private fun part1(input: List<String>): Int {
    val clouds = input.mapMatching(Regex("id=(\\d+), energy=(\\d+), connections=([\\d,]+)")).map { (id, energy, connections) ->
        Input(id.toInt(), energy.toInt(), connections.splitToInts(","))
    }.associateBy { it.id }

    val (totalEnergy, _) = (5 downTo 1).fold(
        Pair(
            clouds.values.sumOf { it.initialEnergy },
            listOf(clouds.values.maxBy { it.connections.size }.id)
        )
    ) { (prevTotalEnergy, cloudsToPropagateTo), energyToAdd ->
        val addedAtThisStep = cloudsToPropagateTo.size * energyToAdd
        val nextClouds = cloudsToPropagateTo.flatMap { clouds[it]!!.connections }
        Pair(prevTotalEnergy + addedAtThisStep, if (energyToAdd == 1) listOf() else nextClouds)
    }
    return totalEnergy
}


private fun part2(input: List<String>): Int {
    val clouds = input.mapMatching(Regex("id=(\\d+), energy=(\\d+), connections=([\\d,]+)")).map { (id, energy, connections) ->
        Input(id.toInt(), energy.toInt(), connections.splitToInts(","))
    }.associateBy { it.id }

    val (totalEnergy, _) = (10 downTo 1).fold(
        Pair(
            clouds.values.sumOf { it.initialEnergy },
            listOf(clouds.values.maxBy { it.connections.size }.id)
        )
    ) { (prevTotalEnergy, cloudsToPropagateTo), energyToAdd ->
        val addedAtThisStep = cloudsToPropagateTo.size * energyToAdd
        val nextClouds = cloudsToPropagateTo.flatMap { clouds[it]!!.connections }
        Pair(prevTotalEnergy + addedAtThisStep, if (energyToAdd == 1) listOf() else nextClouds)
    }
    return totalEnergy
}


private fun part3(input: List<String>, explosionLimit: Int = 1_000_000): Int {
    val cloudsInputs = input.mapMatching(Regex("id=(\\d+), energy=(\\d+), connections=([\\d,]+)")).map { (id, energy, connections) ->
        Input(id.toInt(), energy.toInt(), connections.splitToInts(","))
    }.sortedBy { it.id }

    return runBlocking(Dispatchers.Default) {
        cloudsInputs.map { it.id }
            .map { id ->
                async {
                    totalEnergyStartingAt(id, cloudsInputs, explosionLimit)
                        .also { result -> println("Result for ID $id: $result") }
                }
            }
            .awaitAll()
            .max()
    }
//    return cloudsInputs.map { it.id }.maxOf { totalEnergyStartingAt(it, cloudsInputs, explosionLimit) }
}

private fun totalEnergyStartingAt(startId: Int, cloudsInputs: List<Input>, explosionLimit: Int): Int {
    data class State(val id: Int, var currentEnergy: Int, var impulsesToApply: List<Int> = ArrayList(), var exploded: Boolean = false)

    val clouds = cloudsInputs
        .map { State(it.id, it.initialEnergy) }.toTypedArray()

    clouds[startId - 1].impulsesToApply = listOf(10)
    while (true) {
        val impulsesToApply = clouds.filter { cloud -> !cloud.exploded && cloud.impulsesToApply.isNotEmpty() }
        if (impulsesToApply.isEmpty()) return clouds.filterNot { it.exploded }.sumOf { it.currentEnergy }

        val impulsesToApplyNextTime = Array<MutableList<Int>?>(clouds.size) { null }

        impulsesToApply.forEach { cloudReceivingImpulse ->
            cloudReceivingImpulse.currentEnergy += cloudReceivingImpulse.impulsesToApply.sum()
            val connections = cloudsInputs[cloudReceivingImpulse.id - 1].connections.filterNot { clouds[it - 1].exploded }
            val impulsesToPropagate = if (connections.isEmpty()) emptyList() else cloudReceivingImpulse.impulsesToApply.map { it - 1 }.filter { it > 0 }
            connections.forEach { connectionId ->
                if (impulsesToApplyNextTime[connectionId - 1] == null) impulsesToApplyNextTime[connectionId - 1] = ArrayList()
                impulsesToApplyNextTime[connectionId - 1]!!.addAll(impulsesToPropagate)
            }
        }

        impulsesToApply.filter { it.currentEnergy >= explosionLimit }.forEach { explodingCloud ->
            explodingCloud.currentEnergy = 0
            explodingCloud.impulsesToApply = emptyList()
            explodingCloud.exploded = true
            val connections = cloudsInputs[explodingCloud.id - 1].connections.filterNot { clouds[it - 1].exploded }
            connections.forEach { connectionId ->
                if (impulsesToApplyNextTime[connectionId - 1] == null) impulsesToApplyNextTime[connectionId - 1] = ArrayList()
                impulsesToApplyNextTime[connectionId - 1]!!.add(10)
            }
        }

        impulsesToApplyNextTime.forEachIndexed { index, impulses ->
            clouds[index].impulsesToApply = impulses.orEmpty()
        }
    }
}
