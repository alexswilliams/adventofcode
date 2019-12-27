package day6

import common.fromClasspathFileToLines
import kotlin.test.assertEquals


fun main() {
    runTests()

    val chart = "day6/input.txt".fromClasspathFileToLines()
        .map { it.split(')').let { (parent, child) -> parent to child } }

    val allChildren = chart.map { it.second }.distinct()
    val allChildrenWithAncestorCount = chart.countAncestors(allChildren)
    val sumOfAncestors = allChildrenWithAncestorCount.sum()
    println("Part 1: sum = $sumOfAncestors")
    assertEquals(270768, sumOfAncestors)

    val countTransfers = chart.countTransfersBetween("SAN", "YOU") - 2
    println("Part 2: transfers = $countTransfers")
    assertEquals(451, countTransfers)
}

private fun Collection<Pair<String, String>>.countAncestors(children: List<String>): List<Int> {
    val parentsByChild = this.associateBy({ it.second }, { it.first })
    val cache = mutableMapOf<String, Int>()

    // With caching, part 1 takes 2982 node visits; without it takes 272259 node visits
    fun walkParentsWithCache(child: String, acc: Int = 0): Int =
        when (val cachedValue = cache[child]) {
            null -> when (val parent = parentsByChild[child]) {
                null -> acc
                else -> walkParentsWithCache(parent, acc + 1)
                    .also { cache[child] = it - acc }
            }
            else -> acc + cachedValue
        }

    return children.map { walkParentsWithCache(it) }
}


private fun Collection<Pair<String, String>>.countTransfersBetween(childA: String, childB: String): Int {
    val parentsByChild = this.associateBy({ it.second }, { it.first })

    val commonAncestor = parentsByChild.closestCommonAncestorOf(childA, childB)
    val (countA, countB, countCommon) = this.countAncestors(listOf(childA, childB, commonAncestor))

    return (countA + countB - countCommon * 2)
}

private tailrec fun Map<String, String>.ancestorsOf(child: String, acc: List<String> = emptyList()): List<String> =
    when (val parent = this[child]) {
        null -> listOf(child) + acc
        else -> this.ancestorsOf(parent, listOf(child) + acc)
    }

private fun Map<String, String>.closestCommonAncestorOf(childA: String, childB: String): String {
    val ancestorsA = this.ancestorsOf(childA)
    val ancestorsB = this.ancestorsOf(childB)
    val commonPart = ancestorsA.zip(ancestorsB) { a, b -> if (a == b) a else null }
        .takeWhile { it != null }
        .filterNotNull()
    return commonPart.last()
}

private fun runTests() {
    val chart = listOf(
        "COM" to "B",
        "B" to "C",
        "C" to "D",
        "D" to "E",
        "E" to "F",
        "B" to "G",
        "G" to "H",
        "D" to "I",
        "E" to "J",
        "J" to "K",
        "K" to "L"
    )

    assertEquals(listOf(0), chart.countAncestors(listOf("COM")))
    assertEquals(listOf(3), chart.countAncestors(listOf("D")))
    assertEquals(listOf(7), chart.countAncestors(listOf("L")))
    assertEquals(listOf(0, 3, 7), chart.countAncestors(listOf("COM", "D", "L")))

    val parentsByChild = chart.associateBy({ it.second }, { it.first })
    assertEquals(listOf("COM"), parentsByChild.ancestorsOf("COM"))
    assertEquals(listOf("COM", "B", "C", "D"), parentsByChild.ancestorsOf("D"))
    assertEquals(listOf("COM", "B", "C", "D", "E", "J", "K", "L"), parentsByChild.ancestorsOf("L"))


    val chartWithSanta = chart + listOf("K" to "YOU", "I" to "SAN")

    val parentsByChildWithSanta = chartWithSanta.associateBy({ it.second }, { it.first })
    assertEquals("D", parentsByChildWithSanta.closestCommonAncestorOf("YOU", "SAN"))

    assertEquals(4, chartWithSanta.countTransfersBetween("YOU", "SAN") - 2)
}

