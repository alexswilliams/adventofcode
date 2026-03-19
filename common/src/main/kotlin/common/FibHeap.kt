package common

class FibHeap<Element>(
    initialValues: Iterable<Pair<Element, Int>>,
    elementFinder: ElementFinder<Element> = ElementFinder.TreeWalker(),
    private val weightOffset: (Element) -> Int = { 0 },
) : PriorityHeap<Element> {
    private val elementFinderStrategy: ElementFinderStrategy<Element>

    constructor(
        elementFinder: ElementFinder<Element> = ElementFinder.TreeWalker(),
        weightOffset: (Element) -> Int = { 0 },
    ) : this(emptyList(), elementFinder, weightOffset)

    constructor(
        initialValue: Pair<Element, Int>,
        elementFinder: ElementFinder<Element> = ElementFinder.TreeWalker(),
        weightOffset: (Element) -> Int = { 0 },
    ) : this(listOf(initialValue), elementFinder, weightOffset)

    init {
        elementFinderStrategy = when (elementFinder) {
            is ElementFinder.TreeWalker -> ElementFinderStrategy.TreeWalkLookup { e, weight -> findElementByWalkingHeap(e, firstRootNode, weight) }
            is ElementFinder.Dictionary -> ElementFinderStrategy.DictionaryLookup()
            is ElementFinder.Grid -> ElementFinderStrategy.GridLookup(
                elementFinder.height,
                elementFinder.width,
                elementFinder.row,
                elementFinder.col
            )
        }
        initialValues.forEach { offer(it.first, it.second) }
    }

    sealed interface ElementFinder<E> {
        class TreeWalker<E> : ElementFinder<E>
        class Dictionary<E> : ElementFinder<E>
        data class Grid<E>(val height: Int, val width: Int, val row: (e: E) -> Int, val col: (e: E) -> Int) : ElementFinder<E>
    }

    private sealed interface ElementFinderStrategy<E> {
        fun link(e: E, weight: Int, node: Node<E>)
        fun unlink(e: E, weight: Int)
        fun relink(e: E, oldWeight: Int, newWeight: Int, node: Node<E>)
        fun find(e: E, weight: Int): Node<E>?

        class TreeWalkLookup<E>(private val findNode: (e: E, weight: Int) -> Node<E>?) : ElementFinderStrategy<E> {
            override fun link(e: E, weight: Int, node: Node<E>) {}
            override fun unlink(e: E, weight: Int) {}
            override fun relink(e: E, oldWeight: Int, newWeight: Int, node: Node<E>) {}
            override fun find(e: E, weight: Int): Node<E>? = findNode(e, weight)
        }

        class GridLookup<E>(height: Int, width: Int, val row: (e: E) -> Int, val col: (e: E) -> Int) : ElementFinderStrategy<E> {
            private val grid = Array(height) { Array<Node<E>?>(width) { null } }
            override fun find(e: E, weight: Int): Node<E>? = grid[row(e)][col(e)]
            override fun relink(e: E, oldWeight: Int, newWeight: Int, node: Node<E>) {}
            override fun link(e: E, weight: Int, node: Node<E>) {
                grid[row(e)][col(e)] = node
            }

            override fun unlink(e: E, weight: Int) {
                grid[row(e)][col(e)] = null
            }
        }

        class DictionaryLookup<E> : ElementFinderStrategy<E> {
            private val nodes = HashMap<E, Node<E>>()
            override fun find(e: E, weight: Int): Node<E>? = nodes[e]
            override fun relink(e: E, oldWeight: Int, newWeight: Int, node: Node<E>) {}
            override fun link(e: E, weight: Int, node: Node<E>) {
                nodes[e] = node
            }

            override fun unlink(e: E, weight: Int) {
                nodes.remove(e)
            }
        }
    }

    private data class Node<Element>(
        override var key: Int,
        override val value: Element,
        var parent: Node<Element>? = null,
        var previousSibling: Node<Element>? = null,
        var nextSibling: Node<Element>? = null,
        var firstChild: Node<Element>? = null,
        var marked: Boolean = false,
        var childCount: Int = 0,
    ) : Map.Entry<Int, Element> {

        override fun toString(): String {
            return "Node(key=$key, value=$value)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Node<*>
            if (key != other.key) return false
            if (value != other.value) return false
            return true
        }

        override fun hashCode(): Int {
            var result = key
            result = 31 * result + (value?.hashCode() ?: 0)
            return result
        }
    }

    private var firstRootNode: Node<Element>? = null
    private var minNode: Node<Element>? = null
    var size: Int = 0
        private set

    private fun offer(e: Element, weight: Int, offset: Int) {
        if (debug) println("offer($e, ${weight + offset})")
        size++
        val node = Node(weight + offset, e)
        elementFinderStrategy.link(e, weight + offset, node)

        if (size == 1) {
            makeSiblingsInOrder(node, node)
            minNode = node
        } else {
            val prev = firstRootNode?.previousSibling
            makeSiblingsInOrder(node, firstRootNode!!)
            makeSiblingsInOrder(prev!!, node)
            if (minNode!!.key > node.key) minNode = node
        }

        firstRootNode = node
    }

    override fun offer(e: Element, weight: Int) {
        offer(e, weight, weightOffset(e))
    }

    override fun offerOrReposition(e: Element, oldWeight: Int, newWeight: Int) {
        val offset = weightOffset(e)
        if (debug) println("offerOrReposition($e, ${oldWeight + offset}, ${newWeight + offset})")
        val oldWeightAdj = oldWeight + offset
        val newWeightAdj = newWeight + offset
        if (oldWeightAdj < newWeightAdj)
            throw UnsupportedOperationException("Decrease-key operation call with an increase of key: $oldWeight ($oldWeightAdj), $newWeight ($newWeightAdj)")

        if (size == 0) {
            offer(e, newWeight, offset)
            return
        }

        val foundNode = elementFinderStrategy.find(e, oldWeightAdj)
        if (foundNode === null) {
            offer(e, newWeight, offset)
            return
        }

        foundNode.key = newWeightAdj
        if (foundNode.parent === null) {
            if (minNode!!.key > newWeightAdj) minNode = foundNode
        } else if (foundNode.parent!!.key > newWeightAdj) {
            cutSubTreeToRootList(foundNode)
        }
        elementFinderStrategy.relink(e, oldWeightAdj, newWeightAdj, foundNode)
    }

    private tailrec fun cutSubTreeToRootList(node: Node<Element>) {
        deleteFromList(node)
        if (firstRootNode === null) {
            makeSiblingsInOrder(node, node)
            minNode = node
        } else {
            val prev = firstRootNode!!.previousSibling!!
            makeSiblingsInOrder(node, firstRootNode!!)
            makeSiblingsInOrder(prev, node)
            if (minNode!!.key > node.key) minNode = node
        }
        firstRootNode = node

        val originalParent = node.parent
        node.parent = null
        node.marked = false
        if (originalParent !== null && originalParent.marked)
            cutSubTreeToRootList(originalParent)
        else if (originalParent !== null && originalParent.parent !== null)
            originalParent.marked = true
    }

    private fun findElementByWalkingHeap(e: Element, startNode: Node<Element>?, oldWeight: Int): Node<Element>? {
        if (startNode === null) return null
        var node: Node<Element> = startNode
        do {
            if (node.value == e) return node
            if (node.key > oldWeight) return null
            if (node.firstChild !== null) {
                val foundInChildren = findElementByWalkingHeap(e, node.firstChild!!, oldWeight)
                if (foundInChildren !== null) return foundInChildren
            }
            node = node.nextSibling!!
        } while (node !== startNode)
        return null
    }

    // Leaves children orphaned / to be dealt with by the caller
    private fun deleteFromList(nodeToDelete: Node<Element>) {
        nodeToDelete.parent?.childCount--

        if (nodeToDelete.nextSibling === nodeToDelete) { // List size 1
            nodeToDelete.parent?.firstChild = null
            if (firstRootNode === nodeToDelete) firstRootNode = null
            return
        }

        if (nodeToDelete.parent?.firstChild === nodeToDelete)
            nodeToDelete.parent!!.firstChild = nodeToDelete.nextSibling

        val prev = nodeToDelete.previousSibling!!
        val next = nodeToDelete.nextSibling!!
        makeSiblingsInOrder(prev, next)
        nodeToDelete.nextSibling = null
        nodeToDelete.previousSibling = null
        if (firstRootNode === nodeToDelete) firstRootNode = next
    }

    override fun poll(): Element? = pollEntry()?.value

    override fun pollEntry(): Map.Entry<Int, Element>? {
        val nodeToPop = minNode ?: return null.also { if (debug) println("pollEntry() == ${null}") }

        deleteFromList(nodeToPop)
        elementFinderStrategy.unlink(nodeToPop.value, nodeToPop.key)

        if (nodeToPop.firstChild !== null)
            promoteAllSiblingsToRootList(nodeToPop.firstChild!!)

        if (debug) println("pollEntry() == $nodeToPop")

        rebalance()

        findNewMinNode()

        size--
        return nodeToPop
    }

    private fun promoteAllSiblingsToRootList(firstSibling: Node<Element>) {
        val nodeAfterSublist = firstRootNode

        firstRootNode = firstSibling
        if (nodeAfterSublist === null) return

        val lastNodeInRootList = nodeAfterSublist.previousSibling!!
        val lastSibling = firstSibling.previousSibling!!
        makeSiblingsInOrder(lastNodeInRootList, firstSibling)
        makeSiblingsInOrder(lastSibling, nodeAfterSublist)

        var n = firstSibling
        do {
            n.parent = null
            n = n.nextSibling!!
        } while (n !== nodeAfterSublist)
    }

    private fun findNewMinNode() {
        var newMinNode = firstRootNode
        if (firstRootNode !== null) {
            var node = firstRootNode!!
            do {
                if (node.parent !== null) node.parent = null
                if (newMinNode!!.key > node.key) newMinNode = node
                node = node.nextSibling!!
            } while (node !== firstRootNode)
        }
        minNode = newMinNode
    }

    private var nodeWithDegree = Array<Node<Element>?>(32) { null }

    private fun rebalance() {
        if (firstRootNode?.nextSibling === firstRootNode) return // size 0 or 1 is always balanced

        val alreadySeen = mutableSetOf<Node<Element>>()
        nodeWithDegree.fill(null)
        var pointer = firstRootNode!!
        do {
            if (debug) println("rebalance(): pointer === $pointer; ${nodeWithDegree.contentToString()}")
//            if (!alreadySeen.add(pointer)) throw Error("Already seen node $pointer")
            val existingNode = nodeWithDegree[pointer.childCount]
            if (existingNode === null || existingNode.parent !== null) {
                nodeWithDegree[pointer.childCount] = pointer
            } else if (existingNode !== pointer) {
                val staysInRootList = if (existingNode.key < pointer.key) existingNode else pointer
                val becomesChild = if (existingNode.key >= pointer.key) existingNode else pointer
                val nextPointer = becomesChild.nextSibling!! // TODO: wants to be pointer

                siblingsToChildren(staysInRootList, becomesChild)
                nodeWithDegree[becomesChild.childCount] = null

                pointer = nextPointer
                continue
            }
            pointer = pointer.nextSibling!!
        } while (pointer !== firstRootNode)

        // check invariant
        if (debug) {
            if (rootListChildCounts().distinct().size != rootListChildCounts().size)
                throw Exception("Duplicate child count found in root list")
        }
    }

    private fun siblingsToChildren(staysInRootList: Node<Element>, becomesChild: Node<Element>) {
        if (debug) println("rebalance(staysAtRoot=$staysInRootList, becomesChild=$becomesChild)")
        staysInRootList.childCount++
        if (nodeWithDegree.size <= staysInRootList.childCount) nodeWithDegree = nodeWithDegree.copyOf(staysInRootList.childCount shl 1 + staysInRootList.childCount)
        deleteFromList(becomesChild)
        becomesChild.parent = staysInRootList
        if (staysInRootList.firstChild === null) {
            makeSiblingsInOrder(becomesChild, becomesChild)
        } else {
            val next = staysInRootList.firstChild!!
            val prev = next.previousSibling!!
            makeSiblingsInOrder(prev, becomesChild)
            makeSiblingsInOrder(becomesChild, next)
        }
        staysInRootList.firstChild = becomesChild
    }

    private fun makeSiblingsInOrder(firstNode: Node<Element>, nextNode: Node<Element>) {
        firstNode.nextSibling = nextNode
        nextNode.previousSibling = firstNode
    }

    // Test functions

    private val debug: Boolean = false
    internal fun entries(): Collection<Map.Entry<Int, Element>> =
        if (firstRootNode === null) emptyList()
        else buildList {
            fun yieldNodes(startNode: Node<Element>) {
                var node = startNode
                do {
                    add(node)
                    if (node.firstChild !== null) {
                        yieldNodes(node.firstChild!!)
                    }
                    node = node.nextSibling!!
                } while (node !== startNode)
            }
            yieldNodes(firstRootNode!!)
        }

    internal fun rootListChildCounts(): Collection<Int> =
        if (firstRootNode === null) emptyList()
        else buildList {
            var rootNode = firstRootNode!!
            do {
                add(rootNode.childCount)
                rootNode = rootNode.nextSibling!!
            } while (rootNode !== firstRootNode)
        }
}
