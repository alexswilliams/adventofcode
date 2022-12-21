package day19

import common.*
import day19.RobotType.*
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.test.*

private val exampleInput = "day19/example.txt".fromClasspathFileToLines()
private val puzzleInput = "day19/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 33
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 62 * 56

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 960, took 33.8s

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 2040, took 4m 43.4s ... good enough.
}

private fun part1(input: List<String>): Int {
    val blueprints = parseInput(input)
    return runBlocking(Dispatchers.Default) {
        blueprints.mapIndexed { index, blueprint ->
            async {
                val quality = maxGeodesFromRobotRecipe(intArrayOf(0), emptyList(), blueprint, 24).also { println("Blueprint $index: $it") }
                quality * (index + 1)
            }
        }.awaitAll().sum()
    }
}

private fun part2(input: List<String>): Int {
    val blueprints = parseInput(input)
    return runBlocking(Dispatchers.Default) {
        blueprints.take(3).mapIndexed { index, blueprint ->
            async {
                maxGeodesFromRobotRecipe(intArrayOf(0), emptyList(), blueprint, 32).also { println("Blueprint $index: $it") }
            }
        }.awaitAll().product()
    }
}


private fun maxGeodesFromRobotRecipe(bestSoFar: IntArray, robotCreationOrder: List<RobotType>, blueprint: Blueprint, minutes: Int): Int {
    if (robotCreationOrder.size > minutes) return 0 // this is not a valid recipe
    if (willNeverStartValidRobotRecipe(robotCreationOrder)) {
        return 0
    }

    val geodesFromThisRecipe = simulate(bestSoFar, robotCreationOrder, minutes, blueprint)
    if (geodesFromThisRecipe == -1) return 0 // this recipe and all that start with this sequence of robots will always run out of time
    if (geodesFromThisRecipe == -2) return 0 // this sequence prefix could never produce a better result than already found elsewhere

    return max(
        geodesFromThisRecipe,
        maxGeodesFromRobotRecipe(bestSoFar, robotCreationOrder.plus(GEODE), blueprint, minutes),
        maxGeodesFromRobotRecipe(bestSoFar, robotCreationOrder.plus(OBSIDIAN), blueprint, minutes),
        maxGeodesFromRobotRecipe(bestSoFar, robotCreationOrder.plus(CLAY), blueprint, minutes),
        maxGeodesFromRobotRecipe(bestSoFar, robotCreationOrder.plus(ORE), blueprint, minutes),
    )
}

private fun simulate(bestSoFar: IntArray, robotCreationOrder: List<RobotType>, deadline: Int, bp: Blueprint): Int {
    var oreRobots = 1
    var clayRobots = 0
    var obsidianRobots = 0
    var geodeRobots = 0
    var ore = 0
    var clay = 0
    var obsidian = 0
    var geodes = 0
    var time = 0

    for (nextRobot in robotCreationOrder) {
        val turnsToAcquireResources = when (nextRobot) {
            ORE -> max(0, (bp.oreOreCost - ore) divideRoundingUp oreRobots)
            CLAY -> max(0, (bp.clayOreCost - ore) divideRoundingUp oreRobots)
            OBSIDIAN -> max(0, (bp.obsidianOreCost - ore) divideRoundingUp oreRobots, (bp.obsidianClayCost - clay) divideRoundingUp clayRobots)
            GEODE -> max(0, (bp.geodeOreCost - ore) divideRoundingUp oreRobots, (bp.geodeObsidianCost - obsidian) divideRoundingUp obsidianRobots)
        }

        val turnsToBuildRobot = turnsToAcquireResources + 1
        time += turnsToBuildRobot
        if (time >= deadline) return -1 // Ran out of time while trying to create next robot

        ore += oreRobots * turnsToBuildRobot
        clay += clayRobots * turnsToBuildRobot
        obsidian += obsidianRobots * turnsToBuildRobot
        geodes += geodeRobots * turnsToBuildRobot
        when (nextRobot) {
            ORE -> {
                oreRobots++; ore -= bp.oreOreCost
            }

            CLAY -> {
                clayRobots++; ore -= bp.clayOreCost
            }

            OBSIDIAN -> {
                obsidianRobots++; ore -= bp.obsidianOreCost; clay -= bp.obsidianClayCost
            }

            GEODE -> {
                geodeRobots++; ore -= bp.geodeOreCost; obsidian -= bp.geodeObsidianCost
            }
        }

        val timeRemaining = deadline - time
        val totalPossibleGeodesFromThisPoint = geodes + (geodeRobots * timeRemaining) + (timeRemaining) * (timeRemaining + 1) / 2 // very optimistic
        if (totalPossibleGeodesFromThisPoint < bestSoFar[0]) return -2 // there is no point searching this subtree any further
//        println("Created $nextRobot at time $time; $ore+=$oreRobots ore, $clay+=$clayRobots clay, $obsidian+=$obsidianRobots obs, $geodes+=$geodeRobots")
    }
    val totalGeodesProduced = geodes + (geodeRobots * (deadline - time))
    if (bestSoFar[0] < totalGeodesProduced) {
        bestSoFar[0] = totalGeodesProduced
    }
    return totalGeodesProduced
}

private fun willNeverStartValidRobotRecipe(listOfMachines: List<RobotType>): Boolean {
    val firstClay = listOfMachines.indexOfFirst { it == CLAY }
    val firstObsidian = listOfMachines.indexOfFirst { it == OBSIDIAN }
    val firstGeode = listOfMachines.indexOfFirst { it == GEODE }
    if (firstClay == -1 && (firstGeode >= 0 || firstObsidian >= 0)) return true // you can't make geodes or obsidian before making clay
    if (firstObsidian == -1 && firstGeode >= 0) return true // you can't make geodes before you make obsidian
    return false
}

private enum class RobotType { ORE, CLAY, OBSIDIAN, GEODE }

private val inputMatcher =
    Regex("Blueprint (\\d+): Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore." + " Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian.")

private fun parseInput(input: List<String>): List<Blueprint> =
    input.mapMatching(inputMatcher).map { (number, oreCost, clayCost, obsidianOreCost, obsidianClayCost, geodeOreCost, geodeObsidianCost) ->
        Blueprint(
            number.toInt(),
            oreCost.toInt(),
            clayCost.toInt(),
            obsidianOreCost.toInt(),
            obsidianClayCost.toInt(),
            geodeOreCost.toInt(),
            geodeObsidianCost.toInt()
        )
    }

private data class Blueprint(
    val number: Int,
    val oreOreCost: Int,
    val clayOreCost: Int,
    val obsidianOreCost: Int,
    val obsidianClayCost: Int,
    val geodeOreCost: Int,
    val geodeObsidianCost: Int
)
