import aoc2023.day12.*
import common.*
import org.junit.jupiter.api.*

class Tests2023 {
    @TestFactory
    fun aoc2023_part1(): List<DynamicTest> =
        allChallengesUnder<TwoPartChallenge>("aoc2023")
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    it.assertPart1Correct()
                }
            }

    @TestFactory
    fun aoc2023_part2(): List<DynamicTest> =
        allChallengesUnder<TwoPartChallenge>("aoc2023")
            .filterNot { it in setOf(Day12) }
            .map {
                DynamicTest.dynamicTest(it::class.simpleName) {
                    it.assertPart2Correct()
                }
            }
}
