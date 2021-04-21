package digital.fact.saver

import digital.fact.saver.utils.toLongFormatter
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {

    @Test
    fun isStringToLong_correct() {
        assertEquals(100L, "1.00".toLongFormatter())
        assertEquals(1L, "0.01".toLongFormatter())
        assertEquals(1232323L, "12323.23".toLongFormatter())
    }

}