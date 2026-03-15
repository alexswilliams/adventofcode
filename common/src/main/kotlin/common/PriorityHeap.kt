package common

interface PriorityHeap<Element> {
    fun offer(e: Element, weight: Int)
    fun offerOrReposition(e: Element, oldWeight: Int, newWeight: Int)
    fun poll(): Element?
    fun pollEntry(): Map.Entry<Int, Element>?
}
