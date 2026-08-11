package aoc2015.day13

import common.*

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
            .also { println("comparisons = $lookups") }
        check(664, "P1 Puzzle") { part1(puzzle) }
            .also { println("comparisons = $lookups") }

        check(640, "P2 Puzzle") { part2(puzzle) }
            .also { println("comparisons = $lookups") }
    }
}


private fun part1(input: List<String>): Int {
    val (people, rulesByPerson) = parseInput(input)
    lookups = 0
    return maxApproval(people, rulesByPerson)
}

private fun part2(input: List<String>): Int {
    val (originalPeople, rulesByPerson) = parseInput(input)
    val people = originalPeople.plus("Me")
    lookups = 0
    return maxApproval(people, rulesByPerson)
}

// Symmetry:
// - rotation - fixed by arbitrarily choosing on person to remain in a fixed position.
// - reflection - the guests could reverse the order; the recursive solution below doesn't solve this.
// Using a recusive approach by picking from the rules for each person, it takes 27 / 2639 / 6784 comparisons.
// A generating every permutation and doing the comparison fresh for each round takes 24 / 40320 / 362880 comparisons.
private var lookups: Int = 0

@Suppress("unused")
private fun forPermutationsOf(input: List<String>, limit: Long? = null, body: (newIndexOrder: IntArray) -> Unit) {
    val n = input.size
    val indexArray = IntArray(input.size) { it }
    val upperBound = factorial(input.size)

    body(indexArray)

    fun permute(): Boolean {
        var i = n - 1
        while (i > 0 && indexArray[i - 1] >= indexArray[i])
            i--
        if (i <= 0)
            return false
        var j = n
        while (j > i && indexArray[j - 1] <= indexArray[i - 1])
            j--
        // j points to the start of the tail of the list from where the rest of the list is bubble-sorted
        swap(indexArray, i - 1, j - 1)
        i++
        j = n
        while (i < j) {
            swap(indexArray, i - 1, j - 1)
            i++
            j--
        }
        return true
    }

    ((0..(limit ?: (upperBound - 2)))).forEach { _ ->
        if (!permute()) throw AssertionError()
        body(indexArray)
    }
}

fun swap(indexArray: IntArray, i1: Int, i2: Int) {
    val temp = indexArray[i1]
    indexArray[i1] = indexArray[i2]
    indexArray[i2] = temp
}

private fun maxApproval(people: List<String>, rulesByPerson: Map<String, Map<String, Int>>): Int {
//    val firstPerson = people.first()
//    val unseated = people.tail()
//    var max = 0
//    forPermutationsOf(unseated) { newIndexOrder ->
//        max = max(
//            max,
//            newIndexOrder.asList().zipWithNext().sumOf { (ind1, ind2) ->
//                (rulesByPerson[unseated[ind1]]?.get(unseated[ind2]) ?: 0).also { comparisons++ }
//            } + (rulesByPerson[unseated[newIndexOrder.first()]]?.get(firstPerson) ?: 0).also { comparisons++ } +
//                    (rulesByPerson[unseated[newIndexOrder.last()]]?.get(firstPerson) ?: 0)).also { comparisons++ }
//    }
//    return max

    val cache = HashMap<Pair<String, Set<String>>, Int>()
    fun maxApproval(lastPerson: String, seatedPeople: List<String>): Int {
        cache[lastPerson to seatedPeople.toSet()]?.also { lookups++; return it }
        if (people.size - seatedPeople.size == 1) {
            val remainingPerson = people.first { it !in seatedPeople }
            lookups += 3
            val rules = rulesByPerson[remainingPerson]
            return ((rules?.get(lastPerson) ?: 0) + (rules?.get(people.first()) ?: 0))
        }
        val rules = rulesByPerson[lastPerson]
        val remainingPeople = people.filter { it !in seatedPeople }
        return remainingPeople.maxOf { person ->
            lookups++
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
