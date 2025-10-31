package aoc2024.day24

import common.*

private val example = loadFilesToLines("aoc2024/day24", "example.txt").single()
private val puzzle = loadFilesToLines("aoc2024/day24", "input.txt").single()

internal fun main() {
    Day24.assertCorrect()
    benchmark { part1(puzzle) } // 586µs
    benchmark { part2(puzzle) } // 179µs
}

internal object Day24 : Challenge {
    override fun assertCorrect() {
        check(2024, "P1 Example") { part1(example) }
        check(49430469426918L, "P1 Puzzle") { part1(puzzle) }

        check("fbq,pbv,qff,qnw,qqp,z16,z23,z36", "P2 Puzzle") { part2(puzzle) }
    }
}

private enum class Op { AND, OR, XOR }
private data class Gate(val inA: String, val inB: String, val op: Op, val output: String)

private fun part1(input: List<String>): Long {
    val (initialValues, mappings) = parseInput(input)
    val state = mutableMapOf<String, Boolean>().also { it.putAll(initialValues) }
    while (true) {
        val gatesToUpdate = mappings.filter { it.inA in state.keys && it.inB in state.keys && it.output !in state.keys }
        if (gatesToUpdate.isEmpty()) break
        else gatesToUpdate.forEach { (inA, inB, op, output): Gate ->
            state[output] = when (op) {
                Op.AND -> (state[inA]!! && state[inB]!!)
                Op.OR -> (state[inA]!! || state[inB]!!)
                Op.XOR -> (state[inA]!! xor state[inB]!!)
            }
        }
    }
    return state.keys.filter { it[0] == 'z' }.sortedDescending()
        .fold(0L) { acc, b -> if (state[b]!!) (acc * 2 + 1) else (acc * 2) }
}


private fun part2(input: List<String>): String {
    val (_, mappings) = parseInput(input)
    // renderGraph(mappings)

    val byInputs = (mappings.map { it.inA to it } + mappings.map { it.inB to it }).groupBy({ it.first }) { it.second }
    val byOutputs = mappings.associateBy { it.output }

    return buildList {
        val finalZ = mappings.asSequence().filter { it.output[0] == 'z' }.maxBy { it.output.toIntFromIndex(1) }.output
        // every z output should be fed by a XOR (except the last which is probably fed by an OR)
        val zFedByIncorrectGate = mappings
            .filter { it.output[0] == 'z' && it.output != finalZ }
            .filter { it.op != Op.XOR }
            .map { it.output }
        addAll(zFedByIncorrectGate)

        // internal XOR gates should only ever point at AND gates and XOR gates, never OR gates
        val outputsOfNonZXorGates = mappings.filter { it.op == Op.XOR && it.output[0] != 'z' }.map { it.output }
        val badlyPointingXorOutputs = outputsOfNonZXorGates
            .filterNot { it in byInputs && byInputs[it]!!.any { gate -> gate.op == Op.AND } && byInputs[it]!!.any { gate -> gate.op == Op.XOR } }
        addAll(badlyPointingXorOutputs)

        // Output XOR gates should only ever be fed by input XOR gates (plus AND gates)
        val zXorGates = mappings.filter { it.op == Op.XOR && it.output[0] == 'z' }
        val badInternalXorInputs = zXorGates.flatMap { listOf(it.inA, it.inB) }.filter { it in byOutputs && byOutputs[it]!!.op == Op.XOR }
            .filterNot {
                val gate = byOutputs[it]!!
                (gate.inA[0] == 'x' || gate.inA[0] == 'y') && (gate.inB[0] == 'x' || gate.inB[0] == 'y')
            }
        addAll(badInternalXorInputs)

        // Output XOR gates should be fed by only XOR and OR gates, never AND (except for the second bit which has no carry to include)
        val badOutputXorInputs = zXorGates.flatMap { listOf(it.inA, it.inB) }.filter { it in byOutputs && byOutputs[it]!!.op == Op.AND }
            .filterNot { (byOutputs[it]!!.inA == "x00" || byOutputs[it]!!.inA == "y00") }
        addAll(badOutputXorInputs)
    }.distinct().sorted().joinToString(",")
}

private fun parseInput(input: List<String>) =
    input.partitionOnLineBreak(
        { line -> line.map { it.substring(0, 3) to (it[5] == '1') } })
    { it.mapMatching(Regex("(.+) (AND|OR|XOR) (.+) -> (.+)")).map { (a, op, b, dest) -> Gate(a, b, Op.valueOf(op), dest) } }


@Suppress("unused")
private fun renderGraph(mappings: List<Gate>) {
    val fixedNodes = mappings.flatMap { listOf(it.inA, it.inB, it.output) }.filter { it[0] in setOf('x', 'y', 'z') }.distinct().groupBy { it[0] }
    fun colourForNode(op: Op): String = when (op) {
        Op.AND -> "deeppink"
        Op.OR -> "lawngreen"
        Op.XOR -> "lightblue"
    }
    println(
        """
        digraph {
        K=0.5
    """.trimIndent()
    )
    println(fixedNodes['x']!!.sorted().joinToString("; ") { "$it [pin=true,pos=\"1,${it.toIntFromIndex(1) * 10 + 10}\"]" })
    println(fixedNodes['y']!!.sorted().joinToString("; ") { "$it [pin=true,pos=\"10,${it.toIntFromIndex(1) * 10 + 10}\"]" })
    println(fixedNodes['z']!!.sorted().joinToString("; ") { "$it [pin=true,pos=\"40,${it.toIntFromIndex(1) * 10 + 10}\"]" })
    mappings.forEachIndexed { index, gate ->
        println("${gate.op}${index} [style=filled,fillcolor=${colourForNode(gate.op)}]")
        println("${gate.inA} -> ${gate.op}${index}; ${gate.inB} -> ${gate.op}${index}; ${gate.op}${index} -> ${gate.output};")
    }
    println("}")
}

