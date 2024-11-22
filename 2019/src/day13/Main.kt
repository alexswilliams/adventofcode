package day13

import common.*
import kotlin.math.*
import kotlin.test.*

private val SCORE_XY = XY(-1, 0)

private data class GameState(
    val cells: MutableMap<XY, Long> = LinkedHashMap(),
    val score: Long = 0,
    val ball: XY = XY(0, 0),
    val paddle: XY = XY(0, 0),
    val blocks: Int = 0,
)
private typealias Screen = Map<XY, Long>


fun main() {
    val memory = "day13/input.txt".fromClasspathFileToLongProgram()

    val state = runUntilHalted(RunState(memory), ::runProgram)
    val gameState = interpretOutputIntoGameState(state.outputs)
    println("Part 1: block types = ${gameState.blocks}")
    assertEquals(230, gameState.blocks)


    val part2Memory = memory.copyOf().also { it[0] = 2L }
    val lastGameState = playGame(RunState(part2Memory))
    println(lastGameState.cells.renderToString())
    println("Part 2: Final Score: ${lastGameState.score}; blocks remaining: ${lastGameState.blocks}")
    assertEquals(11_140L, lastGameState.score)
}


private tailrec fun playGame(oldState: RunState, lastGameState: GameState = GameState()): GameState {
    val machineState = runUntilHalted(oldState, ::runProgram)
    val gameState = interpretOutputIntoGameState(machineState.outputs, lastGameState)

    return if (gameState.blocks == 0 || machineState.state == MachineState.HALTED)
        gameState
    else {
        if ((gameState.ball.y - gameState.paddle.y).absoluteValue == 1L)
            println("Score: ${gameState.score}; Blocks remaining: ${gameState.blocks}")

        val direction = when {
            gameState.ball.x < gameState.paddle.x -> -1L
            gameState.ball.x > gameState.paddle.x -> 1L
            else -> 0L
        }
        playGame(machineState.copy(inputs = listOf(direction), outputs = emptyList()), gameState)
    }
}


private fun interpretOutputIntoGameState(output: List<Long>, lastGameState: GameState = GameState()): GameState {
    val blocks = output.windowed(3, 3)
    val gameState = lastGameState.mergeWith(blocks)
    return gameState.copy(
        blocks = gameState.cells.count { it.value == Contents.BLOCK.value }
    )
}


private tailrec fun GameState.mergeWith(output: List<List<Long>>): GameState =
    if (output.isEmpty())
        this
    else {
        val xy = XY(output[0][0], output[0][1])
        if (xy != SCORE_XY) this.cells[xy] = output[0][2].toCellContents().value
        when {
            xy == SCORE_XY -> this.copy(score = output[0][2]).mergeWith(output.drop(1))
            output[0][2] == Contents.BALL.value -> this.copy(ball = xy).mergeWith(output.drop(1))
            output[0][2] == Contents.H_PADDLE.value -> this.copy(paddle = xy).mergeWith(output.drop(1))
            else -> this.mergeWith(output.drop(1))
        }
    }

private tailrec fun runUntilHalted(state: RunState, programRunner: (RunState) -> RunState = ::runProgram): RunState {
    val newState = programRunner(state)
    return if (newState.state == MachineState.RUNNING)
        runUntilHalted(newState, programRunner)
    else
        newState
}

private fun Screen.renderToString(): String {
    val xs = map { it.key.x }.min()..map { it.key.x }.max()
    val ys = map { it.key.y }.min()..map { it.key.y }.max()
    return ys.joinToString("\n") { y ->
        xs.joinToString("") { x ->
            this.getOrDefault(XY(x, y), Contents.EMPTY.value).toCellContents().displayedAs
        }
    }
}

private data class XY(val x: Long, val y: Long) {
    override fun toString() = "[$x,$y]"
}

private enum class Contents(
    val value: Long,
    val displayedAs: CharSequence,
) {
    EMPTY(0, "."),
    WALL(1, "▓"),
    BLOCK(2, "█"),
    H_PADDLE(3, "▁"),
    BALL(4, "◎")
}

private fun Long.toCellContents() = when (this) {
    0L -> Contents.EMPTY
    1L -> Contents.WALL
    2L -> Contents.BLOCK
    3L -> Contents.H_PADDLE
    4L -> Contents.BALL
    else -> error("Unknown value: $this")
}

