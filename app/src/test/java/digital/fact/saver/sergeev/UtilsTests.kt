package digital.fact.saver.sergeev

import digital.fact.saver.utils.formatToMoney
import digital.fact.saver.utils.getLongSumFromString
import org.junit.Test
import org.junit.Assert.*

class UtilsTests {

    @Test
    fun getLongSumFromStringWorksCorrectly() {
        assertEquals(1, getLongSumFromString("0.01"))
        assertEquals(10, getLongSumFromString("0.1"))
        assertEquals(100, getLongSumFromString("1"))
        assertEquals(10000000000, getLongSumFromString("100000000"))
        assertEquals(10000000080, getLongSumFromString("100000000.8"))
        assertEquals(10000000089, getLongSumFromString("100000000.89"))
        assertEquals(10000000, getLongSumFromString("100 000"))
    }

    @Test
    fun isFormatToMoneyCorrectWithSpaces() {
        assertEquals("0.01", 1L.formatToMoney())
        assertEquals("0.10", 10L.formatToMoney())
        assertEquals("1.00", 100L.formatToMoney())
        assertEquals("10.00", 1000L.formatToMoney())
        assertEquals("100.00", 10000L.formatToMoney())
        assertEquals("1 000.00", 100000L.formatToMoney())
        assertEquals("10 000.00", 1000000L.formatToMoney())
        assertEquals("44 444.44", 4444444L.formatToMoney())
        assertEquals("444 444 400.00", 44444440000L.formatToMoney())
        assertEquals("- 0.01", (-1L).formatToMoney())
        assertEquals("- 0.10", (-10L).formatToMoney())
        assertEquals("- 1.00", (-100L).formatToMoney())
        assertEquals("- 10.00", (-1000L).formatToMoney())
        assertEquals("- 100.00", (-10000L).formatToMoney())
        assertEquals("- 1 000.00", (-100000L).formatToMoney())
        assertEquals("- 10 000.00", (-1000000L).formatToMoney())
        assertEquals("- 44 444.44", (-4444444L).formatToMoney())
        assertEquals("- 444 444 400.00", (-44444440000L).formatToMoney())
    }

    @Test
    fun isFormatToMoneyCorrectWithoutSpaces() {
        assertEquals("0.01", 1L.formatToMoney(false))
        assertEquals("0.10", 10L.formatToMoney(false))
        assertEquals("1.00", 100L.formatToMoney(false))
        assertEquals("10.00", 1000L.formatToMoney(false))
        assertEquals("100.00", 10000L.formatToMoney(false))
        assertEquals("1000.00", 100000L.formatToMoney(false))
        assertEquals("10000.00", 1000000L.formatToMoney(false))
        assertEquals("44444.44", 4444444L.formatToMoney(false))
        assertEquals("444444400.00", 44444440000L.formatToMoney(false))
        assertEquals("- 0.01", (-1L).formatToMoney(false))
        assertEquals("- 0.10", (-10L).formatToMoney(false))
        assertEquals("- 1.00", (-100L).formatToMoney(false))
        assertEquals("- 10.00", (-1000L).formatToMoney(false))
        assertEquals("- 100.00", (-10000L).formatToMoney(false))
        assertEquals("- 1000.00", (-100000L).formatToMoney(false))
        assertEquals("- 10000.00", (-1000000L).formatToMoney(false))
        assertEquals("- 44444.44", (-4444444L).formatToMoney(false))
        assertEquals("- 444444400.00", (-44444440000L).formatToMoney(false))
    }

}