package aoc2022.day20

import aoc2022.common.*
import kotlin.math.*
import kotlin.test.*

private val exampleInput = "aoc2022/day20/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day20/input.txt".fromClasspathFileToLines()
private const val PART_1_EXPECTED_EXAMPLE_ANSWER = 3L
private const val PART_2_EXPECTED_EXAMPLE_ANSWER = 1623178306L

fun main() {
    assertEquals(PART_1_EXPECTED_EXAMPLE_ANSWER, part1(exampleInput))
    part1(puzzleInput).also { println("Part 1: $it") } // 4151, took 12.73ms

    assertEquals(PART_2_EXPECTED_EXAMPLE_ANSWER, part2(exampleInput))
    part2(puzzleInput).also { println("Part 2: $it") } // 7848878698663, took 186.60ms
}

private fun part1(input: List<String>): Long {
    val initial = input.map { it.toLong() }
    val soFar = initial.withIndex().toMutableList()
    rotateList(initial, soFar)
    val indexOf0 = soFar.indexOfFirst { it.value == 0L }
    return soFar[(indexOf0 + 1000) % soFar.size].value + soFar[(indexOf0 + 2000) % soFar.size].value + soFar[(indexOf0 + 3000) % soFar.size].value
}

private fun part2(input: List<String>): Long {
    val initial = input.map { it.toLong() * 811589153L }
    val soFar = initial.withIndex().toMutableList()
    repeat(10) { rotateList(initial, soFar) }
    val indexOf0 = soFar.indexOfFirst { it.value == 0L }
    return soFar[(indexOf0 + 1000) % soFar.size].value + soFar[(indexOf0 + 2000) % soFar.size].value + soFar[(indexOf0 + 3000) % soFar.size].value
}

private fun rotateList(
    initial: List<Long>,
    soFar: MutableList<IndexedValue<Long>>
) {
    val modulus = initial.size - 1
    initial.indices.forEach { initialIndex ->
        val currentIndex = soFar.indexOfFirst { it.index == initialIndex }
        val item = soFar[currentIndex]
        val newIndex = ((currentIndex + item.value + modulus * item.value.absoluteValue) % modulus).toInt()
        soFar.shift(currentIndex, newIndex)
    }
}

private fun <T> MutableList<T>.shift(currentIndex: Int, newIndex: Int) {
    if (currentIndex == newIndex) return
    val item = removeAt(currentIndex)
    add(newIndex, item)
}
