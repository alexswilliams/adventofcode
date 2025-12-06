package aoc2022.day20

import common.*
import kotlin.math.*

private val example = loadFilesToLines("aoc2022/day20", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2022/day20", "input.txt").single()

internal fun main() {
    Day20.assertCorrect()
    benchmark(100) { part1(puzzle) } // 7.4ms
    benchmark(10) { part2(puzzle) } // 102.8ms
}

internal object Day20 : Challenge {
    override fun assertCorrect() {
        check(3, "P1 Example") { part1(example) }
        check(4151, "P1 Puzzle") { part1(puzzle) }

        check(1623178306, "P2 Example") { part2(example) }
        check(7848878698663, "P2 Puzzle") { part2(puzzle) }
    }
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
