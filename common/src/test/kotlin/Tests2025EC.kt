import com.github.ajalt.mordant.rendering.*
import common.*
import org.junit.jupiter.api.*

class Tests2025EC {
    @TestFactory
    fun ec2025(): List<DynamicTest> =
        allChallengesUnder<Challenge>("ec2025")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    println(TextColors.cyan(it::class.simpleName!!))
                    it.assertCorrect()
                }
            }

}
