@file:Suppress("SameReturnValue", "PublicApiImplicitType", "unused", "DuplicatedCode")

package common

import java.io.*
import java.util.*
import kotlin.math.*

object Common

typealias Grid = Array<CharArray>
typealias DigitGrid = Array<IntArray>
typealias BooleanGrid = Array<BooleanArray>

fun Grid.toDigitGrid() = Array(this.height) { r -> IntArray(this.width) { c -> this[r][c].digitToInt() } }

val Grid.height get() = this.size
val Grid.width get() = this[0].size
val Grid.rowIndices get() = this.indices
val Grid.colIndices get() = this[0].indices
fun Grid.at(pos: Location1616) = this[pos.row()][pos.col()]
infix fun Location1616.isWithin(grid: Grid) = row() in grid.rowIndices && col() in grid.colIndices
fun Grid.set(pos: Location1616, value: Char) {
    this[pos.row()][pos.col()] = value
}

fun DigitGrid.locationOf(i: Int): Location1616 {
    val startRowIndex = indexOfFirst { i in it }
    return startRowIndex by16 this[startRowIndex].indexOf(i)
}

fun DigitGrid.allLocationOf(i: Int): List<Location1616> {
    return this.mapCartesianNotNull { row, col, int -> if (int == i) row by16 col else null }
}

fun Grid.subGrid(startRow: Int, startCol: Int, width: Int, height: Int): Grid =
    Array(height) { r -> this[startRow + r].copyOfRange(startCol, startCol + width) }

fun Grid.locationOf(ch: Char): Location1616 {
    val startRowIndex = indexOfFirst { ch in it }
    return startRowIndex by16 this[startRowIndex].indexOf(ch)
}

fun Grid.allLocationOf(ch: Char): List<Location1616> {
    return this.mapCartesianNotNull { row, col, char -> if (char == ch) row by16 col else null }
}

fun Grid.render(): String = this.joinToString("\n") { row -> row.joinToString("") }


val DigitGrid.height get() = this.size
val DigitGrid.width get() = this[0].size
val DigitGrid.rowIndices get() = this.indices
val DigitGrid.colIndices get() = this[0].indices
fun DigitGrid.at(pos: Location1616) = this[pos.row()][pos.col()]
fun DigitGrid.set(pos: Location1616, value: Int) {
    this[pos.row()][pos.col()] = value
}

fun <T> Array<Array<T>>.at(pos: Location1616) = this[pos.row()][pos.col()]
fun <T> Array<Array<T>>.set(pos: Location1616, value: T) {
    this[pos.row()][pos.col()] = value
}

fun BooleanGrid.at(pos: Location1616) = this[pos.row()][pos.col()]
fun BooleanGrid.filterTrue(): List<Location1616> {
    val result = ArrayList<Location1616>(this.size * 10)
    this.forEachIndexed { rowNum, row ->
        row.forEachIndexed { colNum, c ->
            if (c) result.add(rowNum by16 colNum)
        }
    }
    return result
}

fun BooleanGrid.countTrue(): Int {
    var result = 0
    this.forEach { row -> result += row.count { it } }
    return result
}

fun BooleanArray.countTrue(): Int = this.count { it }

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

fun loadFiles(root: String, vararg files: String): List<String> = files.map { "$root/$it".fromClasspathFile() }
fun loadFilesToLines(root: String, vararg files: String): List<List<String>> = files.map { "$root/$it".fromClasspathFileToLines() }
fun loadFilesToGrids(root: String, vararg files: String): List<Grid> = files.map { "$root/$it".fromClasspathFile().linesAsCharArrays() }

fun <A, B> List<String>.partitionOnLineBreak(transformFirst: (List<String>) -> A, transformSecond: (List<String>) -> B): Pair<A, B> =
    this.indexOf("").let { transformFirst(this.subList(0, it)) to transformSecond(this.subList(it + 1, this.size)) }

