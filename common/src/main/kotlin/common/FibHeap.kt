package common

import java.util.*

class FibHeap<Element>(
    initialValues: Iterable<Pair<Element, Int>>,
    private val weightOffset: (Element) -> Int = { 0 },
) : PriorityHeap<Element> {
    init {
        initialValues.forEach { offer(it.first, it.second) }
    }

    constructor(weightOffset: (Element) -> Int = { 0 }) : this(emptyList(), weightOffset)
    constructor(initialValue: Pair<Element, Int>, weightOffset: (Element) -> Int = { 0 }) : this(listOf(initialValue), weightOffset)

    data class Node<Element>(
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

    override fun offer(e: Element, weight: Int) {
        size++
        val next = firstRootNode
        val prev = firstRootNode?.previousSibling
        firstRootNode = Node(weight, e, previousSibling = prev, nextSibling = next)

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

    override fun offerOrReposition(e: Element, oldWeight: Int, newWeight: Int) {
        if (size == 0) {
            offer(e, newWeight)
            return
        }

        // TODO: this is super naive
        val foundNode = findElement(e, firstRootNode!!, oldWeight)
        if (foundNode === null) {
            offer(e, newWeight)
            return
        }

        if (foundNode.parent === null && newWeight < oldWeight) {
            foundNode.key = newWeight
            if (minNode!!.key > newWeight)
                minNode = foundNode
        } else if (foundNode.parent!!.key < newWeight && newWeight < oldWeight) {
            foundNode.key = newWeight
        } else {
            deleteFromList(foundNode)
            size--
            if (foundNode.firstChild !== null) {
                promoteToRootList(foundNode.firstChild!!)
                findNewMinNode()
            }
            offer(e, newWeight)
        }
    }

    fun findElement(e: Element, startNode: Node<Element>, oldWeight: Int): Node<Element>? {
        var node = startNode
        do {
            if (node.value == e)
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

    private fun promoteToRootList(firstNode: Node<Element>) {
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
        if (nodeToPop.firstChild !== null)
            promoteToRootList(nodeToPop.firstChild!!)

        // Rebalance
        rebalance()

        findNewMinNode()

        size--
        return nodeToPop
    }

    private fun rebalance() {
        if (firstRootNode === null || firstRootNode!!.nextSibling === firstRootNode) return

        var nodeWithDegree = TreeMap<Int, Node<Element>>()
        var node = firstRootNode!!
        var done = false
        do {
            val existingNode = nodeWithDegree[node.childCount]
            if (existingNode === null) {
                nodeWithDegree[node.childCount] = node
            } else {
                rebalance(existingNode, node)

                node = firstRootNode!!
                nodeWithDegree = TreeMap()
                continue;
            }

            node = node.nextSibling!!
            if (node === firstRootNode) done = true
        } while (!done)
    }

    private fun rebalance(nodeA: Node<Element>, nodeB: Node<Element>) {
        val staysAsRoot = if (nodeA.key < nodeB.key) nodeA else nodeB
        val becomesChild = if (nodeA.key >= nodeB.key) nodeA else nodeB
        staysAsRoot.childCount++
        deleteFromList(becomesChild)
        becomesChild.parent = staysAsRoot
        if (staysAsRoot.firstChild === null) {
            becomesChild.previousSibling = becomesChild
            becomesChild.nextSibling = becomesChild
        } else {
            val next = staysAsRoot.firstChild!!
            val prev = next.previousSibling!!
            becomesChild.previousSibling = prev
            prev.nextSibling = becomesChild
            becomesChild.nextSibling = next
            next.previousSibling = becomesChild
        }
        staysAsRoot.firstChild = becomesChild
    }

    // Test functions

    fun entries(): Collection<Map.Entry<Int, Element>> =
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
