package aoc2023.day20

import aoc2023.day20.Module.*
import aoc2023.day20.Module.FlipFlop.State.*
import aoc2023.day20.PulseType.*
import common.*

private val examples = loadFilesToLines("aoc2023/day20", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("aoc2023/day20", "input.txt").single()

internal fun main() {
    Day20.assertCorrect()
    benchmark { part1(puzzle) } // 940µs
    benchmark(100) { part2(puzzle) } // 11.2ms
}

internal object Day20 : Challenge {
    override fun assertCorrect() {
        check(32000000, "P1 Example 1") { part1(examples[0]) }
        check(11687500, "P1 Example 2") { part1(examples[1]) }
        check(763500168, "P1 Puzzle") { part1(puzzle) }

        check(207652583562007L, "P2 Puzzle") { part2(puzzle) }
    }
}


private fun part1(input: List<String>): Int {
    var hiSignals = 0
    var loSignals = 0
    val onPulse: (Module, PulseType) -> Unit = { _, pulseType -> if (pulseType == HI) hiSignals++ else loSignals++ }
    val modules: Map<String, Module> = parseModules(input)
    repeat(1000) { pushButton(modules, onPulse) }
    return hiSignals * loSignals
}

private fun part2(input: List<String>): Long {
    // observation: rx is fed by only &dh, which is fed by only &tr, &xm, &dr and &nh
    val modules: Map<String, Module> = parseModules(input)
    return listOf("dr", "nh", "tr", "xm")
        .map { modules[it]!! }
        .map { source ->
            modules.forEach { it.value.reinitialise() }
            pushesUntil(modules) { from, pulseType -> pulseType == HI && from == source }
        }.let { lcm(it) }
}


private fun pushesUntil(modules: Map<String, Module>, predicate: (Module, PulseType) -> Boolean): Long {
    var pushes = 0L
    var found = false
    while (!found) {
        pushes++
        pushButton(modules) { from: Module, pulseType: PulseType -> if (predicate(from, pulseType)) found = true }
    }
    return pushes
}

private data class Pulse(val source: Module, val target: Module, val type: PulseType)

private val work = ArrayDeque<Pulse>()
private fun pushButton(modules: Map<String, Module>, onPulse: (newSource: Module, PulseType) -> Unit) {
    work.clear()
    work.add(Pulse(modules["button"]!!, modules["button"]!!, LO))
    val emitter: (Module, Module, PulseType) -> Unit = { newSource, newTarget, pulseType ->
        onPulse(newSource, pulseType)
        if (newTarget !is FlipFlop || pulseType != HI)
            work.addLast(Pulse(newSource, newTarget, pulseType))
    }

    while (work.isNotEmpty()) with(work.removeFirst()) {
        target.receivePulse(source, type, emitter)
    }
}


private fun parseModules(input: List<String>): Map<String, Module> =
    input.mapIndexed { lineNum, line ->
        val (lhs, rhs) = line.split(" -> ")
        val name = if (lhs[0] == '%' || lhs[0] == '&') lhs.substring(1) else lhs
        val destinations = rhs.split(", ")
        when {
            lhs[0] == '%' -> FlipFlop(name, destinations, lineNum)
            lhs[0] == '&' -> Conjunction(name, destinations, lineNum)
            else -> Broadcaster(destinations, lineNum)
        }
    }.plus(listOf(Button(), Output("output", -2), Output("rx", -3)))
        .associateBy { it.name }
        .also { modules ->
            modules.values.forEach { it.populateDestinations(modules) }
            modules.values
                .filterIsInstance<Conjunction>()
                .forEach { it.initInputs(modules.values.filter { source -> it in source.destinations }) }
        }


private enum class PulseType { HI, LO }

private sealed class Module(
    val name: String,
    val destinationNames: List<String>,
    val lineNum: Int,
) {
    lateinit var destinations: Array<Module>
    abstract fun receivePulse(from: Module, type: PulseType, emit: (newSource: Module, newTarget: Module, type: PulseType) -> Unit)
    abstract fun reinitialise()
    fun populateDestinations(modules: Map<String, Module>) {
        destinations = Array(destinationNames.size) { modules[destinationNames[it]]!! }
    }

    class FlipFlop(name: String, destinations: List<String>, lineNum: Int) : Module(name, destinations, lineNum) {
        private enum class State { ON, OFF }

        private var state = OFF
        override fun receivePulse(from: Module, type: PulseType, emit: (Module, Module, PulseType) -> Unit) {
            if (type == HI) return
            state = if (state == ON) OFF else ON
            when (state) {
                OFF -> destinations.forEach { d -> emit(this, d, LO) }
                ON -> destinations.forEach { d -> emit(this, d, HI) }
            }
        }

        override fun reinitialise() {
            state = OFF
        }
    }

    class Conjunction(name: String, destinations: List<String>, lineNum: Int) : Module(name, destinations, lineNum) {
        private lateinit var inputPulsesLevels: BooleanArray
        private val inputModuleLines = mutableListOf<Int>()
        override fun reinitialise() = inputPulsesLevels.indices.forEach { inputPulsesLevels[it] = false }
        fun initInputs(inputs: List<Module>) {
            inputPulsesLevels = BooleanArray(inputs.size) { false } // LO
            inputs.forEach { inputModuleLines.add(it.lineNum) }
        }

        override fun receivePulse(from: Module, type: PulseType, emit: (Module, Module, PulseType) -> Unit) {
            val index = inputModuleLines.indexOf(from.lineNum)
            inputPulsesLevels[index] = type == HI
            if (inputPulsesLevels.all { it })
                destinations.forEach { d -> emit(this, d, LO) }
            else destinations.forEach { d -> emit(this, d, HI) }
        }
    }

    class Button() : Module("button", listOf("broadcaster"), -1) {
        override fun reinitialise() {}
        override fun receivePulse(from: Module, type: PulseType, emit: (Module, Module, PulseType) -> Unit) =
            emit(this, destinations.single(), LO)
    }

    class Broadcaster(destinations: List<String>, lineNum: Int) : Module("broadcaster", destinations, lineNum) {
        override fun reinitialise() {}
        override fun receivePulse(from: Module, type: PulseType, emit: (Module, Module, PulseType) -> Unit) =
            destinations.forEach { d -> emit(this, d, type) }
    }

    class Output(name: String, lineNum: Int) : Module(name, listOf(), lineNum) {
        override fun reinitialise() {}
        override fun receivePulse(from: Module, type: PulseType, emit: (Module, Module, PulseType) -> Unit) {}
    }
}
