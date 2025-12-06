package common

import kotlin.time.*

fun benchmark(times: Int = 1000, body: () -> Unit): Duration {
    val duration = measureTime { repeat(times) { body() } }.div(times)
    println("Average Duration ($times repeats):\t$duration")
    return duration
}
