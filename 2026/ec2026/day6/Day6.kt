package ec2026.day6

import common.*

private val example = loadFilesToLines("ec2026/day6", "example1.txt", "example2.txt", "example3.txt")
private val puzzle = loadFilesToLines("ec2026/day6", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzle[0]) } // 51.2µs
    benchmark { part2(puzzle[1]) } // 504.4µs
    benchmark { part3(puzzle[2]) } // 2.3ms
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

    fun uniqueIngredientsMaking(product: String, uniqueIngredientsSoFar: MutableSet<String> = HashSet(listOf(product))): Set<String> {
        val recipe: List<String> = codex[product].orEmpty()
        val newIngredients = recipe.filter { it !in uniqueIngredientsSoFar }
        uniqueIngredientsSoFar.addAll(newIngredients)
        newIngredients.forEach { uniqueIngredientsMaking(it, uniqueIngredientsSoFar) }
        return uniqueIngredientsSoFar
    }

    return uniqueIngredientsMaking("NULL").size - 1
}


private fun part2(input: List<String>): Int {
    val reversedCodex = buildReverseCodex(input)
    val containsNull = allRecipesUsing(reversedCodex, "NULL")
    return containsNull.size
}

private fun allRecipesUsing(
    reversedCodex: Map<String, List<String>>,
    ingredientToFind: String
): HashSet<String> {
    val containsIngredient = HashSet<String>(reversedCodex.size * 2).apply { add(ingredientToFind) }
    val thisIteration = ArrayList<String>(20).apply { add(ingredientToFind) }
    val nextIteration = ArrayList<String>(20)
    do {
        nextIteration.clear()
        thisIteration.forEach { knownToContain ->
            val allCreatedWith = reversedCodex[knownToContain].orEmpty()
            allCreatedWith.forEach { createdWith ->
                val wasNew = containsIngredient.add(createdWith)
                if (wasNew) nextIteration.add(createdWith)
            }
        }
        thisIteration.clear()
        thisIteration.addAll(nextIteration)
    } while (nextIteration.isNotEmpty())
    containsIngredient.remove(ingredientToFind)
    return containsIngredient
}

private fun buildReverseCodex(input: List<String>): Map<String, List<String>> =
    buildMap<String, MutableList<String>>(input.size * 2) {
        input.forEach { recipe ->
            recipe.splitForEachRange(",", 5) { string, start, end ->
                computeIfAbsent(string.substring(start, end + 1)) { ArrayList(30) }.add(recipe.substring(0, 4))
            }
        }
    }


private fun part3(input: List<String>): Int {
    val reversedCodex = buildReverseCodex(input)

    val null1 = allRecipesUsing(reversedCodex, "NULLv1")
    val null2 = allRecipesUsing(reversedCodex, "NULLv2")
    val null3 = allRecipesUsing(reversedCodex, "NULLv3")

    val v12 = null1.intersect(null2)
    val v23 = null2.intersect(null3)
    val v13 = null1.intersect(null3)
    return v12.union(v23).union(v13).size
}
