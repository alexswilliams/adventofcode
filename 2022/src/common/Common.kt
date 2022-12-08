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

infix fun IntRange.fullyContains(other: IntRange) = this.contains(other.first) && this.contains(other.last)
infix fun IntRange.overlaps(other: IntRange) = this.intersect(other).isNotEmpty()

fun <T> List<List<T>>.transpose(): List<List<T>> =
    List(this.first().size) { col ->
        List(this.size) { row ->
            this[row][col]
        }
    }

fun <T : CharSequence> List<T>.filterNotBlank() = this.filter { it.isNotBlank() }
fun <T : CharSequence> List<T>.mapMatching(regex: Regex) = this.mapNotNull { regex.matchEntire(it)?.destructured }

tailrec fun <T> List<T>.startsWith(other: List<T>): Boolean {
    if (other.isEmpty()) return true
    if (this.isEmpty()) return false
    if (this.first() != other.first()) return false
    return this.drop(1).startsWith(other.drop(1))
}

fun <T> List<T>.takeUntilIncludingItemThatBreaksCondition(predicate: (T) -> Boolean): List<T> = ArrayList<T>().also {
    for (item in this) {
        it.add(item)
        if (predicate(item)) break
    }
}

fun cartesianProductOf(x: IntRange, y: IntRange): List<Pair<Int, Int>> = x.flatMap { row -> y.map { col -> row to col } }
fun List<Int>.product() = this.fold(1) { acc, i -> acc * i }

object Common
