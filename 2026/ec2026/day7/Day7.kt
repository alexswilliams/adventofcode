package ec2026.day7

import common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

private val example = loadFilesToLines("ec2026/day7", "example1.txt", "example2a.txt", "example2b.txt", "example3.txt")
private val puzzle = loadFilesToLines("ec2026/day7", "input1.txt", "input2.txt", "input3.txt")


internal fun main() {
    Day7.assertCorrect()
    benchmark { part1(puzzle[0]) } // 44.6µs
    benchmark { part2(puzzle[1]) } // 202.4µs
    benchmark { part3(puzzle[2]) } // 2.3ms parallel, 12.5ms sequential
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

private fun part1(input: List<String>): Int = totalEnergyWithoutExplosions(input, 5)
private fun part2(input: List<String>): Int = totalEnergyWithoutExplosions(input, 10)

private fun totalEnergyWithoutExplosions(input: List<String>, initialAmount: Int): Int {
    // noticing that the input is sequential IDs starting from 1, this doesn't need sorting or associating, index === id - 1
    val clouds = input.mapMatching(Regex("id=(\\d+), energy=(\\d+), connections=([\\d,]+)"))
        .map { (id, energy, connections) -> Input(id.toInt(), energy.toInt(), connections.splitToInts(",")) }
    val cache = HashMap<Int, Int>()
    fun addedByImpulse(amount: Int, id: Int): Int {
        if (amount == 1) return 1
        cache[amount * 100 + id]?.let { return it }
        return (amount +
                clouds[id - 1].connections.sumOf { addedByImpulse(amount - 1, it) })
            .also { cache[amount * 100 + id] = it }
    }
    return clouds.sumOf { it.initialEnergy } + addedByImpulse(initialAmount, clouds.maxBy { it.connections.size }.id)
}


private fun part3(input: List<String>, explosionLimit: Int = 1_000_000): Int {
    val cloudsInputs = input.mapMatching(Regex("id=(\\d+), energy=(\\d+), connections=([\\d,]+)"))
        .map { (id, energy, connections) -> Input(id.toInt(), energy.toInt(), connections.splitToInts(",")) }

    return runBlocking(Dispatchers.Default) {
        cloudsInputs.map { async { totalEnergyStartingAt(it.id, cloudsInputs, explosionLimit) } }.awaitAll().max()
    }
//    return cloudsInputs.map { it.id }.maxOf { totalEnergyStartingAt(it, cloudsInputs, explosionLimit) }
}

private fun totalEnergyStartingAt(startId: Int, inputs: List<Input>, explosionLimit: Int): Int {
    @Suppress("ArrayInDataClass")
    data class State(val id: Int, var currentEnergy: Int, var impulsesToApply: IntArray = IntArray(10), var exploded: Boolean = false)

    val clouds = inputs.map { State(it.id, it.initialEnergy) }.toTypedArray()
    val connections = inputs.map { it.connections }.toTypedArray()
    val toApplyNext = Array(clouds.size) { IntArray(10) }

    clouds[startId - 1].impulsesToApply[10 - 1] = 1
    val cloudsToProcess = ArrayList<State>(clouds.size)
        .apply { add(clouds[startId - 1]) }

    while (true) {
        if (cloudsToProcess.isEmpty())
            return clouds.sumOf { it.currentEnergy }

        toApplyNext.forEach { it.fill(0) }

        cloudsToProcess.forEach { cloud ->
            cloud.currentEnergy += cloud.impulsesToApply.sumOfIndexed { energy, quantity -> (energy + 1) * quantity }
            connections[cloud.id - 1].forEach { connectionId ->
                if (!clouds[connectionId - 1].exploded) {
                    cloud.impulsesToApply.forEachIndexed { energy, quantity ->
                        if (energy >= 1)
                            toApplyNext[connectionId - 1][energy - 1] += quantity
                    }
                }
            }
        }

        val explodingClouds = cloudsToProcess.filter { it.currentEnergy >= explosionLimit }
        explodingClouds.forEach {
            it.exploded = true
            it.currentEnergy = 0
            it.impulsesToApply.fill(0)
        }
        explodingClouds.forEach { cloud ->
            inputs[cloud.id - 1].connections.forEach { id ->
                if (!clouds[id - 1].exploded)
                    toApplyNext[id - 1][10 - 1] += 1
            }
        }

        cloudsToProcess.clear()
        toApplyNext.forEachIndexed { index, impulses ->
            if (!clouds[index].exploded) {
                impulses.copyInto(clouds[index].impulsesToApply)
                if (impulses.any { it > 0 })
                    cloudsToProcess.add(clouds[index])
            }
        }
    }
}
