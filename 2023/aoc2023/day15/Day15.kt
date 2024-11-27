package aoc2023.day15

import common.*


private val examples = loadFiles("aoc2023/day15", "example.txt")
private val puzzles = loadFiles("aoc2023/day15", "input.txt")

internal fun main() {
    Day15.assertCorrect()
    benchmark { part1(puzzles[0]) } // 99µs
    benchmark { part2(puzzles[0]) } // 268µs
}

internal object Day15 : Challenge {
    override fun assertCorrect() {
        check(1320, "P1 Example") { part1(examples[0]) }
        check(517965, "P1 Puzzle") { part1(puzzles[0]) }

        check(145, "P2 Example") { part2(examples[0]) }
        check(267372, "P2 Puzzle") { part2(puzzles[0]) }
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
