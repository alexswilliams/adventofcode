package common

import kotlin.test.*

interface Challenge {
    fun assertCorrect()

    fun <R> check(expected: R, description: String, test: () -> R) {
        val result = test()
        println("$description: $result")
        assertEquals(expected, result)
    }
}
