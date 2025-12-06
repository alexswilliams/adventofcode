package common

import com.github.ajalt.mordant.rendering.*
import kotlin.test.*
import kotlin.time.*

interface Challenge {
    fun assertCorrect()
    val skipTests: Boolean get() = false

    fun <R> check(expected: R, description: String, test: () -> R) {
        val result = if (skipTests)
            measureTimedValue { TextStyles.bold("** SKIPPED **") }
        else
            measureTimedValue { test() }

        if (skipTests) print(TextColors.brightRed(" - Test Skipped:"))
        if (description.contains("Example")) {
            println(TextColors.gray(" - $description:\t${result.value}") + TextColors.gray(TextStyles.italic("\t\t(${result.duration})")))
        } else {
            println(" - $description:\t${TextColors.brightBlue(result.value.toString())}" + TextColors.gray(TextStyles.italic("\t\t(${result.duration})")))
        }
        if (!skipTests)
            assertEquals(expected, result.value)
    }
}
