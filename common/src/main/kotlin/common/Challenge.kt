package common

import com.github.ajalt.mordant.rendering.*
import kotlin.test.*

interface Challenge {
    fun assertCorrect()

    fun <R> check(expected: R, description: String, test: () -> R) {
        val result = test()

        if (description.contains("Example")) {
            println(TextColors.gray(" - $description: $result"))
        } else {
            println(" - $description: ${TextColors.brightBlue(result.toString())}")
        }
        assertEquals(expected, result)
    }
}
