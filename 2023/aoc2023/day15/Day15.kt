package aoc2023.day15

import common.*
import kotlin.test.*


private val exampleInput = "aoc2023/day15/example.txt".fromClasspathFile()
private val puzzleInput = "aoc2023/day15/input.txt".fromClasspathFile()

internal fun main() {
    Day15.assertPart1Correct()
    Day15.assertPart2Correct()
    benchmark { part1(puzzleInput) } // 99µs
    benchmark { part2(puzzleInput) } // 268µs
}

internal object Day15 : TwoPartChallenge {
    override fun assertPart1Correct() {
        part1(exampleInput).also { println("[Example] Part 1: $it") }.also { assertEquals(1320, it) }
        part1(puzzleInput).also { println("[Puzzle] Part 1: $it") }.also { assertEquals(517965, it) }
    }

    override fun assertPart2Correct() {
        part2(exampleInput).also { println("[Example] Part 2: $it") }.also { assertEquals(145, it) }
        part2(puzzleInput).also { println("[Puzzle] Part 2: $it") }.also { assertEquals(267372, it) }
    }
}

private fun part1(input: String) = input.split(',').sumOf { s -> hash(s) }

private fun part2(input: String): Int {
    data class Lens(val label: String, val length: Int)

    val boxes = Array<MutableList<Lens>>(256) { arrayListOf() }
    input.split(',').forEach { s ->
        val idxEquals = s.indexOf('=')
        val label = if (idxEquals == -1) s.dropLast(1) else s.take(idxEquals)
        val box = boxes[hash(label)]
        val idxInBox = box.indexOfFirst { it.label == label }
        if (idxEquals == -1) {
            if (idxInBox >= 0)
                box.removeAt(idxInBox)
        } else {
            val lens = Lens(label, s.toIntFromIndex(idxEquals + 1))
            if (idxInBox >= 0)
                box[idxInBox] = lens
            else
                box.add(lens)
        }
    }
    return boxes.asIterable().sumOfIndexed { boxIndex: Int, lenses: MutableList<Lens> ->
        (boxIndex + 1) * lenses.sumOfIndexed { listIndex: Int, lens: Lens -> (listIndex + 1) * lens.length }
    }
}

private fun hash(s: String) = s.fold(0) { currentValue, c -> (17 * (currentValue + c.code)) and 0xff }