package common

class TreeQueue<Element>(initialValues: Iterable<Pair<Element, Int>>, private val weightOffset: (Element) -> Int = { 0 }) : PriorityHeap<Element> {
    // Thoughts on why this might be faster:
    // - it's not a tree of nodes, it's a tree of weights, and many nodes share the same weight
    // - for a grid, it's probably log(k) to delete-min, rather than log(n), where k is height + width, so probably O(log(n^1/2)) ish
    private val tree = MinTreeMap<Int, MutableList<Element>>()

    init {
        initialValues.forEach { offer(it.first, it.second) }
    }

    constructor(weightOffset: (Element) -> Int = { 0 }) : this(emptyList(), weightOffset)
    constructor(initialValue: Pair<Element, Int>, weightOffset: (Element) -> Int = { 0 }) : this(listOf(initialValue), weightOffset)

    override fun offer(e: Element, weight: Int) {
        tree.computeIfAbsent(weight + weightOffset(e)) { arrayListOf() }!!.add(e)
    }

    override fun poll(): Element? {
        val minEntry = tree.firstEntry() ?: return null
        if (minEntry.value.size == 1) {
            tree.deleteEntry(minEntry)
            return minEntry.value[0]
        }
        return minEntry.value.removeLast()
    }

    private data class Entry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>

    override fun pollEntry(): Map.Entry<Int, Element>? {
        val minEntry = tree.firstEntry() ?: return null
        if (minEntry.value.size == 1) {
            tree.deleteEntry(minEntry)
            return Entry(minEntry.key, minEntry.value[0])
        }
        return Entry(minEntry.key, minEntry.value.removeLast())
    }

    override fun offerOrReposition(e: Element, oldWeight: Int, newWeight: Int) {
        val offset = weightOffset(e)
        val weight = oldWeight + offset
        if (newWeight + offset >= weight) throw UnsupportedOperationException("TreeQueue experienced an increase in key during decrease-key: ${newWeight + offset} ($newWeight), $weight ($oldWeight)")
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