fun List<String>.splitOnSpaces() = map { it.split(' ') }
fun List<CharArray>.splitArrayOnSpaces() = map { it.concatToString().split(' ') }
fun List<String>.asArrayOfCharArrays(): Grid = Array(size) { r -> this[r].toCharArray() }

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

fun List<String>.transposeToStrings(): List<String> =
    List(first().length) { col ->
        StringBuilder(size).also { sb ->
            indices.forEach { row ->
                sb.append(this[row][col])
            }
        }.toString()
    }

fun List<String>.transposedView(): List<List<Char>> =
    this[0].indices.map { ListStringColumnView(it, this) }

class ListStringColumnView(private val column: Int, private val delegate: List<String>) : AbstractList<Char>(), RandomAccess {
    override val size: Int get() = delegate.size
    override fun get(index: Int): Char = delegate[index][column]
}


@Suppress("UNCHECKED_CAST")
fun <T : CharSequence> List<T?>.filterNotNullOrBlank() = this.filter { !it.isNullOrBlank() } as List<T>
fun <T : CharSequence> List<T>.filterNotBlank() = this.filter { it.isNotBlank() }
fun <T : CharSequence> List<T>.mapMatching(regex: Regex) = this.mapNotNull { regex.matchEntire(it)?.destructured }
fun String.matching(regex: Regex) = this.let { regex.matchEntire(it)?.destructured }
fun String.matchingAsIntList(regex: Regex) = this.let { regex.matchEntire(it)?.groupValues?.tail()?.map(String::toInt) }
fun String.matchingAsLongList(regex: Regex) = this.let { regex.matchEntire(it)?.groupValues?.tail()?.map(String::toLong) }

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

fun <R, C> cartesianProductSequenceOf(rows: Iterable<R>, cols: Iterable<C>): Sequence<Pair<R, C>> = sequence {
    rows.forEach { row -> cols.forEach { col -> yield(row to col) } }
}

inline fun <R, C> forEachCartesianProductOf(rows: Iterable<R>, cols: Iterable<C>, crossinline onEach: (r: R, c: C) -> Unit) {
    for (row in rows) {
        for (col in cols) {
            onEach(row, col)
        }
    }
}

inline fun <R, C> countCartesianProductOf(rows: Iterable<R>, cols: Iterable<C>, crossinline predicate: (r: R, c: C) -> Boolean): Int {
    var count = 0
    forEachCartesianProductOf(rows, cols) { r, c -> if (predicate(r, c)) count++ }
    return count
}

fun Iterable<Int>.product() = this.reduce { acc, i -> acc * i }
fun IntArray.product() = this.reduce { acc, i -> acc * i }
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

fun lcm(input: Iterable<Int>) = input.fold(1) { acc, i -> acc * (i / gcd(acc, i)) }
fun lcm(input: Iterable<Long>) = input.fold(1L) { acc, i -> acc * (i / gcd(acc, i)) }


fun String.linesAsCharArrays(skipEmptyLines: Boolean = false): Grid =
    this.linesAsCharArrayList(skipEmptyLines).toTypedArray()

