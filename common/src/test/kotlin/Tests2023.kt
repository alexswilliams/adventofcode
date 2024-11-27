import aoc2023.day12.*
import com.github.ajalt.mordant.rendering.*
import common.*
import org.junit.jupiter.api.*

class Tests2023 {
    @TestFactory
    fun aoc2023(): List<DynamicTest> =
        allChallengesUnder<Challenge>("aoc2023")
            .filterNot { it is Day12 }
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    println(TextColors.cyan(it::class.simpleName!!))
                    it.assertCorrect()
                }
            }
}
