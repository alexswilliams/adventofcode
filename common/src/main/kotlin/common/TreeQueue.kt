package common

import java.util.*

class TreeQueue<Element>(private val weightOffset: (Element) -> Int = { 0 }) {
    private val tree = TreeMap<Int, MutableList<Element>>()

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

    fun offerOrReposition(e: Element, oldWeight: Int, newWeight: Int) {
        val weight = oldWeight + weightOffset(e)
        tree[weight]?.apply { if (remove(e) && isEmpty()) tree.remove(weight) }
        offer(e, newWeight)
    }
}
