package ec2025.day7

import common.*

private val examples = loadFilesToLines("ec2025/day7", "example1.txt", "example2.txt", "example3a.txt", "example3b.txt")
private val puzzles = loadFilesToLines("ec2025/day7", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day7.assertCorrect()
    benchmark { part1(puzzles[0]) } // 16.9µs
    benchmark { part2(puzzles[1]) } // 36.6µs
    benchmark { part3(puzzles[2]) } // 30.1µs
}

internal object Day7 : Challenge {
    override fun assertCorrect() {
        check("Oroneth", "P1 Example") { part1(examples[0]) }
        check("Nythyris", "P1 Puzzle") { part1(puzzles[0]) }

        check(23, "P2 Example") { part2(examples[1]) }
        check(1939, "P2 Puzzle") { part2(puzzles[1]) }

        check(25, "P3 Example A") { part3(examples[2]) }
        check(1154, "P3 Example B") { part3(examples[3]) }
        check(9745208, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String =
    parseInput(input).let { (names, rules) -> names.first { name -> isValidName(name, rules) } }

private fun part2(input: List<String>): Int =
    parseInput(input).let { (names, rules) -> names.sumOfIndexed { index, name -> if (isValidName(name, rules)) index + 1 else 0 } }

private fun part3(input: List<String>): Int {
    val (names, rules) = parseInput(input)
    val validPrefixes = names.filter { name -> isValidName(name, rules) }
    val dedupedPrefixes = validPrefixes.filterNot { longerName -> validPrefixes.any { prefix -> longerName != prefix && longerName.startsWith(prefix) } }

    val cache = IntArray(1024)
    fun namesBeneath(char: Char, length: Int): Int {
        cache[key(char, length)].let { if (it > 0) return it }
        val matchesAtThisLength = if (length in 7..11) 1 else 0
        return matchesAtThisLength +
                if (length < 11 && char in rules)
                    rules[char]!!.sumOf { nextChar ->
                        namesBeneath(nextChar, length + 1)
                            .also { cache[key(nextChar, length + 1)] = it }
                    }
                else 0
    }

    return dedupedPrefixes.sumOf { namesBeneath(it.last(), it.length) }
}


private fun parseInput(input: List<String>): Pair<List<String>, Map<Char, Collection<Char>>> =
    Pair(
        input[0].split(','),
        input.subList(2, input.size).associate { rule ->
            rule[0] to rule.splitMappingRanges(",", 4) { string, start, _ -> string[start] }
        }
    )

private fun isValidName(name: String, rules: Map<Char, Collection<Char>>): Boolean =
    name.zipWithNext().all { (a, b) -> a in rules && b in rules[a]!! }

private fun key(char: Char, length: Int): Int =
    (char.code and 0x3f) or (length and 0x0f shl 6)
