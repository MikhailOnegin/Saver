package digital.fact.saver.sergeev

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

}