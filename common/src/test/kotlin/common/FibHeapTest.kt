package common

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*

class FibHeapTest {

    @Test
    fun `An empty heap pops null`() {
        val heap = FibHeap<Any>()
        assertThat(heap.poll()).isNull()
    }

    @Test
    fun `An empty heap that is offered and popped will preserve the value and return to an empty heap`() {
        val heap = FibHeap<Any>()
        heap.offer(Any(), 5)
        assertThat(heap.size).isEqualTo(1)
        assertThat(heap.entries().map { it.key }).containsExactlyInAnyOrder(5)

        val firstEntry = heap.pollEntry()
        assertThat(firstEntry!!.key).isEqualTo(5)
        assertThat(heap.size).isZero()
    }


    @Test
    fun `A heap receiving two offers will poll the lowest offer`() {
        val heap = FibHeap<Any>()
        heap.offer(Any(), 5)
        heap.offer(Any(), 10)
        assertThat(heap.size).isEqualTo(2)
        assertThat(heap.entries().map { it.key }).containsExactlyInAnyOrder(5, 10)

        val firstEntry = heap.pollEntry()
        assertThat(firstEntry!!.key).isEqualTo(5)
        assertThat(heap.size).isEqualTo(1)
    }

    @Test
    fun `A heap receiving three offers will poll the lowest offer`() {
        val heap = FibHeap<Any>()
        heap.offer(Any(), 10)
        heap.offer(Any(), 5)
        heap.offer(Any(), 15)
        assertThat(heap.size).isEqualTo(3)

        val firstEntry = heap.pollEntry()
        assertThat(firstEntry!!.key).isEqualTo(5)
        assertThat(heap.size).isEqualTo(2)
        assertThat(heap.entries().map { it.key }).containsExactlyInAnyOrder(10, 15)
    }

    data class TestObject(val s: String)

    @Test
    fun `Repositioning the highest key to be in the middle of the range preserves the other keys and the smallest key still polls`() {
        val heap = FibHeap<TestObject>()
        heap.offer(TestObject("10"), 10)
        heap.offer(TestObject("5"), 5)
        heap.offer(TestObject("15"), 15)
        assertThat(heap.size).isEqualTo(3)

        heap.offerOrReposition(TestObject("15"), 15, 8)
        assertThat(heap.size).isEqualTo(3)
        assertThat(heap.entries().map { it.key }).containsExactlyInAnyOrder(5, 8, 10)

        val firstEntry = heap.pollEntry()
        assertThat(firstEntry!!.key).isEqualTo(5)
        assertThat(heap.size).isEqualTo(2)
        assertThat(heap.entries().map { it.key }).containsExactlyInAnyOrder(8, 10)
    }
}
