import common.ThreePartChallenge
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class Tests2024EC {
    @TestFactory
    fun ec2024_part1(): List<DynamicTest> =
        allChallengesUnder<ThreePartChallenge>("ec2024")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    it.assertPart1Correct()
                }
            }

    @TestFactory
    fun ec2024_part2(): List<DynamicTest> =
        allChallengesUnder<ThreePartChallenge>("ec2024")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    it.assertPart2Correct()
                }
            }

    @TestFactory
    fun ec2024_part3(): List<DynamicTest> =
        allChallengesUnder<ThreePartChallenge>("ec2024")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    it.assertPart3Correct()
                }
            }
}
