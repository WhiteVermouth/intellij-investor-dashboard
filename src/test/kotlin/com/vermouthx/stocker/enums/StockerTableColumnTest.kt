package com.vermouthx.stocker.enums

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StockerTableColumnTest {

    @Test
    fun `fromName resolves a known enum name`() {
        assertEquals(StockerTableColumn.NET_PROFIT, StockerTableColumn.fromName("NET_PROFIT"))
    }

    @Test
    fun `fromName is case sensitive`() {
        assertNull(StockerTableColumn.fromName("net_profit"))
    }

    @Test
    fun `fromName returns null for an unknown name`() {
        assertNull(StockerTableColumn.fromName("UNKNOWN_COLUMN"))
    }

    @Test
    fun `default visible columns are name, current and change percent`() {
        assertEquals(
            listOf("NAME", "CURRENT", "CHANGE_PERCENT"),
            StockerTableColumn.defaultVisibleNames()
        )
    }

    @Test
    fun `every default visible name resolves back to an enum constant`() {
        for (name in StockerTableColumn.defaultVisibleNames()) {
            assertEquals(name, StockerTableColumn.fromName(name)?.name)
        }
    }
}
