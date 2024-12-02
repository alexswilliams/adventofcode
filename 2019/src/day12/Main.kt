package day12

import common.*
import java.util.regex.*
import kotlin.math.*
import kotlin.test.*


private data class XYZ(val x: Int, val y: Int, val z: Int) {
    fun applyVector(vector: XYZ) = XYZ(x + vector.x, y + vector.y, z + vector.z)
    fun sum() = x.absoluteValue + y.absoluteValue + z.absoluteValue
}

private data class PV(val pos: Int, val vel: Int)

private data class MoonState(val position: XYZ, val velocity: XYZ = XYZ(0, 0, 0)) {
    val totalEnergy get() = position.sum() * velocity.sum()
}

private val List<MoonState>.totalEnergy get() = this.sumOf { it.totalEnergy }


fun main() {
    runTests()

    val moonStartPositions = "day12/input.txt".fromClasspathFileToLines()
        .map { line ->
            Pattern.compile("=([-0-9]+)[,>]").matcher(line)
                .results()
                .map { it.group(1).toInt() }
                .toList()
        }.map { MoonState(XYZ(it[0], it[1], it[2])) }


    val afterStep1000 = moonStartPositions.stepSequence().drop(1000).first()
    println("Part 1: total energy = ${afterStep1000.totalEnergy}")
    assertEquals(12490, afterStep1000.totalEnergy)


    val (ix, iy, iz) = findRepeatedStates(moonStartPositions)
    val lcmSteps = lcm(ix, iy, iz)
    println("Part 2: [X=$ix; Y=$iy; Z=$iz]; LCM of Steps=$lcmSteps")
    assertEquals(392733896255168L, lcmSteps)
}


private tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
private fun lcm(a: Long, b: Long) = a / gcd(a, b) * b
private fun lcm(vararg xs: Long) = xs.fold(1L) { acc, l -> lcm(acc, l) }


private fun findRepeatedStates(initialMoons: List<MoonState>): Triple<Long, Long, Long> {
    // Doing this with an immutable set caused so much allocation and GC that the process didn't return in a reasonable time.
    tailrec fun Iterator<List<PV>>.findRepeat(acc: MutableSet<List<PV>> = mutableSetOf()): Long =
        if (acc.add(this.next()))
            this.findRepeat(acc)
        else
            acc.size.toLong()

    val x = initialMoons.map { PV(it.position.x, it.velocity.x) }.stepIterator().findRepeat()
    val y = initialMoons.map { PV(it.position.y, it.velocity.y) }.stepIterator().findRepeat()
    val z = initialMoons.map { PV(it.position.z, it.velocity.z) }.stepIterator().findRepeat()
    return Triple(x, y, z)
}


private fun List<PV>.takePVStep(): List<PV> = map { (pos, vel) ->
    val newVelocity = fold(vel) { velocity, (otherPos) ->
        velocity + pretendGravity(pos, otherPos)
    }
    PV(pos + newVelocity, newVelocity)
}


private fun List<MoonState>.takeStep(): List<MoonState> = map { moon ->
    val newVelocity = fold(moon.velocity) { velocity, otherMoon ->
        XYZ(
            x = velocity.x + pretendGravity(moon.position.x, otherMoon.position.x),
            y = velocity.y + pretendGravity(moon.position.y, otherMoon.position.y),
            z = velocity.z + pretendGravity(moon.position.z, otherMoon.position.z)
        )
    }
    MoonState(position = moon.position.applyVector(newVelocity), velocity = newVelocity)
}

private fun pretendGravity(thisMoon: Int, otherMoon: Int) = when {
    otherMoon > thisMoon -> 1
    otherMoon < thisMoon -> -1
    else -> 0
}


private fun List<MoonState>.stepSequence() =
    generateSequence(this) { it.takeStep() }

private fun List<PV>.stepIterator() =
    generateSequence(this) { it.takePVStep() }.iterator()


private fun runTests() {
    val initialMoons = listOf(
        MoonState(XYZ(-1, 0, 2)),
        MoonState(XYZ(2, -10, -7)),
        MoonState(XYZ(4, -8, 8)),
        MoonState(XYZ(3, 5, -1))
    )
    val afterStep1 = initialMoons.takeStep()
    assertEquals(MoonState(XYZ(2, -1, 1), XYZ(3, -1, -1)), afterStep1[0])
    assertEquals(MoonState(XYZ(3, -7, -4), XYZ(1, 3, 3)), afterStep1[1])
    assertEquals(MoonState(XYZ(1, -7, 5), XYZ(-3, 1, -3)), afterStep1[2])
    assertEquals(MoonState(XYZ(2, 2, 0), XYZ(-1, -3, 1)), afterStep1[3])

    val afterStep2 = afterStep1.takeStep()
    assertEquals(MoonState(XYZ(5, -3, -1), XYZ(3, -2, -2)), afterStep2[0])
    assertEquals(MoonState(XYZ(1, -2, 2), XYZ(-2, 5, 6)), afterStep2[1])
    assertEquals(MoonState(XYZ(1, -4, -1), XYZ(0, 3, -6)), afterStep2[2])
    assertEquals(MoonState(XYZ(1, -4, 2), XYZ(-1, -6, 2)), afterStep2[3])

    val afterStep10 = afterStep2
        .takeStep().takeStep().takeStep().takeStep()
        .takeStep().takeStep().takeStep().takeStep()
    assertEquals(MoonState(XYZ(2, 1, -3), XYZ(-3, -2, 1)), afterStep10[0])
    assertEquals(MoonState(XYZ(1, -8, 0), XYZ(-1, 1, 3)), afterStep10[1])
    assertEquals(MoonState(XYZ(3, -6, 1), XYZ(3, 2, -3)), afterStep10[2])
    assertEquals(MoonState(XYZ(2, 0, 4), XYZ(1, -1, -1)), afterStep10[3])

    assertEquals(179, afterStep10.totalEnergy)


    val initialMoons2 = listOf(
        MoonState(XYZ(-8, -10, 0)),
        MoonState(XYZ(5, 5, 10)),
        MoonState(XYZ(2, -7, 3)),
        MoonState(XYZ(9, -8, -3))
    )
    val after100 = initialMoons2.stepSequence().drop(100).first()
    assertEquals(MoonState(XYZ(8, -12, -9), XYZ(-7, 3, 0)), after100[0])
    assertEquals(MoonState(XYZ(13, 16, -3), XYZ(3, -11, -5)), after100[1])
    assertEquals(MoonState(XYZ(-29, -11, -1), XYZ(-3, 7, 4)), after100[2])
    assertEquals(MoonState(XYZ(16, -13, 23), XYZ(7, 1, 1)), after100[3])

    assertEquals(1940, after100.totalEnergy)

    val (ix, iy, iz) = findRepeatedStates(initialMoons)
    val lcmSteps = lcm(ix, iy, iz)

    val afterLcmSteps = initialMoons.stepSequence().drop(lcmSteps.toInt()).first()
    assertEquals(2772, lcmSteps)
    assertEquals(initialMoons, afterLcmSteps)

}

