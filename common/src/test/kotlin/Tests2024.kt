import com.github.ajalt.mordant.rendering.*
import common.*
import org.junit.jupiter.api.*

class Tests2024 {
    @TestFactory
    fun aoc2023(): List<DynamicTest> =
        allChallengesUnder<Challenge>("aoc2024")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    println(TextColors.cyan(it::class.simpleName!!))
                    it.assertCorrect()
                }
            }
}
