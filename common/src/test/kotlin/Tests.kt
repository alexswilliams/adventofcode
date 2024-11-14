import aoc2023.day1.Day1
import aoc2023.day10.Day10
import aoc2023.day11.Day11
import aoc2023.day12.Day12
import aoc2023.day13.Day13
import aoc2023.day14.Day14
import aoc2023.day15.Day15
import aoc2023.day2.Day2
import aoc2023.day3.Day3
import aoc2023.day4.Day4
import aoc2023.day5.Day5
import aoc2023.day6.Day6
import aoc2023.day7.Day7
import aoc2023.day8.Day8
import aoc2023.day9.Day9
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class Tests {
    @TestFactory
    fun part1_2023(): Iterable<DynamicTest> = listOf(
        Day1,
        Day2,
        Day3,
        Day4,
        Day5,
        Day6,
        Day7,
        Day8,
        Day9,
        Day10,
        Day11,
        Day12,
        Day13,
        Day14,
        Day15
    ).map { DynamicTest.dynamicTest(it.javaClass.simpleName) { it.assertPart1Correct() } }

    @TestFactory
    fun part2_2023(): Iterable<DynamicTest> = listOf(
        Day1,
        Day2,
        Day3,
        Day4,
        Day5,
        Day6,
        Day7,
        Day8,
        Day9,
        Day10,
        Day11,
//        Day12,
        Day13,
        Day14,
        Day15
    ).map { DynamicTest.dynamicTest(it.javaClass.simpleName) { it.assertPart2Correct() } }
}
