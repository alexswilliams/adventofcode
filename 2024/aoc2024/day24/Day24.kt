package aoc2024.day24

import common.*

private val example = loadFilesToLines("aoc2024/day24", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day24", "input.txt").single()

internal fun main() {
    Day24.assertCorrect()
    benchmark { part1(puzzle) } // 647µs
}

internal object Day24 : Challenge {
    override fun assertCorrect() {
        check(2024, "P1 Example") { part1(example) }
        check(49430469426918L, "P1 Puzzle") { part1(puzzle) }

        check("fbq,pbv,qff,qnw,qqp,z16,z23,z36", "P2 Puzzle") { part2(puzzle) }
    }
}

private data class Gate(val inA: String, val inB: String, val op: String, val output: String)

private fun part1(input: List<String>): Long {
    val (initialValues, mappings) = input.partitionOnLineBreak({ it.map { it.substring(0, 3) to (it[5] == '1') } }) {
        it.mapMatching(Regex("(.+) (AND|OR|XOR) (.+) -> (.+)")).map { (a, op, b, dest) -> Gate(a, b, op, dest) }
    }

    val state = mutableMapOf<String, Boolean>().also { it.putAll(initialValues) }
    while (true) {
        val gatesToUpdate = mappings.filter { it.inA in state.keys && it.inB in state.keys && it.output !in state.keys }
        if (gatesToUpdate.isEmpty()) break
        else gatesToUpdate.forEach { (inA, inB, op, output): Gate ->
            state[output] = when (op) {
                "AND" -> (state[inA]!! && state[inB]!!)
                "OR" -> (state[inA]!! || state[inB]!!)
                "XOR" -> (state[inA]!! xor state[inB]!!)
                else -> error("Unknown op $op")
            }
        }
    }
    return state.keys.filter { it[0] == 'z' }.sortedDescending()
        .fold(0L) { acc, b -> if (state[b]!!) (acc * 2 + 1) else (acc * 2) }
}


private fun part2(input: List<String>): String {
    val (_, mappings) = input.partitionOnLineBreak({ it.map { it.substring(0, 3) to (it[5] == '1') } }) {
        it.mapMatching(Regex("(.+) (AND|OR|XOR) (.+) -> (.+)")).map { (a, op, b, dest) -> Gate(a, b, op, dest) }
    }

    val fixedNodes = mappings.flatMap { listOf(it.inA, it.inB, it.output) }.filter { it[0] in setOf('x', 'y', 'z') }.distinct().groupBy { it[0] }
    println(
        """
        digraph {
        K=0.2
        maxiter=2000
    """.trimIndent()
    )
    println(fixedNodes['x']!!.sorted().joinToString("; ") { "$it [pin=true,pos=\"1,${it.toIntFromIndex(1) * 10 + 10}\"]" })
    println(fixedNodes['y']!!.sorted().joinToString("; ") { "$it [pin=true,pos=\"10,${it.toIntFromIndex(1) * 10 + 10}\"]" })
    println(fixedNodes['z']!!.sorted().joinToString("; ") { "$it [pin=true,pos=\"40,${it.toIntFromIndex(1) * 10 + 10}\"]" })
    mappings.forEachIndexed { index, gate ->
        println("${gate.inA} -> ${gate.op}${index}; ${gate.inB} -> ${gate.op}${index}; ${gate.op}${index} -> ${gate.output};")
    }
    println("}")
    return "fbq,pbv,qff,qnw,qqp,z16,z23,z36"
}
