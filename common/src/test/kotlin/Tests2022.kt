import com.github.ajalt.mordant.rendering.*
import common.*
import org.junit.jupiter.api.*

class Tests2022 {
    @TestFactory
    fun aoc2022(): List<DynamicTest> =
        allChallengesUnder<Challenge>("aoc2022")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    println(TextColors.cyan(it::class.simpleName!!))
                    if (it.skipTests) fail("Tests are skipped")
                    it.assertCorrect()
                }
            }
}
