package common

import java.io.*

fun String.fromClasspathFileToLines(): List<String> {
    val url = Common::class.java.classLoader.getResource(this)
        ?: throw Exception("Could not find file '$this'")
    return File(url.toURI()).readLines()
}

infix fun IntRange.fullyContains(other: IntRange) = this.contains(other.first) && this.contains(other.last)
infix fun IntRange.overlaps(other: IntRange) = this.intersect(other).isNotEmpty()

object Common
