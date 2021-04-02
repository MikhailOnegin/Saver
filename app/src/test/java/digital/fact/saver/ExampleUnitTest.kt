package digital.fact.saver

import digital.fact.saver.utils.toLongFormatter
import digital.fact.saver.utils.formatToMoney
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {

    @Test
    fun isLongToString_correct() {
        assertEquals("0.01", 1L.formatToMoney())
        assertEquals("0.10", 10L.formatToMoney())
        assertEquals("1.00", 100L.formatToMoney())
        assertEquals("10.00", 1000L.formatToMoney())
        assertEquals("100.00", 10000L.formatToMoney())
        assertEquals("1 000.00", 100000L.formatToMoney())
        assertEquals("10 000.00", 1000000L.formatToMoney())
        assertEquals("44 444.44", 4444444L.formatToMoney())
        assertEquals("444 444 400.00", 44444440000L.formatToMoney())
    }

    @Test
    fun isStringToLong_correct() {
        assertEquals(100L, "1.00".toLongFormatter())
        assertEquals(1L, "0.01".toLongFormatter())
        assertEquals(1232323L, "12323.23".toLongFormatter())
    }
}