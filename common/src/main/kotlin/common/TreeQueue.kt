package common

import kotlin.contracts.*

class TreeQueue<Element>(initialValues: Iterable<Pair<Element, Int>>, private val weightOffset: (Element) -> Int = { 0 }) {
    // Thoughts on why this might be faster:
    // - it's not a tree of nodes, it's a tree of weights, and many nodes share the same weight
    // - for a grid, it's probably log(k) to delete-min, rather than log(n), where k is height + width, so probably O(log(n^1/2)) ish
    private val tree = MinTreeMap<Int, MutableList<Element>>()

    init {
        initialValues.forEach { offer(it.first, it.second) }
    }

    constructor(weightOffset: (Element) -> Int = { 0 }) : this(emptyList(), weightOffset)
    constructor(initialValue: Pair<Element, Int>, weightOffset: (Element) -> Int = { 0 }) : this(listOf(initialValue), weightOffset)

    fun offer(e: Element, weight: Int) {
        tree.computeIfAbsent(weight + weightOffset(e)) { arrayListOf() }!!.add(e)
    }

    fun poll(): Element? {
        val minEntry = tree.firstEntry() ?: return null
        if (minEntry.value.size == 1) {
            tree.deleteEntry(minEntry)
            return minEntry.value[0]
        }
        return minEntry.value.removeLast()
    }

    @OptIn(ExperimentalContracts::class)
    fun poll(exfiltrateWeight: (Int) -> Unit): Element? {
        contract { callsInPlace(exfiltrateWeight, InvocationKind.EXACTLY_ONCE) }

        val minEntry = tree.firstEntry()
        if (minEntry == null) {
            exfiltrateWeight(-1)
            return null
        }
        exfiltrateWeight(minEntry.key)
        if (minEntry.value.size == 1) {
            tree.deleteEntry(minEntry)
            return minEntry.value[0]
        }
        return minEntry.value.removeLast()
    }

    fun offerOrReposition(e: Element, oldWeight: Int, newWeight: Int) {
        val weight = oldWeight + weightOffset(e)
        val entry = tree.getEntry(weight)
        if (entry != null) {
            if (entry.value.size == 1)
                tree.deleteEntry(entry)
            else
                entry.value.remove(e)
        }
        offer(e, newWeight)
    }
}
