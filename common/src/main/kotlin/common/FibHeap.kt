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
            is ElementFinder.TreeWalker -> ElementFinderStrategy.TreeWalkLookup { e, weight -> findElement(e, firstRootNode, weight) }
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
    }

    private var firstRootNode: Node<Element>? = null
    private var minNode: Node<Element>? = null
    var size: Int = 0
        private set

    private fun offer(e: Element, weight: Int, offset: Int) {
        size++
        val next = firstRootNode
        val prev = firstRootNode?.previousSibling
        firstRootNode = Node(weight + offset, e, previousSibling = prev, nextSibling = next)
        elementFinderStrategy.link(e, weight + offset, firstRootNode!!)

        if (size == 1) {
            firstRootNode!!.previousSibling = firstRootNode
            firstRootNode!!.nextSibling = firstRootNode
        } else {
            prev!!.nextSibling = firstRootNode
            next!!.previousSibling = firstRootNode
        }

        if (minNode === null || minNode!!.key > firstRootNode!!.key)
            minNode = firstRootNode
    }

    override fun offer(e: Element, weight: Int) {
        offer(e, weight, weightOffset(e))
    }

    override fun offerOrReposition(e: Element, oldWeight: Int, newWeight: Int) {
        val offset = weightOffset(e)
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
            offer(e, newWeight)
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

    private fun cutSubTreeToRootList(node: Node<Element>) {
        deleteFromList(node)
        if (firstRootNode === null) {
            firstRootNode = node
            node.nextSibling = node
            node.previousSibling = node
            minNode = node
        } else {
            val next = firstRootNode!!
            val prev = firstRootNode!!.previousSibling!!
            firstRootNode = node
            node.nextSibling = next
            next.previousSibling = node
            node.previousSibling = prev
            prev.nextSibling = node
            if (minNode!!.key > node.key) minNode = node
        }
        val parent = node.parent
        node.parent = null
        node.marked = false

        if (parent !== null && parent.marked)
            cutSubTreeToRootList(parent)
        else if (parent !== null && parent.parent !== null)
            parent.marked = true
    }

    private fun findElement(e: Element, startNode: Node<Element>?, oldWeight: Int): Node<Element>? {
        if (startNode === null) return null
        var node: Node<Element> = startNode
        do {
            if (node.value === e) // TODO: might not be appropriate to use strict equal, might need to be comparison
                return node
            if (node.key > oldWeight) return null
            if (node.firstChild !== null) {
                val foundInChildren = findElement(e, node.firstChild!!, oldWeight)
                if (foundInChildren !== null) return foundInChildren
            }
            node = node.nextSibling!!
        } while (node !== startNode)
        return null
    }

    // Leaves children orphaned / to be dealt with by the caller
    private fun deleteFromList(nodeToDelete: Node<Element>) {
        if (nodeToDelete.nextSibling === nodeToDelete) { // List size 1
            assert(nodeToDelete.parent === null || nodeToDelete.parent!!.firstChild === nodeToDelete)
            nodeToDelete.parent?.firstChild = null
            if (firstRootNode === nodeToDelete) firstRootNode = null
            return
        }

        if (nodeToDelete.parent !== null) {
            nodeToDelete.parent!!.childCount--
            if (nodeToDelete.parent!!.firstChild === nodeToDelete)
                nodeToDelete.parent!!.firstChild = nodeToDelete.nextSibling
        }

        val prev = nodeToDelete.previousSibling!!
        val next = nodeToDelete.nextSibling!!
        nodeToDelete.nextSibling = null
        nodeToDelete.previousSibling = null
        prev.nextSibling = next
        next.previousSibling = prev
        if (firstRootNode === nodeToDelete) firstRootNode = next
    }

    private fun promoteChildrenToRootList(firstNode: Node<Element>) {
        val nodeAfterSublist = firstRootNode
        val lastNodeInRootList = firstRootNode?.previousSibling
        val lastNode = firstNode.previousSibling!!

        firstRootNode = firstNode
        if (nodeAfterSublist === null) return

        firstNode.previousSibling = lastNodeInRootList
        lastNodeInRootList!!.nextSibling = firstNode
        lastNode.nextSibling = nodeAfterSublist
        nodeAfterSublist.previousSibling = lastNode

        var n = firstNode
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


    override fun poll(): Element? = pollEntry()?.value

    override fun pollEntry(): Map.Entry<Int, Element>? {
        val nodeToPop = minNode ?: return null
        assert(nodeToPop.parent === null)

        deleteFromList(nodeToPop)
        elementFinderStrategy.unlink(nodeToPop.value, nodeToPop.key)

        if (nodeToPop.firstChild !== null)
            promoteChildrenToRootList(nodeToPop.firstChild!!)

        rebalance()

        findNewMinNode()

        size--
        return nodeToPop
    }

    private var nodeWithDegree = Array<Node<Element>?>(32) { null }

    private fun rebalance() {
        if (firstRootNode === null || firstRootNode!!.nextSibling === firstRootNode) return

        nodeWithDegree.fill(null)
        var node = firstRootNode!!
        do {
            val existingNode = nodeWithDegree[node.childCount]
            if (existingNode === null || existingNode.parent !== null) {
                nodeWithDegree[node.childCount] = node
            } else if (existingNode !== node) {
                val staysAtRoot = if (existingNode.key < node.key) existingNode else node
                val becomesChild = if (existingNode.key >= node.key) existingNode else node
                val next = becomesChild.nextSibling!!

                rebalance(staysAtRoot, becomesChild)
                nodeWithDegree[becomesChild.childCount] = null

                node = next
                continue
            }
            node = node.nextSibling!!
        } while (node !== firstRootNode)
    }

    private fun rebalance(staysAtRoot: Node<Element>, becomesChild: Node<Element>) {
        staysAtRoot.childCount++
        if (nodeWithDegree.size <= staysAtRoot.childCount) nodeWithDegree = Array(staysAtRoot.childCount shl 1 + staysAtRoot.childCount) { null }
        deleteFromList(becomesChild)
        becomesChild.parent = staysAtRoot
        if (staysAtRoot.firstChild === null) {
            becomesChild.previousSibling = becomesChild
            becomesChild.nextSibling = becomesChild
        } else {
            val next = staysAtRoot.firstChild!!
            val prev = next.previousSibling!!
            becomesChild.previousSibling = prev
            prev.nextSibling = becomesChild
            becomesChild.nextSibling = next
            next.previousSibling = becomesChild
        }
        staysAtRoot.firstChild = becomesChild
    }

    // Test functions

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
}
