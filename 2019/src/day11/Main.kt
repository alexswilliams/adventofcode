package day11

import common.MachineState
import common.RunState
import common.fromClasspathFileToLongProgram
import common.runProgram
import kotlin.test.assertEquals


fun main() {
    runTests()

    val memory = "day11/input.txt".fromClasspathFileToLongProgram()

    val picture = paint(memory, startingPanel = Colour.BLACK)
    println("Part 1: size = ${picture.size}")
    assertEquals(1894, picture.size)

    val betterPicture = paint(memory, startingPanel = Colour.WHITE)
    println("Part 2:")
    println(betterPicture.renderToString())
    assertEquals(
        """
        ░░░██░█░░█░████░█░░░░████░░░██░███░░█░░█░░░
        ░░░░█░█░█░░░░░█░█░░░░░░░█░░░░█░█░░█░█░░█░░░
        ░░░░█░██░░░░░█░░█░░░░░░█░░░░░█░███░░████░░░
        ░░░░█░█░█░░░█░░░█░░░░░█░░░░░░█░█░░█░█░░█░░░
        ░█░░█░█░█░░█░░░░█░░░░█░░░░█░░█░█░░█░█░░█░░░
        ░░██░░█░░█░████░████░████░░██░░███░░█░░█░░░
    """.trimIndent(), betterPicture.renderToString()
    )
}

private fun paint(memory: LongArray, startingPanel: Colour): Map<XY, Long> {
    return paint(
        RobotState(
            intComputer = RunState(memory),
            position = XY(0, 0),
            heading = Heading.NORTH,
            visitedNodes = mapOf(XY(0, 0) to startingPanel.value)
        )
    )
}

private tailrec fun paint(state: RobotState): Map<XY, Long> {
    val newState = advanceRobot(state, ::runProgram)
    return if (newState.intComputer.state == MachineState.HALTED) newState.visitedNodes else paint(newState)
}

private fun advanceRobot(state: RobotState, programRunner: (RunState) -> RunState = ::runProgram): RobotState {
    val initialColour = state.visitedNodes[state.position] ?: 0

    val newState = programRunner(state.intComputer.copy(inputs = state.intComputer.inputs.plus(initialColour)))
    val (colourToPaint, rotation) = newState.outputs.takeLast(2)

    val newVisitedNodes = state.visitedNodes.plus(state.position to colourToPaint.toColour().value)
    val newHeading = if (rotation == 0L) state.heading.rotateAntiClockwise() else state.heading.rotateClockwise()
    val newPosition = state.position.afterMoving(newHeading)

    return RobotState(
        heading = newHeading,
        position = newPosition,
        visitedNodes = newVisitedNodes,
        intComputer = newState
    )
}

private fun Map<XY, Long>.renderToString(): String {
    val xs = map { it.key.x }.min()!!..map { it.key.x }.max()!!
    val ys = map { it.key.y }.max()!! downTo map { it.key.y }.min()!!
    return ys.joinToString("\n") { y ->
        xs.joinToString("") { x ->
            if (this.getOrDefault(XY(x, y), 0L) == 1L) "█" else "░"
        }
    }
}

private data class XY(val x: Int, val y: Int) {
    override fun toString() = "[$x,$y]"
}

private enum class Heading { NORTH, EAST, SOUTH, WEST }
private enum class Colour(val value: Long) { BLACK(0), WHITE(1) }

private fun Long.toColour() = when (this) {
    0L -> Colour.BLACK
    1L -> Colour.WHITE
    else -> error("Unknown colour value: $this")
}

private fun Heading.rotateAntiClockwise() = when (this) {
    Heading.NORTH -> Heading.WEST
    Heading.EAST -> Heading.NORTH
    Heading.SOUTH -> Heading.EAST
    Heading.WEST -> Heading.SOUTH
}

private fun Heading.rotateClockwise() = when (this) {
    Heading.NORTH -> Heading.EAST
    Heading.EAST -> Heading.SOUTH
    Heading.SOUTH -> Heading.WEST
    Heading.WEST -> Heading.NORTH
}

private fun XY.afterMoving(heading: Heading) = when (heading) {
    Heading.NORTH -> this.copy(y = y + 1)
    Heading.EAST -> this.copy(x = x + 1)
    Heading.SOUTH -> this.copy(y = y - 1)
    Heading.WEST -> this.copy(x = x - 1)
}

private data class RobotState(
    val heading: Heading,
    val position: XY,
    val visitedNodes: Map<XY, Long>,
    val intComputer: RunState
)


private fun runTests() {
    val mockComputer = RunState(longArrayOf())
    val initialState = RobotState(
        intComputer = mockComputer,
        heading = Heading.NORTH,
        position = XY(0, 0),
        visitedNodes = emptyMap()
    )
    val after = advanceRobot(initialState) {
        assertEquals(1, it.inputs.size) // there should be one input
        assertEquals(0, it.inputs.single()) // it should represent a black panel
        it.copy(outputs = it.outputs + listOf(1L, 0L))
    }
    assertEquals(Heading.WEST, after.heading)
    assertEquals(XY(-1, 0), after.position)
    assertEquals(1L, after.visitedNodes[XY(0, 0)])
}
