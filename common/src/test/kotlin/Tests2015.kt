import com.github.ajalt.mordant.rendering.*
import common.*
import org.junit.jupiter.api.*

class Tests2015 {
    @TestFactory
    fun aoc2015(): List<DynamicTest> =
        allChallengesUnder<Challenge>("aoc2015")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    println(TextColors.cyan(it::class.simpleName!!))
                    it.assertCorrect()
                }
            }
}
