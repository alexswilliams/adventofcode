package common

import java.io.File

fun String.fromClasspathFileToLines(): List<String> {
    val url = Loader::class.java.classLoader.getResource(this)
        ?: throw Exception("Could not find file '$this'")

    return File(url.toURI())
        .readLines()
        .filter { it.isNotEmpty() }
}

fun String.fromClasspathFileToProgram(): IntArray = this.fromClasspathFileToLines()
    .asSequence()
    .map { it.split(',') }.flatten()
    .map(String::trim).filter(String::isNotEmpty)
    .map(String::toInt)
    .toList().toIntArray()

object Loader
