package common

import java.math.*
import kotlin.time.*
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.ZERO

fun benchmark(times: Int = 1000, body: () -> Unit): Duration {
    val duration = measureTime { repeat(times) { body() } }.div(times)
    println("Average Duration ($times repeats):\t${duration.toRoundedString()}")
    return duration
}

fun Duration.toRoundedString(): String = when (this.absoluteValue) {
    ZERO -> "0s"
    INFINITE -> "Infinity"
    else -> {
        buildString {
            absoluteValue.toComponents { days, hours, minutes, seconds, nanoseconds ->
                val hasDays = days != 0L
                val hasHours = hours != 0
                val hasMinutes = minutes != 0
                val hasSeconds = seconds != 0 || nanoseconds != 0
                var components = 0
                if (hasDays) {
                    append(days).append('d')
                    components++
                }
                if (hasHours || (hasDays && (hasMinutes || hasSeconds))) {
                    if (components++ > 0) append(' ')
                    append(hours).append('h')
                }
                if (hasMinutes || (hasSeconds && (hasHours || hasDays))) {
                    if (components++ > 0) append(' ')
                    append(minutes).append('m')
                }
                if (hasSeconds) {
                    if (components > 0) append(' ')
                    when {
                        seconds != 0 || hasDays || hasHours || hasMinutes ->
                            appendFractional(seconds, nanoseconds, 9, "s", isoZeroes = false)

                        nanoseconds >= 1_000_000 ->
                            appendFractional(nanoseconds / 1_000_000, nanoseconds % 1_000_000, 6, "ms", isoZeroes = false)

                        nanoseconds >= 1_000 ->
                            appendFractional(nanoseconds / 1_000, nanoseconds % 1_000, 3, "µs", isoZeroes = false)

                        else ->
                            append(nanoseconds).append("ns")
                    }
                }
            }
        }
    }
}

private fun StringBuilder.appendFractional(whole: Int, fractional: Int, fractionalSize: Int, unit: String, isoZeroes: Boolean) {
    val result = buildString {
        append(whole)
        if (fractional != 0) {
            append('.')
            val fracString = fractional.toString().padStart(fractionalSize, '0')
            val nonZeroDigits = fracString.indexOfLast { it != '0' } + 1
            when {
                !isoZeroes && nonZeroDigits < 3 -> appendRange(fracString, 0, nonZeroDigits)
                else -> appendRange(fracString, 0, ((nonZeroDigits + 2) / 3) * 3)
            }
        }
    }
    append(BigDecimal(result).setScale(1, RoundingMode.HALF_UP))
    append(unit)
}
