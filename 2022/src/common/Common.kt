package common

import java.io.*

fun String.fromClasspathFileToLines(): List<String> {
    val url = Common::class.java.classLoader.getResource(this)
        ?: throw Exception("Could not find file '$this'")
    return File(url.toURI()).readLines()
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

object Common
