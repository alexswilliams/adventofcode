package day12

import common.fromClasspathFileToLines
import java.util.regex.Pattern
import kotlin.math.absoluteValue
import kotlin.streams.toList
import kotlin.test.assertEquals


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
}

private data class XYZ(val x: Int, val y: Int, val z: Int) {
    override fun toString() = "[$x,$y,$z]"
    fun applyVector(vector: XYZ) = XYZ(x + vector.x, y + vector.y, z + vector.z)
    fun sum() = x.absoluteValue + y.absoluteValue + z.absoluteValue
}

private data class MoonState(val position: XYZ, val velocity: XYZ = XYZ(0, 0, 0)) {
    val totalEnergy get() = position.sum() * velocity.sum()
}

private fun List<MoonState>.takeStep(): List<MoonState> {
    return map { moon ->
        val newVelocity = fold(moon.velocity) { velocity, otherMoon ->
            XYZ(
                x = velocity.x + pretendGravity(moon.position.x, otherMoon.position.x),
                y = velocity.y + pretendGravity(moon.position.y, otherMoon.position.y),
                z = velocity.z + pretendGravity(moon.position.z, otherMoon.position.z)
            )
        }
        MoonState(position = moon.position.applyVector(newVelocity), velocity = newVelocity)
    }
}

private val List<MoonState>.totalEnergy get() = this.sumBy { it.totalEnergy }

private fun List<MoonState>.stepSequence() =
    generateSequence(this) { it.takeStep() }

private fun pretendGravity(thisMoon: Int, otherMoon: Int) = when {
    otherMoon > thisMoon -> 1
    otherMoon < thisMoon -> -1
    else -> 0
}

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
}
