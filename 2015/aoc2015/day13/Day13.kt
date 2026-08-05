package aoc2015.day13

import common.Challenge
import common.benchmark
import common.loadFilesToLines
import common.mapMatching

private val example = loadFilesToLines("aoc2015/day13", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2015/day13", "input.txt").single()

internal fun main() {
    Day13.assertCorrect()
    benchmark(100) { part1(puzzle) } // 1.5ms
    benchmark(100) { part2(puzzle) } // 2.8ms
}

internal object Day13 : Challenge {
    override fun assertCorrect() {
        check(330, "P1 Example") { part1(example) }
            .also { println("comparisons = $x") }
        check(664, "P1 Puzzle") { part1(puzzle) }
            .also { println("comparisons = $x") }

        check(640, "P2 Puzzle") { part2(puzzle) }
            .also { println("comparisons = $x") }
    }
}


private fun part1(input: List<String>): Int {
    val (people, rulesByPerson) = parseInput(input)
    x = 0
    return maxApproval(people, rulesByPerson)
}

private fun part2(input: List<String>): Int {
    val (originalPeople, rulesByPerson) = parseInput(input)
    val people = originalPeople.plus("Me")
    x = 0
    return maxApproval(people, rulesByPerson)
}

private var x: Int = 0

private fun maxApproval(people: List<String>, rulesByPerson: Map<String, Map<String, Int>>): Int {
    val cache = HashMap<Pair<String, Set<String>>, Int>()
    fun maxApproval(lastPerson: String, seatedPeople: List<String>): Int {
        x++
        cache[lastPerson to seatedPeople.toSet()]?.also { return it }
        if (people.size - seatedPeople.size == 1) {
            val remainingPerson = people.first { it !in seatedPeople }
            val rules = rulesByPerson[remainingPerson]
            return ((rules?.get(lastPerson) ?: 0) + (rules?.get(people.first()) ?: 0))
        }
        val rules = rulesByPerson[lastPerson]
        val remainingPeople = people.filter { it !in seatedPeople }
        return remainingPeople.maxOf { person ->
            val afterPlacement = (rules?.get(person) ?: 0)
            afterPlacement + maxApproval(person, seatedPeople.plus(person))
        }.also { cache[lastPerson to seatedPeople.toSet()] = it }
    }
    return maxApproval(people.first(), listOf(people.first()))
}

private fun parseInput(input: List<String>): Pair<List<String>, Map<String, Map<String, Int>>> {
    val rules = input.mapMatching("([A-Za-z]+) would (gain|lose) (\\d+) happiness units by sitting next to ([A-Za-z]+).".toRegex())
        .groupBy({ (a, _, _, b) -> if (a < b) Pair(a, b) else Pair(b, a) }) { (_, dir, amount) -> amount.toInt() * if (dir == "gain") 1 else -1 }
        .mapValues { it.value.sum() }
    val people = rules.keys.flatMap { (a, b) -> listOf(a, b) }.distinct()
    val rulesByPerson = people.associateWith { person ->
        rules.mapNotNull {
            if (it.key.first == person) (it.key.second to it.value) else
                if (it.key.second == person) (it.key.first to it.value) else null
        }.toMap()
    }
    return Pair(people, rulesByPerson)
}
