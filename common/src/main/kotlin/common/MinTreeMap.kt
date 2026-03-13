/*
 * Copyright likely with Oracle, this is derivative, modifications made under the GNU GPLv2.
 */
package common

import java.util.*
import java.util.function.*
import java.util.function.Function

/**
 * A derivative of the standard library TreeMap which also efficiently tracks the first entry, allowing fast polling of the minimum key.
 */
class MinTreeMap<K : Comparable<K>, V> : AbstractMap<K, V>() {
    private var root: Entry<K, V>? = null
    private var firstEntry: Entry<K, V>? = null

    override var size: Int = 0

    override fun containsKey(key: K): Boolean {
        return getEntry(key) != null
    }

    override fun containsValue(value: V?): Boolean {
        var e = this.firstEntry
        while (e != null) {
            if (valEquals(value, e.value)) return true
            e = successor(e)
        }
        return false
    }

    override fun get(key: K): V? {
        return getEntry(key)?.value
    }

    override fun putAll(map: Map<out K, V>) {
        super.putAll(map)
    }

    internal fun getEntry(key: K): Entry<K, V>? {
        var p = root
        while (p != null) {
            val cmp = key.compareTo(p.key)
            p = when {
                cmp < 0 -> p.left
                cmp > 0 -> p.right
                else -> return p
            }
        }
        return null
    }

    override fun put(key: K, value: V): V? {
        return put(key, value, true)
    }

    override fun putIfAbsent(key: K, value: V): V? {
        return put(key, value, false)
    }

    override fun computeIfAbsent(key: K, mappingFunction: Function<in K, out V?>): V? {
        val newValue: V?
        var t = root
        if (t == null) {
            newValue = mappingFunction.apply(key)
            if (newValue != null) addEntryToEmptyMap(key, newValue)
            return newValue
        }
        var cmp: Int
        var parent: Entry<K, V>?
        do {
            parent = t!!
            cmp = key.compareTo(t.key)
            when {
                cmp < 0 -> t = t.left
                cmp > 0 -> t = t.right
                else -> {
                    if (t.value == null) mappingFunction.apply(key)
                        ?.also { t.value = it }
                    return t.value
                }
            }
        } while (t != null)
        return mappingFunction.apply(key)
            ?.also { addEntry(key, it, parent, cmp < 0) }
    }

    override fun computeIfPresent(key: K, remappingFunction: BiFunction<in K, in V & Any, out V?>): V? {
        throw UnsupportedOperationException()
    }

    override fun compute(key: K, remappingFunction: BiFunction<in K, in V?, out V>): V? {
        throw UnsupportedOperationException()
    }

    override fun merge(key: K, value: V & Any, remappingFunction: BiFunction<in V & Any, in V & Any, out V>): V? {
        throw UnsupportedOperationException()
    }

    private fun addEntry(key: K, value: V, parent: Entry<K, V>, addToLeft: Boolean) {
        val e = Entry(key, value, parent)
        if (addToLeft) parent.left = e else parent.right = e
        if (key < firstEntry!!.key) firstEntry = e
        fixAfterInsertion(e)
        size++
    }

    private fun addEntryToEmptyMap(key: K, value: V) {
        root = Entry(key, value, null)
        firstEntry = root
        size = 1
    }

    private fun put(key: K, value: V, replaceOld: Boolean): V? {
        var t = root
        if (t == null) {
            addEntryToEmptyMap(key, value)
            return null
        }
        var cmp: Int
        var parent: Entry<K, V>?
        do {
            parent = t
            cmp = key.compareTo(t!!.key)
            when {
                cmp < 0 -> t = t.left
                cmp > 0 -> t = t.right
                else -> {
                    val oldValue = t.value
                    if (replaceOld || oldValue == null) t.value = value
                    return oldValue
                }
            }
        } while (t != null)
        addEntry(key, value, parent, cmp < 0)
        return null
    }

    override fun remove(key: K): V? {
        val p = getEntry(key) ?: return null
        return (p.value).also { deleteEntry(p) }
    }

    override fun clear() {
        size = 0
        root = null
    }

