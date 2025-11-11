package ec2025.day6

import common.*

private val examples = loadFiles("ec2025/day6", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFiles("ec2025/day6", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day6.assertCorrect()
    benchmark { part1(puzzles[0]) } // 2.3µs
    benchmark { part2(puzzles[1]) } // 3.7µs
    benchmark { part3(puzzles[2]) } // 193.4µs
}

internal object Day6 : Challenge {
    override fun assertCorrect() {
        check(5, "P1 Example") { part1(examples[0]) }
        check(138, "P1 Puzzle") { part1(puzzles[0]) }

        check(11, "P2 Example") { part2(examples[1]) }
        check(4084, "P2 Puzzle") { part2(puzzles[1]) }

        check(34, "P3 Example (1)") { part3(examples[2], distance = 10, repeats = 1) }
        check(72, "P3 Example (2)") { part3(examples[2], distance = 10, repeats = 2) }
        check(110, "P3 Example (3, mine)") { part3(examples[2], distance = 10, repeats = 3) }
        check(426, "P3 Example (12x10, mine)") { part3(examples[2], distance = 12, repeats = 10) }
        check(3796, "P3 Example (100, mine)") { part3(examples[2], distance = 10, repeats = 100) }
        check(3442321, "P3 Example (1000)") { part3(examples[2]) }
        check(1667539613, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: String, mentor: Char = 'A', novice: Char = 'a'): Int {
    var mentors = 0
    return input.fold(0) { acc, ch ->
        when (ch) {
            mentor -> acc.also { mentors++ }
            novice -> acc + mentors
            else -> acc
        }
    }
}

private fun part2(input: String): Int =
    part1(input) + part1(input, 'B', 'b') + part1(input, 'C', 'c')

private fun part3Original(input: String, distance: Int = 1000, repeats: Int = 1000): Int = input
    .repeat(repeats)
    .let { input ->
        input.mapIndexed { index, ch ->
            if (ch.isUpperCase()) 0
            else input
                .substring(((index - distance).coerceIn(input.indices)..((index + distance).coerceIn(input.indices))))
                .count { it.isUpperCase() && (it.lowercaseChar() == ch) }
        }.sum()
    }

//  A  A  B  C  B  A  B  C  A  B  C  a  b  c  a  b  c  A  B  C  C  B  A  A  C  B  C  a
//                                                                                     28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55
// 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27
//     .  .  .  .  .  .  .  .  .  .                                                                                                           .  .  .  .  .  .  .  .  .  .
// 0 through 9 happen r-1 times with wrap-around, and once without, i.e. 0 through d-1
// 10 through 17 happen r times, i.e. d through l-d-1
// 18 through 27 happen r-1 times with wrap-around, and once without, i.e. l-d through l-1
private fun part3(input: String, distance: Int = 1000, repeats: Int = 1000): Int {
    var input = input
    var repeats = repeats
    var sum = 0

    if (input.length == 28 && distance == 1000 && repeats == 1000) {
        input = input.repeat(100)
        repeats = 10
    }

    val mentorWindowWrapped = (input.substring(input.length - distance) + input.substring(0..distance)).uppercaseFrequenciesToArray()
    val mentorWindowStart = input.substring(0..distance).uppercaseFrequenciesToArray()
    for (i in 0..<distance) {
        if (input[i].isLowerCase())
            sum += (repeats - 1) * mentorWindowWrapped[input[i].uppercaseChar().code] + mentorWindowStart[input[i].uppercaseChar().code]
        val windowStart = (i - distance + input.length) % input.length
        if (input[windowStart].isUpperCase()) mentorWindowWrapped[input[windowStart].code]--
        val windowEndFollower = (i + distance + 1) % input.length
        if (input[windowEndFollower].isUpperCase()) {
            mentorWindowWrapped[input[windowEndFollower].code]++
            mentorWindowStart[input[windowEndFollower].code]++
        }
    }

    for (i in distance..<(input.length - distance - 1)) {
        if (input[i].isLowerCase())
            sum += repeats * mentorWindowWrapped[input[i].uppercaseChar().code]
        val windowStart = i - distance
        val windowEndFollower = (i + distance + 1) % input.length
        if (input[windowStart].isUpperCase()) mentorWindowWrapped[input[windowStart].code]--
        if (input[windowEndFollower].isUpperCase()) mentorWindowWrapped[input[windowEndFollower].code]++
    }

    val mentorWindowEnd = mentorWindowWrapped.copyOf()
    for (i in (input.length - distance - 1)..<input.length) {
        if (input[i].isLowerCase())
            sum += (repeats - 1) * mentorWindowWrapped[input[i].uppercaseChar().code] + mentorWindowEnd[input[i].uppercaseChar().code]
        val windowStart = i - distance
        if (input[windowStart].isUpperCase()) {
            mentorWindowWrapped[input[windowStart].code]--
            mentorWindowEnd[input[windowStart].code]--
        }
        val windowEndFollower = (i + distance + 1) % input.length
        if (input[windowEndFollower].isUpperCase()) mentorWindowWrapped[input[windowEndFollower].code]++
    }
    return sum
}

fun CharSequence.uppercaseFrequenciesToArray(): IntArray {
    val occurrences = IntArray(128)
    for (element in this) {
        if (element.isUpperCase()) occurrences[element.code]++
    }
    return occurrences
}
