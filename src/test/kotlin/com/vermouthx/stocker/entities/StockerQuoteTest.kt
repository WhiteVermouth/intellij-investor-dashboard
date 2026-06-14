package com.vermouthx.stocker.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StockerQuoteTest {

    private fun quote(code: String, current: Double = 1.0, name: String = "name") = StockerQuote(
        code = code,
        name = name,
        current = current,
        opening = 1.0,
        close = 1.0,
        low = 1.0,
        high = 1.0,
        change = 0.0,
        percentage = 0.0,
        updateAt = "2024-01-02 15:00:00"
    )

    @Test
    fun `quotes with the same code are equal regardless of other fields`() {
        assertEquals(quote("SH600519", current = 100.0), quote("SH600519", current = 200.0, name = "other"))
    }

    @Test
    fun `quotes with different codes are not equal`() {
        assertNotEquals(quote("SH600519"), quote("SZ000001"))
    }

    @Test
    fun `hashCode is derived from the code`() {
        assertEquals(quote("SH600519", current = 1.0).hashCode(), quote("SH600519", current = 2.0).hashCode())
    }

    @Test
    fun `code identity enables set deduplication`() {
        val set = hashSetOf(quote("AAPL", current = 1.0))
        assertFalse(set.add(quote("AAPL", current = 999.0)))
        assertTrue(set.add(quote("MSFT")))
    }
}
