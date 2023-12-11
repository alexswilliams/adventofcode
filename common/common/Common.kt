package common

import java.io.File
import kotlin.math.*

fun String.fromClasspathFileToLines(): List<String> {
    val url = Common::class.java.classLoader.getResource(this)
        ?: throw Exception("Could not find file '$this'")
    return File(url.toURI()).readLines()
}

fun String.fromClasspathFile(): String {
    val url = Common::class.java.classLoader.getResource(this)
        ?: throw Exception("Could not find file '$this'")
    return File(url.toURI()).readText()
}

fun List<String>.splitOnSpaces() = map { it.split(' ') }

fun LongRange.intersecting(other: LongRange) = LongRange(max(first, other.first), min(last, other.last))
fun LongRange.shiftedUpBy(other: Long) = LongRange(first + other, last + other)
fun LongRange.keepingAbove(lowerBoundExcl: Long) = first.coerceAtLeast(lowerBoundExcl + 1)..last
fun LongRange.keepingBelow(upperBoundExcl: Long) = first..<last.coerceAtMost(upperBoundExcl)

infix fun IntRange.fullyContains(other: IntRange) = other.first in this && other.last in this
infix fun IntRange.overlaps(other: IntRange) = other.first in this || this.first in other
infix fun IntRange.overlapsOrIsAdjacentTo(other: IntRange) =
    (this.last == other.first - 1) || (other.last == this.first - 1) || this.overlaps(other)

fun List<IntRange>.mergeAdjacent(): List<IntRange> {
    var nulls = 0
    val ranges = Array(this.size) { if (this[it].isEmpty()) null.also { nulls++ } else this[it] }
    while (true) {
        var aIdx = -1
        var bIdx = -1
        var a = -1
        found@ while (++a <= ranges.lastIndex) {
            var b = -1
            while (++b <= ranges.lastIndex) {
                if ((a != b) && ranges[a] != null && ranges[b] != null && ranges[a]!! overlapsOrIsAdjacentTo ranges[b]!!) {
                    aIdx = a
                    bIdx = b
                    break@found
                }
            }
        }
        if (aIdx == -1) return ranges.filterNotNullTo(ArrayList(ranges.size + 1 - nulls))
        val aVal = ranges[aIdx]!!
        val bVal = ranges[bIdx]!!
        ranges[aIdx] = when {
            aVal fullyContains bVal -> aVal
            bVal fullyContains aVal -> bVal
            else -> IntRange(min(aVal.first, bVal.first), max(aVal.last, bVal.last))
        }
        ranges[bIdx] = null.also { nulls++ }
    }
}

fun List<IntRange>.clampTo(minInclusive: Int, maxInclusive: Int) = this
    .mapNotNull {
        when {
            it.last < minInclusive -> null
            it.first > maxInclusive -> null
            it.first >= minInclusive && it.last <= maxInclusive -> it
            else -> IntRange(it.first.coerceAtLeast(minInclusive), it.last.coerceAtMost(maxInclusive))
        }
    }

val IntRange.size: Int get() = this.last - this.first + 1

fun <T> List<List<T>>.transpose(): List<List<T>> =
    List(first().size) { col ->
        List(size) { row ->
            this[row][col]
        }
    }

fun List<String>.transposeToChars(): List<List<Char>> =
    List(first().length) { col ->
        List(size) { row ->
            this[row][col]
        }
    }

fun <T : CharSequence> List<T>.filterNotBlank() = this.filter { it.isNotBlank() }
fun <T : CharSequence> List<T>.mapMatching(regex: Regex) = this.mapNotNull { regex.matchEntire(it)?.destructured }

tailrec fun <T> List<T>.startsWith(other: List<T>): Boolean {
    if (other.isEmpty()) return true
    if (this.isEmpty()) return false
    if (this.first() != other.first()) return false
    return subList(1, size).startsWith(other.subList(1, other.size))
}

inline fun <T> List<T>.takeUntilIncludingItemThatBreaksCondition(predicate: (T) -> Boolean): List<T> =
    ArrayList<T>().also {
        for (item in this) {
            it.add(item)
            if (predicate(item)) break
        }
    }

fun <T> List<T>.tail(): List<T> = if (isEmpty()) emptyList() else subList(1, size)

fun <R, C> cartesianProductOf(rows: Iterable<R>, cols: Iterable<C>): List<Pair<R, C>> =
    rows.flatMap { row -> cols.map { col -> row to col } }

fun Iterable<Int>.product() = this.reduce { acc, i -> acc * i }
fun Iterable<Long>.product() = this.reduce { acc, i -> acc * i }
fun Iterable<Int>.runningTotal(start: Int): List<Int> = this.runningFold(start) { acc, i -> acc + i }

tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
fun extendedGcd(a: Int, b: Int): Triple<Int, Int, Int> {
    tailrec fun extendedGcd(a0: Int, a1: Int, s0: Int, s1: Int, t0: Int, t1: Int): Triple<Int, Int, Int> =
        if (a1 == 0) Triple(a0, s0, t0) else {
            val quotient = a0 / a1
            extendedGcd(a1, a0 - quotient * a1, s1, s0 - quotient * s1, t1, t0 - quotient * t1)
        }
    return extendedGcd(a, b, 1, 0, 0, 1)
}

fun lcm(input: List<Int>) = input.fold(1) { acc, i -> acc * (i / gcd(acc, i)) }
fun lcm(input: List<Long>) = input.fold(1L) { acc, i -> acc * (i / gcd(acc, i)) }

