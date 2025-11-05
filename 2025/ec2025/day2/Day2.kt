package ec2025.day2

import common.*
import kotlinx.coroutines.*
import java.awt.Color.*
import java.awt.image.*
import java.io.*
import javax.imageio.*
import kotlin.math.*

private val examples = loadFilesToLines("ec2025/day2", "example1.txt", "example2.txt", "example3.txt")
private val puzzles = loadFilesToLines("ec2025/day2", "input1.txt", "input2.txt", "input3.txt")

internal fun main() {
    Day2.assertCorrect()
    benchmark { part1(puzzles[0]) } // 3.2µs
    benchmark { part2(puzzles[1], outputPng = false) } // 395.4µs
    benchmark(10) { part3(puzzles[2], outputPng = false) } // 17.5ms
}

internal object Day2 : Challenge {
    override fun assertCorrect() {
        check("[357,862]", "P1 Example") { part1(examples[0]) }
        check("[140021,700015]", "P1 Puzzle") { part1(puzzles[0]) }

        check(4076, "P2 Example") { part2(examples[1]) }
        check(1387, "P2 Puzzle") { part2(puzzles[1]) }

        check(406954, "P3 Example") { part3(examples[2]) }
        check(136830, "P3 Puzzle") { part3(puzzles[2]) }
    }
}


private fun part1(input: List<String>): String {
    val x = input[0].toLongFromIndex(3)
    val y = input[0].toLongFromIndex(input[0].indexOf(',', 4) + 1)

    var rX = 0L
    var rY = 0L
    repeat(3) {
        val tmpX = rX
        rX = (rX * rX - rY * rY) / 10L + x
        rY = (tmpX * rY) / 5L + y
    }
    return "[${rX},${rY}]"
}

private fun part2(input: List<String>, outputPng: Boolean = true): Int = filterGrid(input, 10, outputPng)
private fun part3(input: List<String>, outputPng: Boolean = true): Int = filterGrid(input, 1, outputPng)


private fun filterGrid(input: List<String>, step: Long, outputPng: Boolean): Int {
    val startX = input[0].toLongFromIndex(3)
    val startY = input[0].toLongFromIndex(input[0].indexOf(',', 4) + 1)

    val grid = Array(1001 / step.toInt() + 1) { BooleanArray(1001 / step.toInt() + 1) }

    return runBlocking(Dispatchers.Default) {
        LongProgression.fromClosedRange(startX, startX + 1000, step).map { x ->
            async {
                LongProgression.fromClosedRange(startY, startY + 1000, step)
                    .count { y ->
                        var rX = 0L
                        var rY = 0L
                        repeat(100) {
                            val tmpX = rX
                            rX = (rX * rX - rY * rY) / 100_000L + x
                            rY = (tmpX * rY) / 50_000L + y

                            if (rX.absoluteValue > 1_000_000 || rY.absoluteValue > 1_000_000)
                                return@count false
                        }
                        if (outputPng) grid[((x - startX) / step).toInt()][((y - startY) / step).toInt()] = true
                        true
                    }
            }
        }.awaitAll().sum()
    }.also { if (outputPng) drawGrid(grid) }
}

private fun drawGrid(grid: Array<BooleanArray>) {
    val img = BufferedImage(grid.size, grid[0].size, BufferedImage.TYPE_INT_RGB)
    val pixelData = grid.flatMap { row -> row.flatMap { col -> (if (col) orange else black).let { listOf(it.red, it.green, it.blue) } } }
    img.raster.setPixels(0, 0, img.width, img.height, pixelData.toIntArray())
    ImageIO.write(img, "PNG", File("/tmp/ec/day2/${img.height}x${img.width}.png").also { it.mkdirs() })
}
