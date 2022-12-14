package common

import java.io.*

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

infix fun IntRange.fullyContains(other: IntRange) = this.contains(other.first) && this.contains(other.last)
infix fun IntRange.overlaps(other: IntRange) = this.intersect(other).isNotEmpty()

fun <T> List<List<T>>.transpose(): List<List<T>> =
    List(first().size) { col ->
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

inline fun <T> List<T>.takeUntilIncludingItemThatBreaksCondition(predicate: (T) -> Boolean): List<T> = ArrayList<T>().also {
    for (item in this) {
        it.add(item)
        if (predicate(item)) break
    }
}

fun <T> List<T>.tail(): List<T> = if (isEmpty()) emptyList() else subList(1, size)

fun <R, C> cartesianProductOf(rows: Iterable<R>, cols: Iterable<C>): List<Pair<R, C>> = rows.flatMap { row -> cols.map { col -> row to col } }

fun Iterable<Int>.product() = this.reduce { acc, i -> acc * i }
fun Iterable<Long>.product() = this.reduce { acc, i -> acc * i }
fun Iterable<Int>.runningTotal(start: Int): List<Int> = this.runningFold(start) { acc, i -> acc + i }

tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
fun lcm(input: List<Int>) = input.fold(1) { acc, i -> acc * (i / gcd(acc, i)) }

fun String.linesAsCharArrays(skipEmptyLines: Boolean = false): List<CharArray> {
    if (this.isEmpty()) return emptyList()
    val list = ArrayList<CharArray>()
    val s = this.toCharArray()
    var start = 0;
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

object Common
