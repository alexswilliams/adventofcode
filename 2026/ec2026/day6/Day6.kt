package ec2026.day6

import common.Challenge
import common.benchmark
import common.countTrue
import common.loadFilesToLines

private val example = loadFilesToLines("ec2026/day6", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToLines("ec2026/day6", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzle[0]) } // 54.0µs
    benchmark { part2(puzzle[1]) } // 2.7ms
    benchmark(100) { part3(puzzle[2]) } // 22.3ms
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(7, "P1 Example") { part1(example[0]) }
        check(49, "P1 Puzzle") { part1(puzzle[0]) }

        check(6, "P2 Example") { part2(example[1]) }
        check(823, "P2 Puzzle") { part2(puzzle[1]) }

        check(3, "P3 Example") { part3(example[2]) }
        check(1649, "P3 Puzzle") { part3(puzzle[2]) }
    }
}


private fun part1(input: List<String>): Int {
    val codex = input.associateBy({ it.substring(0, 4) }) { it.substring(5).split(',') }

    fun uniqueIngredientsMaking(ingredient: String, uniqueIngredientsSoFar: MutableSet<String> = HashSet()): Set<String> {
        val directIngredients: List<String> = codex[ingredient].orEmpty()
        val newIngredients = directIngredients.filter { it !in uniqueIngredientsSoFar }
        uniqueIngredientsSoFar.addAll(newIngredients)
        newIngredients.forEach { uniqueIngredientsMaking(it, uniqueIngredientsSoFar) }
        return uniqueIngredientsSoFar
    }

    return uniqueIngredientsMaking("NULL").minus("NULL").size
}


private fun part2(input: List<String>): Int {
    val codex = input.associateBy({ it.substring(0, 4) }) { it.substring(5).split(',') }

    fun containsNull(product: String, path: ArrayList<String> = arrayListOf(product)): Boolean {
        val recipe = codex[product].orEmpty()
        return recipe.contains("NULL") || recipe.any {
            if (path.contains(it)) return@any false
            path.add(it)
            containsNull(it, path)
                .also { path.removeLast() }
        }
    }

    return codex.keys.minus("NULL").count { topLevelProduct ->
        containsNull(topLevelProduct)
    }

}


private fun part3(input: List<String>): Int {
    val codex = input.associateBy({ it.substring(0, 4) }) { it.substring(5).split(',') }

    fun containsMultipleNulls(product: String, nullsSeen: BooleanArray = BooleanArray(3), path: ArrayList<String> = arrayListOf(product)): Boolean {
        val recipe = codex[product].orEmpty()
        if (product.startsWith("NULL")) {
            val nullType = product.last().digitToInt()
            nullsSeen[nullType - 1] = true
            if (nullsSeen.countTrue() >= 2) return true
        }
        return recipe.any {
            if (path.contains(it)) return@any false
            path.add(it)
            containsMultipleNulls(it, nullsSeen, path)
                .also { path.removeLast() }
        }
    }

    return codex.keys.count { topLevelProduct ->
        containsMultipleNulls(topLevelProduct)
    }

}
