package com.vermouthx.stocker.utils

import javax.swing.table.DefaultTableModel
import kotlin.test.Test
import kotlin.test.assertEquals

class StockerTableModelUtilTest {

    private fun modelWithCodes(vararg codes: String): DefaultTableModel {
        val model = DefaultTableModel()
        model.addColumn("code")
        for (code in codes) {
            model.addRow(arrayOf<Any>(code))
        }
        return model
    }

    @Test
    fun `returns row index when code exists`() {
        val model = modelWithCodes("SH600519", "SZ000001", "AAPL")
        assertEquals(1, StockerTableModelUtil.existAt(model, "SZ000001"))
    }

    @Test
    fun `returns first row index`() {
        val model = modelWithCodes("SH600519", "SZ000001")
        assertEquals(0, StockerTableModelUtil.existAt(model, "SH600519"))
    }

    @Test
    fun `returns minus one when code is absent`() {
        val model = modelWithCodes("SH600519", "SZ000001")
        assertEquals(-1, StockerTableModelUtil.existAt(model, "AAPL"))
    }

    @Test
    fun `returns minus one for empty model`() {
        assertEquals(-1, StockerTableModelUtil.existAt(modelWithCodes(), "AAPL"))
    }

    @Test
    fun `matching is case sensitive`() {
        val model = modelWithCodes("AAPL")
        assertEquals(-1, StockerTableModelUtil.existAt(model, "aapl"))
    }
}
