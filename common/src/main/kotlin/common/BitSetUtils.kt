package common

const val EMPTY_BITSET = 0L
typealias BitSet = Long

operator fun BitSet.contains(other: Long): Boolean = (this and other) == other
infix fun BitSet.plusItem(other: Long): BitSet = (this or other)
fun BitSet.excluding(other: BitSet): BitSet = (this and other) xor this

fun Collection<Long>.asBitSet(): BitSet = this.sum()

fun Long.toIndex(): Int = this.countTrailingZeroBits() - 1

inline fun BitSet.forEach(body: (Long) -> Unit) {
    var x = this
    while (x != 0L) x = x.takeHighestOneBit().also { body(it) }.let { x xor it }
}

inline fun <R : Comparable<R>> BitSet.maxOf(selector: (Long) -> R): R {
    var x = this
    var maxValue = selector(x.takeHighestOneBit().also { x = x xor it })
    while (x != 0L) {
        x.takeHighestOneBit().also {
            val v = selector(it)
            if (maxValue < v) maxValue = v
        }.also { x = x xor it }
    }
    return maxValue
}
