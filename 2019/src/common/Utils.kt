package common

fun String.fromClasspathFileToProgram(): IntArray = this.fromClasspathFileToLines()
    .asSequence()
    .map { it.split(',') }.flatten()
    .map(String::trim).filter(String::isNotEmpty)
    .map(String::toInt)
    .toList().toIntArray()

fun String.fromClasspathFileToLongProgram(): LongArray = this.fromClasspathFileToLines()
    .asSequence()
    .map { it.split(',') }.flatten()
    .map(String::trim).filter(String::isNotEmpty)
    .map(String::toLong)
    .toList().toLongArray()
