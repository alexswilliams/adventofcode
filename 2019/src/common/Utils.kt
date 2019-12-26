package common

import java.io.File

fun String.fromClasspathFileToLines(): List<String> {
    val url = Loader::class.java.classLoader.getResource(this)
        ?: throw Exception("Could not find file '$this'")

    return File(url.toURI())
        .readLines()
        .filter { it.isNotEmpty() }
}

object Loader
