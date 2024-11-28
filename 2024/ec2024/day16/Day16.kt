package ec2024.day16

import common.*

private val examples = loadFilesToLines("ec2024/day16", "example1.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2024/day16", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day16.assertCorrect()
    benchmark { part1(puzzles[0]) } // 29.3µs
    benchmark(100) { part2(puzzles[1]) } // 25.8ms
//    benchmark(100) { part3(puzzles[2]) }
}

internal object Day16 : Challenge {
    override fun assertCorrect() {
        check(">.- -.- ^,-", "P1 Example") { part1(examples[0]) }
        check("<.* *:* ^_< >:*", "P1 Puzzle") { part1(puzzles[0]) }

        check(280014668134L, "P2 Example") { part2(examples[0]) }
        check(139028598832L, "P2 Puzzle") { part2(puzzles[1]) }

        check("627 128", "P3 Example") { part3(examples[1]) }
//        check("0 0", "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String =
    parse(input) { line, range -> line.substring(range) }
        .joinToString(" ") { (wheel, step) -> wheel[(100 * step) % wheel.size] }

private fun part2(input: List<String>): Long {
    val wheels = parse(input) { line, range -> "${line[range.first]}${line[range.last]}" }
        .map { (wheel, step) -> wheel.indices.map { i -> wheel[(i * step) % wheel.size] } }

    val target = 202420242024L
    val cycleLength = lcm(wheels.map { it.size })
    val cycleCount = (target / cycleLength)
    val remainderLength = (target % cycleLength).toInt()

    fun faceAt(iteration: Int): String = wheels.joinToString("") { wheel -> wheel[iteration % wheel.size] }
    fun valueOfFacesAtIteration(iteration: Int): Int = faceAt(iteration).frequency2().sumOf { (_, freq) -> (freq - 2).coerceAtLeast(0) }

    val valueOfRemainder = (1..remainderLength).sumOf { iteration -> valueOfFacesAtIteration(iteration) }
    val valueAfterRemainder = (remainderLength + 1..cycleLength).sumOf { iteration -> valueOfFacesAtIteration(iteration) }
    return valueOfRemainder * (cycleCount + 1) + valueAfterRemainder * cycleCount
}

private fun part3(input: List<String>): String {
    // you could start at any of the lcm(wheels.map{size}) states, you no longer start at state 0
    // the order of the wheels now matters, it's like there's an extra offset
    val wheels = parse(input) { line, range -> "${line[range.first]}${line[range.last]}" }

    /*
    For 1 right pull:
    -> [f(0,0,0), f(down(0,0,0)), f(up(0,0,0))]

    For 2 right pulls:
    -> [f(f(0,0,0)), f(f(down(0,0,0)), f(f(up(0,0,0)),
                     f(down(f(0,0,0)), f(up(f(0,0,0))]

    For 3 right pulls:
    -> [f(f(f(0,0,0))), f(f(f(down(0,0,0))), f(f(f(up(0,0,0))),
                        f(f(down(f(0,0,0))), f(f(up(f(0,0,0))),
                        f(down(f(f(0,0,0))), f(up(f(f(0,0,0)))]

    a pattern emerges... where
        f(a,b,c) -> ((a + step[0]) % size[0]; (b + step[1]) % size[1]; (c + step[2]) % size[2])
        up(a,b,c) -> ((a-1+size[0]) % size[0]; (b-1+size[1]) % size[1]; (c-1+size[2]) % size[2])
        down(a,b,c) -> ((a+1) % size[0]; (b+1) % size[1]; (c+1) % size[2])

    f() looks very memoize-able...
     */

    val sizes = wheels.map { (wheel, step) -> wheel.size }
    val steps = wheels.map { (wheel, step) -> step }
    val start = wheels.indices.map { 0 }

    fun facesAt(positions: List<Int>) = positions.mapIndexed { wheel, i -> wheels[wheel].first[i] }.joinToString("")
    fun valueOfFaces(faces: String): Int = faces.frequency2().sumOf { (_, freq) -> (freq - 2).coerceAtLeast(0) }
    fun rightLever(positions: List<Int>) = positions.mapIndexed { wheel, i -> (i + steps[wheel]) % sizes[wheel] }
    fun up(positions: List<Int>) = positions.mapIndexed { wheel, i -> (i - 1 + sizes[wheel]) % sizes[wheel] }
    fun down(positions: List<Int>) = positions.mapIndexed { wheel, i -> (i + 1) % sizes[wheel] }

    // todo, this needs to export the value at each iteration, not just the positions
    fun downAt(numberOfFs: Int, downAfterF: Int): List<Int> {
        var positions = start
        for (i in 0..<numberOfFs) {
            if (i == downAfterF)
                positions = down(positions)
            positions = rightLever(positions)
        }
        return positions
    }

    fun upAt(numberOfFs: Int, upAfterF: Int): List<Int> {
        var positions = start
        for (i in 0..<numberOfFs) {
            if (i == upAfterF)
                positions = up(positions)
            positions = rightLever(positions)
        }
        return positions
    }

    fun allFs(numberOfFs: Int): List<Int> {
        var positions = start
        for (i in 0..<numberOfFs) {
            positions = rightLever(positions)
        }
        return positions
    }

    val after1 = listOf(allFs(1), downAt(1, 0), upAt(1, 0))
    println(after1.map { facesAt(it) })
    println(after1.map { valueOfFaces(facesAt(it)) })

    val after2 = listOf(allFs(2), downAt(2, 0), upAt(2, 0), downAt(2, 1), upAt(2, 1))
    println(after2.map { facesAt(it) })
    println(after2.map { valueOfFaces(facesAt(it)) })


    return ""
}

private fun parse(input: List<String>, face: (String, IntRange) -> String) =
    input.first().splitToInts(",").let { steps ->
        input.drop(2).map { line ->
            steps.indices.map { i -> i * 4..i * 4 + 2 }.map { range -> if (line.indices fullyContains range) face(line, range) else null }
        }.transpose().map { it.filterNotNullOrBlank() }.zip(steps)
    }
