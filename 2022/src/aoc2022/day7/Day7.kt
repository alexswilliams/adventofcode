package aoc2022.day7

import aoc2022.common.*
import aoc2022.day7.InputParsing.ListingItem.Companion.toFileSet
import kotlin.test.*

private val exampleInput = "aoc2022/day7/example.txt".fromClasspathFileToLines()
private val puzzleInput = "aoc2022/day7/input.txt".fromClasspathFileToLines()
private val part1ExpectedTree = "aoc2022/day7/expectedExampleFileTree.txt".fromClasspathFile()
private const val PART_1_EXPECTED_ANSWER = 95437
private const val PART_2_EXPECTED_ANSWER = 24933642

fun main() {
    val exampleFileTree = toFileSet(exampleInput)
    val puzzleFileTree = toFileSet(puzzleInput)

    assertEquals(part1ExpectedTree, renderFileTree(exampleFileTree))
    assertEquals(PART_1_EXPECTED_ANSWER, part1(exampleFileTree))
    println("Part 1: " + part1(puzzleFileTree)) // 1297159

    assertEquals(PART_2_EXPECTED_ANSWER, part2(exampleFileTree))
    println("Part 2: " + part2(puzzleFileTree)) // 3866390
}

private fun part1(fileSet: FileSet) =
    sizeOfAllDirectories(fileSet).values
        .filter { it <= 100_000 }
        .sum()

private fun part2(fileSet: FileSet): Int {
    val allDirsWithSize = sizeOfAllDirectories(fileSet)
    val spaceRequired = 30_000_000 - (70_000_000 - allDirsWithSize.getValue(listOf("/")))
    return allDirsWithSize.values
        .filter { it >= spaceRequired }
        .min()
}


private fun sizeOfAllDirectories(fileSet: FileSet): Map<FilePath, Int> = fileSet.asSequence()
    .flatMap { allParentsOf(it.path.dropLast(1)) }
    .distinct()
    .associateWith { path -> fileSet.filter { it.path.startsWith(path) }.sumOf { it.size } }

private fun allParentsOf(path: FilePath): Collection<FilePath> =
    path.runningFold(emptyList()) { acc, s -> acc + s }


private data class FileInTree(val path: FilePath, val size: Int)
private typealias FilePath = List<String>
private typealias FileSet = Set<FileInTree>


private object InputParsing {
    data class TraversalState(val tree: FileSet = emptySet(), val cwd: FilePath = emptyList())

    sealed interface ListingItem {
        fun applyToState(state: TraversalState): TraversalState
        data class CdCommand(val dirName: String) : ListingItem {
            override fun applyToState(state: TraversalState) =
                if (dirName == "..")
                    state.copy(cwd = state.cwd.dropLast(1))
                else
                    state.copy(cwd = state.cwd + dirName)
        }

        data class File(val fileName: String, val fileSize: Int) : ListingItem {
            override fun applyToState(state: TraversalState) =
                state.copy(tree = state.tree.plus(FileInTree(state.cwd + fileName, fileSize)))
        }

        companion object {
            fun toFileSet(input: List<String>) = input
                .mapNotNull { fromInputLine(it) }
                .fold(TraversalState()) { state, listingItem -> listingItem.applyToState(state) }
                .tree

            fun fromInputLine(input: String): ListingItem? {
                val values = input.split(' ')
                val fileSize = values[0].toIntOrNull()
                return when {
                    input.startsWith("$ cd ") -> CdCommand(values[2])
                    fileSize != null -> File(values[1], fileSize)
                    else -> null
                }
            }
        }
    }
}

// Just for fun, as an extra test - render the tree in the same fashion as the example on the page
private fun renderFileTree(tree: FileSet, directory: FilePath = listOf("/")): String {
    val prefix = " ".repeat((directory.size - 1) * 2)
    return tree.filter { it.path.size > directory.size && it.path.startsWith(directory) }
        .groupBy { it.path.take(directory.size + 1) }
        .toSortedMap { a, b -> a.joinToString("/").compareTo(b.joinToString("/")) }
        .entries
        .fold(prefix + "- ${directory.last()} (dir)\n") { response, (root, paths) ->
            response + if (paths.all { it.path.size == root.size }) {
                prefix + "  - ${paths.single().path.last()} (file, size=${paths.single().size})\n"
            } else {
                renderFileTree(tree, root)
            }
        }
}