    public override fun clone(): Any {
        throw UnsupportedOperationException()
    }

    override fun replace(key: K, oldValue: V, newValue: V): Boolean {
        val p = getEntry(key)
        if (p != null && oldValue == p.value) {
            p.value = newValue
            return true
        }
        return false
    }

    override fun replace(key: K, value: V): V? {
        val p = getEntry(key)
        if (p != null) {
            val oldValue = p.value
            p.value = value
            return oldValue
        }
        return null
    }

    override fun forEach(action: BiConsumer<in K, in V>) {
        var e = this.firstEntry
        while (e != null) {
            action.accept(e.key, e.value)
            e = successor(e)
        }
    }

    override fun replaceAll(function: BiFunction<in K, in V?, out V>) {
        throw UnsupportedOperationException()
    }

    internal class Entry<K : Any, V>(
        override var key: K,
        override var value: V,
        var parent: Entry<K, V>?
    ) : MutableMap.MutableEntry<K, V> {
        var left: Entry<K, V>? = null
        var right: Entry<K, V>? = null
        var color: Boolean = BLACK

        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }

        override fun equals(other: Any?): Boolean {
            return other is MutableMap.MutableEntry<*, *>
                    && valEquals(key, other.key)
                    && valEquals(value, other.value)
        }

        override fun hashCode(): Int {
            val keyHash = key.hashCode()
            val valueHash = value?.hashCode() ?: 0
            return keyHash xor valueHash
        }

