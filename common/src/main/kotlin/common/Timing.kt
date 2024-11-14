package common;

import kotlin.time.Duration
import kotlin.time.measureTime

fun benchmark(times: Int = 1000, body: () -> Unit): Duration {
    val duration = measureTime { repeat(times) { body() } }.div(times)
    println("Average Duration: $duration")
    return duration
}
