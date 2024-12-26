package aoc2024.day23

import common.*

// cat input.txt | awk -F- 'BEGIN{print "graph {"; print "maxiter=10000"; print "splines=false"; print "K=0.8"; print "node [shape=point]"; print "edge [color=grey,penwidth=0.2]"} {print $1 "--" $2 ";"} END{print "}"}' | fdp -Tsvg > input.svg
private val examples = loadFilesToLines("aoc2024/day23", "example.txt", "example2.txt")
private val puzzle = loadFilesToLines("aoc2024/day23", "input.txt").single()

internal fun main() {
    Day23.assertCorrect()
    benchmark { part1(puzzle) } // 630µs
    benchmark(10) { part2(puzzle) } // 183.6ms :(
}

internal object Day23 : Challenge {
    override fun assertCorrect() {
        check(7, "P1 Example") { part1(examples[0]) }
        check(1046, "P1 Puzzle") { part1(puzzle) }

        check("aa,bb,cc,dd", "P2 Example (Mine)") { part2(examples[1]) }
        check("co,de,ka,ta", "P2 Example") { part2(examples[0]) }
        check("de,id,ke,ls,po,sn,tf,tl,tm,uj,un,xw,yz", "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    val edges = parseNet(input)
    return buildSet {
        val startSet = edges.keys.filter { it / 26 == 't' - 'a' }
        for (potentialChiefPc in startSet) {
            val work = ArrayDeque(edges[potentialChiefPc]!!.map { it to listOf(potentialChiefPc) })
            while (work.isNotEmpty()) {
                val (thisPc, seenSoFar) = work.removeLast()
                val nextSeen = seenSoFar.plus(thisPc)
                edges[thisPc]!!.forEach { node ->
                    if (seenSoFar.size == 2 && node == potentialChiefPc)
                        add(nextSeen.sorted())
                    else if (seenSoFar.size < 2 && node > thisPc && node !in seenSoFar) // looking for a loop, so only go round in one direction
                        work.addLast(node to nextSeen)
                }
            }
        }
    }.size
}

private fun part2(input: List<String>): String {
    val edges = parseNet(input)
    val edgeCache = Array<List<Int>?>(26 * 26) { null }
    edges.entries.forEach { (k, v) -> edgeCache[k] = v }

    var cliques = edges.keys.map { listOf(it) }
    while (cliques.size > 1) {
        cliques = expandCliquesByOneWhereGreater(cliques, edgeCache)
    }
    return cliques.single().joinToString(",") { keyToStr(it) }
}

private fun expandCliquesByOneWhereGreater(cliques: List<List<Int>>, edgeCache: Array<List<Int>?>) =
    cliques.flatMap { nodeSet ->
        nodeSet.map { node -> edgeCache[node]!!.filter { it > node } }
            .intersectAll()
            .map { nodeSet.plusUniqueSorted(it) }
    }.distinct()

private fun parseNet(input: List<String>): Map<Int, List<Int>> =
    buildMap<Int, MutableList<Int>> {
        input.forEach {
            val a = strToKey(it.substring(0, 2))
            val b = strToKey(it.substring(3, 5))
            getOrPut(a) { mutableListOf() }.add(b)
            getOrPut(b) { mutableListOf() }.add(a)
        }
        forEach { (_, u) -> u.sort() }
    }

private fun strToKey(str: String): Int = (str[0] - 'a') * 26 + (str[1] - 'a')
private fun keyToStr(key: Int): String = buildString { append('a' + (key / 26)); append('a' + key % 26) }

// only works when inner lists are sorted
fun <T : Comparable<T>> Iterable<List<T>>.intersectAll(): List<T> =
    minBy { it.size }.filter { elem -> this.all { it.binarySearch(elem) >= 0 } }

