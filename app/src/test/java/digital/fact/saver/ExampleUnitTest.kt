package digital.fact.saver

import digital.fact.saver.utils.toStringFormatter
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun isLongToString_correct() {
        assertEquals("0.01", 1L.toStringFormatter())
        assertEquals("0.10", 10L.toStringFormatter())
        assertEquals("1.00", 100L.toStringFormatter())
    }
}