        override fun toString(): String = "$key=$value"
    }

    internal fun firstEntry(): Entry<K, V>? {
        return firstEntry
    }

    private fun rotateLeft(p: Entry<K, V>?) {
        if (p != null) {
            val r = p.right
            p.right = r!!.left
            if (r.left != null) r.left!!.parent = p
            r.parent = p.parent
            if (p.parent == null) root = r
            else if (p.parent!!.left === p) p.parent!!.left = r
            else p.parent!!.right = r
            r.left = p
            p.parent = r
        }
    }

    private fun rotateRight(p: Entry<K, V>?) {
        if (p != null) {
            val l = p.left
            p.left = l!!.right
            if (l.right != null) l.right!!.parent = p
            l.parent = p.parent
            if (p.parent == null) root = l
            else if (p.parent!!.right === p) p.parent!!.right = l
            else p.parent!!.left = l
            l.right = p
            p.parent = l
        }
    }

    @Suppress("SimplifyBooleanWithConstants")
    private fun fixAfterInsertion(x: Entry<K, V>) {
        var x: Entry<K, V>? = x
        x!!.color = RED

        while (x != null && x !== root && x.parent!!.color == RED) {
            if (parentOf(x) === leftOf(parentOf(parentOf(x)))) {
                val y = rightOf(parentOf(parentOf(x)))
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK)
                    setColor(y, BLACK)
                    setColor(parentOf(parentOf(x)), RED)
                    x = parentOf(parentOf(x))
                } else {
                    if (x === rightOf(parentOf(x))) {
                        x = parentOf(x)
                        rotateLeft(x)
                    }
                    setColor(parentOf(x), BLACK)
                    setColor(parentOf(parentOf(x)), RED)
                    rotateRight(parentOf(parentOf(x)))
                }
            } else {
                val y = leftOf(parentOf(parentOf(x)))
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK)
                    setColor(y, BLACK)
                    setColor(parentOf(parentOf(x)), RED)
                    x = parentOf(parentOf(x))
                } else {
                    if (x === leftOf(parentOf(x))) {
                        x = parentOf(x)
                        rotateRight(x)
                    }
                    setColor(parentOf(x), BLACK)
                    setColor(parentOf(parentOf(x)), RED)
                    rotateLeft(parentOf(parentOf(x)))
                }
            }
        }
        root!!.color = BLACK
    }

    @Suppress("SimplifyBooleanWithConstants")
    internal fun deleteEntry(p: Entry<K, V>) {
        var p = p
        size--

        if (p === firstEntry) {
            firstEntry = successor(firstEntry)
        }

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != null && p.right != null) {
            // Note: p will never be the first node
            val s = successor(p)!! // not-null because it will at least find p.right
            p.key = s.key
            p.value = s.value
            p = s
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        val replacement = if (p.left != null) p.left else p.right

        if (replacement != null) {
            // Link replacement to parent
            replacement.parent = p.parent
            if (p.parent == null) root = replacement
            else if (p === p.parent!!.left) p.parent!!.left = replacement
            else p.parent!!.right = replacement

            // Null out links so they are OK to use by fixAfterDeletion.
            p.parent = null
            p.right = null
            p.left = null

            // Fix replacement
            if (p.color == BLACK) fixAfterDeletion(replacement)
        } else if (p.parent == null) { // return if we are the only node.
            root = null
        } else { //  No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK) fixAfterDeletion(p)
            if (p.parent != null) {
                if (p === p.parent!!.left) p.parent!!.left = null
                else if (p === p.parent!!.right) p.parent!!.right = null
                p.parent = null
            }
        }
    }

    @Suppress("SimplifyBooleanWithConstants")
    private fun fixAfterDeletion(x: Entry<K, V>?) {
        var x = x
        while (x !== root && colorOf(x) == BLACK) {
            if (x === leftOf(parentOf(x))) {
                var sib: Entry<K, V>? = rightOf(parentOf(x))

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK)
                    setColor(parentOf(x), RED)
                    rotateLeft(parentOf(x))
                    sib = rightOf(parentOf(x))
                }

                if (colorOf(leftOf(sib)) == BLACK &&
                    colorOf(rightOf(sib)) == BLACK
                ) {
                    setColor(sib, RED)
                    x = parentOf(x)
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK)
                        setColor(sib, RED)
                        rotateRight(sib)
                        sib = rightOf(parentOf(x))
                    }
                    setColor(sib, colorOf(parentOf(x)))
                    setColor(parentOf(x), BLACK)
                    setColor(rightOf(sib), BLACK)
                    rotateLeft(parentOf(x))
                    x = root
                }
            } else { // symmetric
                var sib: Entry<K, V>? = leftOf(parentOf(x))

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK)
                    setColor(parentOf(x), RED)
                    rotateRight(parentOf(x))
                    sib = leftOf(parentOf(x))
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                    colorOf(leftOf(sib)) == BLACK
                ) {
                    setColor(sib, RED)
                    x = parentOf(x)
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK)
                        setColor(sib, RED)
                        rotateLeft(sib)
                        sib = leftOf(parentOf(x))
                    }
                    setColor(sib, colorOf(parentOf(x)))
                    setColor(parentOf(x), BLACK)
                    setColor(leftOf(sib), BLACK)
                    rotateRight(parentOf(x))
                    x = root
                }
            }
        }

        setColor(x, BLACK)
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K?, V?>>
        get() = throw UnsupportedOperationException()

    companion object {
        fun valEquals(o1: Any?, o2: Any?): Boolean = o1 == o2

        // Red-black mechanics
        private const val RED = false
        private const val BLACK = true

        private fun <K : Any, V> successor(t: Entry<K, V>?): Entry<K, V>? {
            if (t == null) {
                return null
            } else if (t.right != null) {
                var p = t.right
                while (p!!.left != null) {
                    p = p.left
                }
                return p
            } else {
                var p = t.parent
                var ch: Entry<K, V>? = t
                while (p != null && ch === p.right) {
                    ch = p
                    p = p.parent
                }
                return p
            }
        }

        private fun <K : Any, V> colorOf(p: Entry<K, V>?): Boolean = (p?.color ?: BLACK)

        private fun <K : Any, V> parentOf(p: Entry<K, V>?): Entry<K, V>? = (p?.parent)

        private fun <K : Any, V> setColor(p: Entry<K, V>?, c: Boolean) {
            if (p != null)
                p.color = c
        }

        private fun <K : Any, V> leftOf(p: Entry<K, V>?): Entry<K, V>? = p?.left

        private fun <K : Any, V> rightOf(p: Entry<K, V>?): Entry<K, V>? = p?.right
    }
}
