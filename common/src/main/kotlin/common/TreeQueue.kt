package common

import java.util.*
import kotlin.contracts.*

class TreeQueue<Element>(initialValues: Iterable<Pair<Element, Int>>, private val weightOffset: (Element) -> Int = { 0 }) {
    private val tree = TreeMap<Int, MutableList<Element>>()

    init {
        initialValues.forEach { offer(it.first, it.second) }
    }

    constructor(weightOffset: (Element) -> Int = { 0 }) : this(emptyList(), weightOffset)
    constructor(initialValue: Pair<Element, Int>, weightOffset: (Element) -> Int = { 0 }) : this(listOf(initialValue), weightOffset)

    fun offer(e: Element, weight: Int) {
        tree.getOrPut(weight + weightOffset(e)) { arrayListOf() }.add(e)
    }

    fun poll(): Element? =
        tree.firstEntry()?.let { (weight, list) ->
            if (list.size == 1) {
                tree.remove(weight)
                list.single()
            } else
                list.removeLast()
        }

    @OptIn(ExperimentalContracts::class)
    fun poll(exfiltrateWeight: (Int) -> Unit): Element? {
        contract { callsInPlace(exfiltrateWeight, InvocationKind.EXACTLY_ONCE) }
        return tree.firstEntry().let { entry ->
            if (entry != null) {
                val (weight, list) = entry
                exfiltrateWeight(weight)
                if (list.size == 1) {
                    tree.remove(weight)
                    list.single()
                } else
                    list.removeLast()
            } else {
                exfiltrateWeight(-1)
                null
            }
        }
    }

    fun offerOrReposition(e: Element, oldWeight: Int, newWeight: Int) {
        val weight = oldWeight + weightOffset(e)
        tree[weight]?.apply { if (remove(e) && isEmpty()) tree.remove(weight) }
        offer(e, newWeight)
    }
}
