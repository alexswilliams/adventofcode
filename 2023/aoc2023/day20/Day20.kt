package aoc2023.day20

import aoc2023.day20.Module.FlipFlop.State.*
import aoc2023.day20.PulseType.*
import common.*

private val examples = loadFilesToLines("aoc2023/day20", "example1.txt", "example2.txt")
private val puzzle = loadFilesToLines("aoc2023/day20", "input.txt").single()

internal fun main() {
    Day20.assertCorrect()
    benchmark(100) { part1(puzzle) } // 4.5ms
    benchmark(10) { part2(puzzle) } // 48.2ms
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
    val modules: Map<String, Module> = parseModules(input)
    var hiSignals = 0
    var loSignals = 0
    repeat(1000) {
        pushButton(modules) { _, _, pulseType ->
            when (pulseType) {
                HI -> hiSignals++
                LO -> loSignals++
            }
        }
    }
    return hiSignals * loSignals
}

private fun part2(input: List<String>): Long {
    val modules: Map<String, Module> = parseModules(input)
    // observation: rx is fed by only &dh, which is fed by only &tr, &xm, &dr and &nh
    return listOf("dr", "nh", "tr", "xm").map { source ->
        modules.forEach { it.value.reinitialise() }
        pushesUntil(modules) { from, pulseType -> from == source && pulseType == HI }
    }.let { lcm(it) }
}

private fun pushesUntil(modules: Map<String, Module>, predicate: (String, PulseType) -> Boolean): Long {
    var pushes = 0L
    var found = false
    while (!found) {
        pushes++
        pushButton(modules) { from, target, pulseType -> if (predicate(from, pulseType)) found = true }
    }
    return pushes
}


private data class Work(val from: String, val module: String, val type: PulseType)

private fun pushButton(modules: Map<String, Module>, onPulse: (String, String, PulseType) -> Unit) {
    val work = ArrayDeque<Work>().apply { addFirst(Work("", "button", LO)) }
    while (work.isNotEmpty()) {
        val u = work.removeFirst()
        val module = modules[u.module] ?: Module.Output(u.module)
        module.receivePulse(u.from, u.type) { target, pulseType ->
            onPulse(module.name, target, pulseType)
            work.addLast(Work(module.name, target, pulseType))
        }
    }
}

private fun parseModules(input: List<String>): Map<String, Module> =
    input.map { line ->
        val (lhs, rhs) = line.split(" -> ")
        val name = if (lhs[0] == '%' || lhs[0] == '&') lhs.substring(1) else lhs
        val destinations = rhs.split(", ")
        when {
            lhs[0] == '%' -> Module.FlipFlop(name, destinations)
            lhs[0] == '&' -> Module.Conjunction(name, destinations)
            lhs == "broadcaster" -> Module.Broadcaster(destinations)
            else -> error("unknown module type for $line")
        }
    }.associateBy { it.name }.plus(listOf<Pair<String, Module.Button>>("button" to Module.Button())).also { modules ->
        modules.values.filterIsInstance<Module.Conjunction>()
            .forEach { it.initInputs(modules.values.filter { source -> it.name in source.destinations }.map { it.name }) }
    }


private enum class PulseType { HI, LO }

private sealed class Module(
    val name: String,
    val destinations: List<String>,
) {
    abstract fun receivePulse(from: String, type: PulseType, emit: (target: String, type: PulseType) -> Unit)
    abstract fun reinitialise()
    override fun toString(): String = "${this::class.simpleName}($name -> $destinations)"

    class FlipFlop(name: String, destinations: List<String>) : Module(name, destinations) {
        private enum class State { ON, OFF }

        private var state = OFF
        override fun toString(): String = "${this::class.simpleName}($name ($state) -> $destinations)"
        override fun receivePulse(from: String, type: PulseType, emit: (String, PulseType) -> Unit) {
            if (type == HI) return
            state = if (state == ON) OFF else ON
            when (state) {
                OFF -> destinations.forEach { d -> emit(d, LO) }
                ON -> destinations.forEach { d -> emit(d, HI) }
            }
        }

        override fun reinitialise() {
            state = OFF
        }
    }

    class Conjunction(name: String, destinations: List<String>) : Module(name, destinations) {
        val inputPulses = mutableMapOf<String, PulseType>()
        fun initInputs(names: List<String>) = names.forEach { inputPulses[it] = LO }
        override fun toString(): String = "${this::class.simpleName}($name ($inputPulses) -> $destinations)"
        override fun receivePulse(from: String, type: PulseType, emit: (String, PulseType) -> Unit) {
            inputPulses[from] = type
            if (inputPulses.all { it.value == HI })
                destinations.forEach { d -> emit(d, LO) }
            else destinations.forEach { d -> emit(d, HI) }
        }

        override fun reinitialise() {
            inputPulses.keys.forEach { inputPulses[it] = LO }
        }
    }

    class Button() : Module("button", listOf("broadcaster")) {
        override fun reinitialise() {}
        override fun receivePulse(from: String, type: PulseType, emit: (String, PulseType) -> Unit) =
            emit("broadcaster", LO)
    }

    class Broadcaster(destinations: List<String>) : Module("broadcaster", destinations) {
        override fun reinitialise() {}
        override fun receivePulse(from: String, type: PulseType, emit: (String, PulseType) -> Unit) =
            destinations.forEach { d -> emit(d, type) }
    }

    class Output(name: String) : Module(name, listOf()) {
        override fun receivePulse(from: String, type: PulseType, emit: (String, PulseType) -> Unit) {}
        override fun reinitialise() {}
    }
}
