package day8

import common.*
import kotlin.test.*


fun main() {
    runTests()

    val transmission = "day8/input.txt".fromClasspathFileToLines().first()

    val width = 25
    val height = 6
    val layers = transmission.chunked(width * height).map { it.toList() }

    val withFewestZeros = layers.minBy { it.count { char -> char == '0' } }
    val product = withFewestZeros.count { it == '1' } * withFewestZeros.count { it == '2' }
    println("Part 1: product = $product")
    assertEquals(1330, product)

    val finalImage = renderImage(layers)
    val finalImageAsPicture = finalImage
        .replace("0", " ").replace("1", "█")
        .chunked(width)
        .joinToString("\n")
    println("Part 2: final image = $finalImage")
    println("Message:\n$finalImageAsPicture")
    assertEquals(
        """
        ████  ██  █  █ ████ ████ 
        █    █  █ █  █ █    █    
        ███  █  █ ████ ███  ███  
        █    ████ █  █ █    █    
        █    █  █ █  █ █    █    
        █    █  █ █  █ ████ █    
    """.trimIndent(),
        finalImageAsPicture
    )
}

private fun renderImage(layers: List<List<Char>>) = layers.reduce { image, layer ->
    image.zip(layer) { top, bottom ->
        when (top) {
            '0', '1' -> top
            '2' -> bottom
            else -> error("Unknown pixel value $top")
        }
    }
}.joinToString("")


private fun runTests() {
    val image = "0222112222120000"
    val layers = image.chunked(2 * 2).map { it.toList() }
    assertEquals("0110", renderImage(layers))
}