fun String.linesAsCharArrayList(skipEmptyLines: Boolean = false): List<CharArray> {
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

fun String.cyclicIterator() = this.toCharArray().cyclicIterator()

interface ResettableIterator<T> : Iterator<T> {
    fun reset()
}

fun CharArray.cyclicIterator() = object : ResettableIterator<Char> {
    private var index = 0
    private val lastIndexConst = this@cyclicIterator.lastIndex
    override fun reset() = Unit.also { index = 0 }
    override fun hasNext() = true
    override fun next(): Char {
        if (index > lastIndexConst) index = 0
        return this@cyclicIterator[index++]
    }
}

fun String.cyclicIteratorIndexed() = object : ResettableIterator<Pair<Int, Char>> {
    private var index = 0
    private val lastIndexConst = this@cyclicIteratorIndexed.lastIndex
    override fun reset() = Unit.also { index = 0 }
    override fun hasNext() = true
    override fun next(): Pair<Int, Char> {
        if (index > lastIndexConst) index = 0
        return index to this@cyclicIteratorIndexed[index++]
    }
}

fun <Y> List<Y>.cyclicSequence() = sequence { while (true) yieldAll(this@cyclicSequence) }

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

fun String.toShortFromIndex(startAt: Int) = this.toLongFromIndex(startAt).toShort()
fun String.toIntFromIndex(startAt: Int) = this.toLongFromIndex(startAt).toInt()
fun String.toLongFromIndex(startAt: Int): Long {
    var currentOffset = startAt
    val endAt = this.lastIndex
    var value = 0L
    var char = this[startAt]
    val isNegative = char == '-'
    if (isNegative) char = this[++currentOffset]
    if (char !in '0'..'9') throw Exception("Invalid digit")
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

fun CharSequence.frequency2(): List<Pair<Char, Int>> {
    val occurrences = IntArray(128)
    for (element in this) {
        occurrences[element.code]++
    }
    val result = ArrayList<Pair<Char, Int>>(this.length)
    val added = BooleanArray(128)
    for (element in this) {
        if (!added[element.code]) result.add(element to occurrences[element.code])
        added[element.code] = true
    }
    return result
}

inline fun IntArray.sumOfIndexed(selector: (index: Int, e: Int) -> Int): Int {
    var sum = 0
    var index = 0
    for (element in this) {
        sum += selector(index++, element)
    }
    return sum
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

inline fun <R> Collection<String>.mapCartesianNotNull(transform: (row: Int, col: Int, char: Char) -> R?): List<R> {
    val result = ArrayList<R>(this.size * 10)
    this.forEachIndexed { rowNum, row ->
        row.forEachIndexed { colNum, c ->
            val transformed = transform(rowNum, colNum, c)
            if (transformed != null) {
                result.add(transformed)
            }
        }
    }
    return result
}

inline fun <R> Grid.mapCartesianNotNull(transform: (row: Int, col: Int, char: Char) -> R?): List<R> {
    val result = ArrayList<R>(this.size * 10)
    this.forEachIndexed { rowNum, row ->
        row.forEachIndexed { colNum, c ->
            val transformed = transform(rowNum, colNum, c)
            if (transformed != null) {
                result.add(transformed)
            }
        }
    }
    return result
}

inline fun <R> DigitGrid.mapCartesianNotNull(transform: (row: Int, col: Int, char: Int) -> R?): List<R> {
    val result = ArrayList<R>(this.size * 10)
    this.forEachIndexed { rowNum, row ->
        row.forEachIndexed { colNum, i ->
            val transformed = transform(rowNum, colNum, i)
            if (transformed != null) {
                result.add(transformed)
            }
        }
    }
    return result
}

fun Grid.allLocations(): List<Location1616> = allLocationsWhere { _, _, _ -> true }
inline fun Grid.allLocationsWhere(predicate: (row: Int, col: Int, char: Char) -> Boolean): List<Location1616> {
    val result = ArrayList<Location1616>(this.size * 10)
    this.forEachIndexed { rowNum, row ->
        row.forEachIndexed { colNum, c ->
            if (predicate(rowNum, colNum, c)) {
                result.add(rowNum by16 colNum)
            }
        }
    }
    return result
}

fun String.countOccurrences(s: String): Int {
    var count = 0
    this.indices.forEach { index ->
        if (this.startsWith(s, index)) count++
    }
    return count
}

fun String.locationOfEach(s: String): List<Int> {
    val result = ArrayList<Int>(this.length)
    this.indices.forEach { index ->
        if (this.startsWith(s, index)) result.add(index)
    }
    return result
}

fun String.locationOfEach(c: Char): List<Int> {
    val result = ArrayList<Int>(this.length)
    this.forEachIndexed { index, elem ->
        if (elem == c) result.add(index)
    }
    return result
}

fun Collection<String>.locationOfEach(c: Char): List<Location1616> {
    val result = ArrayList<Location1616>(this.size * 20)
    this.forEachIndexed { rowNum, row ->
        row.forEachIndexed { colNum, cell ->
            if (cell == c)
                result.add(rowNum by16 colNum)
        }
    }
    return result
}

fun Collection<String>.locationsOfEach(a: Char, b: Char): Pair<List<Location>, List<Location>> {
    val resultA = ArrayList<Location>(this.size * 20)
    val resultB = ArrayList<Location>(this.size * 20)
    this.forEachIndexed { rowNum, row ->
        row.forEachIndexed { colNum, cell ->
            if (cell == a)
                resultA.add(rowNum by colNum)
            else if (cell == b)
                resultB.add(rowNum by colNum)
        }
    }
    return resultA to resultB
}

inline fun <T, R : Comparable<R>> Iterable<T>.maxOfBefore(maxIndex: Int, selector: (T) -> R): R {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var i = 0
    var maxValue = selector(iterator.next())
    while (iterator.hasNext() && i++ < maxIndex) {
        val v = selector(iterator.next())
        if (maxValue < v) maxValue = v
    }
    return maxValue
}

fun Sequence<Collection<Int>>.intersect(): List<Int> {
    val iterator = this.iterator()
    val result = LinkedList(iterator.next())
    while (result.isNotEmpty() && iterator.hasNext()) {
        @Suppress("ConvertArgumentToSet")
        result.retainAll(iterator.next())
    }
    return result
}

fun <T> Collection<T>.hasNoOverlapWith(other: Iterable<T>): Boolean {
    for (elem in other)
        if (elem in this) return false
    return true
}

fun neighboursOf(pos: Location1616, grid: Grid, output: IntArray = IntArray(4)): IntArray {
    output[0] = if (pos.col() > 0) pos.minusCol() else -1
    output[1] = if (pos.row() > 0) pos.minusRow() else -1
    output[2] = if (pos.col() < grid[0].lastIndex) pos.plusCol() else -1
    output[3] = if (pos.row() < grid.lastIndex) pos.plusRow() else -1
    return output
}

fun neighboursOf(pos: Location1616, grid: Grid, output: IntArray = IntArray(4), admit: (ch: Char) -> Boolean): IntArray {
    output[0] = if (pos.col() > 0 && admit(grid.at(pos.minusCol()))) pos.minusCol() else -1
    output[1] = if (pos.row() > 0 && admit(grid.at(pos.minusRow()))) pos.minusRow() else -1
    output[2] = if (pos.col() < grid[0].lastIndex && admit(grid.at(pos.plusCol()))) pos.plusCol() else -1
    output[3] = if (pos.row() < grid.lastIndex && admit(grid.at(pos.plusRow()))) pos.plusRow() else -1
    return output
}

fun neighboursOf(pos: Location1616, grid: Grid, wall: Char, output: IntArray = IntArray(4)): IntArray {
    output[0] = if (pos.col() > 0 && grid[pos.row()][pos.col() - 1] != wall) pos.minusCol() else -1
    output[1] = if (pos.row() > 0 && grid[pos.row() - 1][pos.col()] != wall) pos.minusRow() else -1
    output[2] = if (pos.col() < grid[0].lastIndex && grid[pos.row()][pos.col() + 1] != wall) pos.plusCol() else -1
    output[3] = if (pos.row() < grid.lastIndex && grid[pos.row() + 1][pos.col()] != wall) pos.plusRow() else -1
    return output
}

fun neighboursOf(pos: Location1616, grid: Grid, walls: CharArray, output: IntArray = IntArray(4)): IntArray {
    output[0] = if (pos.col() > 0 && grid[pos.row()][pos.col() - 1] !in walls) pos.minusCol() else -1
    output[1] = if (pos.row() > 0 && grid[pos.row() - 1][pos.col()] !in walls) pos.minusRow() else -1
    output[2] = if (pos.col() < grid[0].lastIndex && grid[pos.row()][pos.col() + 1] !in walls) pos.plusCol() else -1
    output[3] = if (pos.row() < grid.lastIndex && grid[pos.row() + 1][pos.col()] !in walls) pos.plusRow() else -1
    return output
}

fun fillDeadEnds(grid: Grid, floor: Char = '.', wall: Char = '#'): Grid {
    var changed: Boolean
    do {
        changed = false

        fun maybeFillCell(row: Int, col: Int) {
            if (grid[row][col] != floor) return
            val up = grid[row - 1][col]
            val down = grid[row + 1][col]
            val left = grid[row][col - 1]
            val right = grid[row][col + 1]
            if (up == wall && down == wall && (left == wall || right == wall)) {
                changed = true
                grid[row][col] = wall
                if (left == floor) maybeFillCell(row, col - 1) else maybeFillCell(row, col + 1)
            }
            if (left == wall && right == wall && (up == wall || down == wall)) {
                changed = true
                grid[row][col] = wall
                if (up == floor) maybeFillCell(row - 1, col) else maybeFillCell(row + 1, col)
            }
        }

        for (row in 1..<grid.lastIndex) {
            for (col in 1..<grid[0].lastIndex) {
                maybeFillCell(row, col)
            }
        }
    } while (changed)
    return grid
}

fun <T> List<T>.times(n: Int): List<T> = when (n) {
    0 -> emptyList()
    1 -> this
    else -> when (size) {
        0 -> emptyList()
        else -> buildList(n * size) { repeat(n) { this.addAll(this@times) } }
    }
}

fun List<Int>.sumFrom(startAt: Int): Int = (startAt..this.lastIndex).sumOf { this[it] }

fun String.doesNotAppearBefore(ch: Char, endExcl: Int): Boolean = (0..<endExcl).none { this[it] == ch }
fun String.appearsOnOrAfter(ch: Char, startIncl: Int): Boolean = (startIncl..this.lastIndex).any { this[it] == ch }

fun String.removeDuplicatesOf(ch: Char): String {
    if (isEmpty()) return this
    var output = this
    var i = 0
    var last = output[i]
    while (i < output.length - 1) {
        val next = output[i + 1]
        if (last == ch && next == ch) {
            var j = i + 2
            while (j < output.length && output[j] == ch) j++
            output = output.removeRange(i + 1, j - 1)
        }
        i++
        last = next
    }
    return output
}

@Suppress("UseWithIndex")
fun <T> List<T>.allIndexed(predicate: (index: Int, T) -> Boolean): Boolean {
    if (isEmpty()) return true
    var index = 0
    for (element in this) {
        if (!predicate(index, element)) return false
        index++
    }
    return true
}

fun <T, Q> List<T>.allZipped(other: List<Q>, predicate: (T, Q) -> Boolean): Boolean {
    val tIterator = listIterator()
    val qIterator = other.listIterator()
    while (tIterator.hasNext() && qIterator.hasNext()) {
        if (!predicate(tIterator.next(), qIterator.next())) return false
    }
    return true
}

fun IntArray.allZipped(other: IntArray, predicate: (Int, Int) -> Boolean): Boolean {
    val tIterator = iterator()
    val qIterator = other.iterator()
    while (tIterator.hasNext() && qIterator.hasNext()) {
        if (!predicate(tIterator.next(), qIterator.next())) return false
    }
    return true
}

@Suppress("UseWithIndex")
fun <T> List<T>.anyIndexed(predicate: (index: Int, T) -> Boolean): Boolean {
    if (isEmpty()) return false
    var index = 0
    for (element in this) {
        if (predicate(index, element)) return true
        index++
    }
    return false
}

inline fun <T, K, V> Iterable<T>.groupByToSet(keySelector: (T) -> K, valueTransform: (T) -> V): MutableMap<K, MutableSet<V>> {
    val destination = LinkedHashMap<K, MutableSet<V>>()
    for (element in this) destination.getOrPut(keySelector(element)) { LinkedHashSet(20) }.add(valueTransform(element))
    return destination
}

fun <T : Comparable<T>> List<T>.plusUniqueSorted(i: T): List<T> {
    val insertPosition = this.binarySearch(i)
    return if (insertPosition >= 0) this
    else ArrayList(this).also { it.add(-insertPosition - 1, i) }
}
