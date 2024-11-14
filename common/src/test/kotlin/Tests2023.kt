import aoc2023.day12.Day12
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class Tests2023 {
    @TestFactory
    fun aoc2023_part1(): List<DynamicTest> =
        allChallengesUnder("aoc2023")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    it.assertPart1Correct()
                }
            }

    @TestFactory
    fun aoc2023_part2(): List<DynamicTest> =
        allChallengesUnder("aoc2023")
            .filterNot { it in setOf(Day12) }
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    it.assertPart2Correct()
                }
            }
}
