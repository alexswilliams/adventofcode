import com.github.ajalt.mordant.rendering.*
import common.*
import org.junit.jupiter.api.*

class Tests2024EC {
    @TestFactory
    fun ec2024(): List<DynamicTest> =
        allChallengesUnder<Challenge>("ec2024")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    println(TextColors.cyan(it::class.simpleName!!))
                    it.assertCorrect()
                }
            }

}