fun String.linesAsCharArrays(skipEmptyLines: Boolean = false): List<CharArray> {
    if (this.isEmpty()) return emptyList()
    val list = ArrayList<CharArray>()
    val s = this.toCharArray()
    var start = 0
    var i = -1
    while (++i < s.lastIndex)
        if (s[i] == '\n') {
            if (i == start) {
                if (!skipEmptyLines) list.add(CharArray(0))
            } else list.add(s.copyOfRange(start, i))
            start = i + 1
        }
    if (start > s.lastIndex) {
        if (!skipEmptyLines) list.add(CharArray(0))
    } else list.add(s.copyOfRange(start, s.lastIndex + 1))
    return list
}

fun factorial(num: Int): Long =
    (2..num).fold(1L) { acc, i -> acc * i }

fun String.cyclicIterator() = object : Iterator<Char> {
    private var index = 0
    override fun hasNext() = true
    override fun next(): Char {
        if (index > this@cyclicIterator.lastIndex) index = 0
        return this@cyclicIterator[index++]
    }
}

fun String.cyclicIteratorIndexed() = object : Iterator<Pair<Int, Char>> {
    private var index = 0
    override fun hasNext() = true
    override fun next(): Pair<Int, Char> {
        if (index > this@cyclicIteratorIndexed.lastIndex) index = 0
        return index to this@cyclicIteratorIndexed[index++]
    }
}

fun <Y> List<Y>.cyclicSequence() = sequence<Y> { while (true) yieldAll(this@cyclicSequence) }

fun max(vararg x: Int): Int = x.max()

infix fun Int.divideRoundingUp(divisor: Int): Int = (this + divisor - 1) / divisor

@Suppress("UseWithIndex")
inline fun <T> Array<out T?>.forEachNotNullIndexed(action: (index: Int, T) -> Unit) {
    var index = 0
    for (item in this) {
        if (item != null) action(index, item)
        index++
    }
}


inline fun <R> CharSequence.firstNotNullOfIndexed(transform: (Int, Char) -> R?): R {
    var index = 0
    for (element in this) {
        val result = transform(index++, element)
        if (result != null) return result
    }
    throw Exception("No non-null result found")
}

inline fun <R> CharSequence.lastNotNullOfIndexed(transform: (Int, Char) -> R?): R {
    for (index in indices.reversed()) {
        val element = this[index]
        val result = transform(index, element)
        if (result != null) return result
    }
    throw Exception("No non-null result found")
}

fun <R> String.splitMappingRanges(
    delimiter: String,
    startAt: Int = 0,
    lastIndex: Int = this.lastIndex,
    transform: (String, start: Int, end: Int) -> R,
): List<R> {
    val delimiterLength = delimiter.length
    var currentOffset = startAt
    var nextIndex = indexOf(delimiter, currentOffset)
    val result = ArrayList<R>(15)
    while (nextIndex != -1) {
        result.add(transform(this@splitMappingRanges, currentOffset, nextIndex - 1))
        currentOffset = nextIndex + delimiterLength
        nextIndex = indexOf(delimiter, currentOffset)
    }
    result.add(transform(this@splitMappingRanges, currentOffset, lastIndex))
    return result
}

fun String.toIntFromIndex(startAt: Int) = this.toLongFromIndex(startAt).toInt()
fun String.toLongFromIndex(startAt: Int): Long {
    var currentOffset = startAt
    val endAt = this.lastIndex
    var value = 0L
    var char = this[startAt]
    val isNegative = char == '-'
    if (isNegative) char = this[++currentOffset]
    if (char < '0' || char > '9') throw Exception("Invalid digit")
    do {
        value = value * 10 + (char - '0')
        char = if (currentOffset < endAt) this[++currentOffset] else Char.MIN_VALUE
    } while (char in '0'..'9')
    return if (isNegative) -value else value
}

fun String.splitToInts(delimiter: String) = splitMappingRanges(delimiter) { s, start, _ -> s.toIntFromIndex(start) }
fun String.splitToLongs(delimiter: String) = splitMappingRanges(delimiter) { s, start, _ -> s.toLongFromIndex(start) }


fun CharSequence.frequency(): List<Pair<Char, Int>> {
    val result = arrayListOf<Pair<Char, Int>>()
    for (element in this) {
        val index = result.indexOfFirst { it.first == element }
        if (index == -1) result.add(element to 1)
        else result[index] = element to (result[index].second + 1)
    }
    return result
}

inline fun <T> Iterable<T>.sumOfIndexed(selector: (index: Int, T) -> Int): Int {
    var sum = 0
    var index = 0
    for (element in this) {
        sum += selector(index++, element)
    }
    return sum
}

inline fun <T> Iterable<T>.sumOfIndexed(initial: Long, selector: (index: Int, T) -> Long): Long {
    var sum = initial
    var index = 0
    for (element in this) {
        sum += selector(index++, element)
    }
    return sum
}

inline fun <R> Collection<CharArray>.mapCartesianNotNull(transform: (row: Int, col: Int, char: Char) -> R?): List<R> {
    val result = ArrayList<R>(this.size * 10)
    this.forEachIndexed { rowNum, row ->
        row.forEachIndexed { colNum, c ->
            val transformed = transform(rowNum, colNum, c)
            if (transformed != null) {
                result.add(transformed)
            }
        }
    }
    return result;
}

object Common
