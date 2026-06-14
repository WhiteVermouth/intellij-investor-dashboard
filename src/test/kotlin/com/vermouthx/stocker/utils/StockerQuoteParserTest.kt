package com.vermouthx.stocker.utils

import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StockerQuoteParserTest {

    /**
     * Build a delimited record where every field defaults to "0" and only the
     * given positions are overridden. This documents the exact column indices the
     * parser relies on without fragile hand-counted filler.
     */
    private fun record(size: Int, separator: String, vararg fields: Pair<Int, String>): String {
        val values = MutableList(size) { "0" }
        for ((index, value) in fields) {
            values[index] = value
        }
        return values.joinToString(separator)
    }

    @Test
    fun `parses Sina A-share quote`() {
        // Sina A-share fields (after the parser prepends the code):
        // [0]=code [1]=name [2]=open [3]=prevClose [4]=current [5]=high [6]=low [31]=date [32]=time
        // The quoted payload is the array without the code, so date/time live at [30]/[31].
        val payload = record(
            32, ",",
            0 to "贵州茅台",
            1 to "100.000", // opening
            2 to "99.000",  // close (previous close)
            3 to "110.000", // current
            4 to "115.000", // high
            5 to "98.000",  // low
            30 to "2024-01-02",
            31 to "15:00:00"
        )
        val response = "var hq_str_sh600519=\"$payload\";"

        val quotes = StockerQuoteParser.parseQuoteResponse(
            StockerQuoteProvider.SINA, StockerMarketType.AShare, response
        )

        assertEquals(1, quotes.size)
        val quote = quotes[0]
        assertEquals("SH600519", quote.code)
        assertEquals("贵州茅台", quote.name)
        assertEquals(110.0, quote.current)
        assertEquals(100.0, quote.opening)
        assertEquals(99.0, quote.close)
        assertEquals(115.0, quote.high)
        assertEquals(98.0, quote.low)
        assertEquals(11.0, quote.change)
        assertEquals(11.11, quote.percentage)
        assertEquals("2024-01-02 15:00:00", quote.updateAt)
    }

    @Test
    fun `parses Sina US-stock quote`() {
        // Sina US fields (after code prepend):
        // [0]=code [1]=name [2]=current [3]=percent [4]=updateAt [6]=open [7]=high [8]=low [27]=close
        // Quoted payload omits the code, shifting everything down by one.
        val payload = record(
            27, ",",
            0 to "Apple Inc",
            1 to "150.000", // current
            2 to "1.50",    // percentage
            3 to "2024-01-02 16:00:00", // updateAt
            5 to "148.000", // opening
            6 to "151.000", // high
            7 to "147.000", // low
            26 to "147.500" // close
        )
        val response = "var hq_str_gb_aapl=\"$payload\";"

        val quotes = StockerQuoteParser.parseQuoteResponse(
            StockerQuoteProvider.SINA, StockerMarketType.USStocks, response
        )

        assertEquals(1, quotes.size)
        val quote = quotes[0]
        assertEquals("AAPL", quote.code)
        assertEquals("Apple Inc", quote.name)
        assertEquals(150.0, quote.current)
        assertEquals(148.0, quote.opening)
        assertEquals(147.5, quote.close)
        assertEquals(151.0, quote.high)
        assertEquals(147.0, quote.low)
        assertEquals(2.5, quote.change)
        assertEquals(1.5, quote.percentage)
        assertEquals("2024-01-02 16:00:00", quote.updateAt)
    }

    @Test
    fun `parses Sina crypto quote`() {
        // Sina crypto fields (after code prepend):
        // [0]=code [6]=open [7]=high [8]=low [9]=current [10]=name; updateAt = [12] + " " + [1]
        // Quoted payload omits the code, shifting everything down by one.
        val payload = record(
            12, ",",
            0 to "15:00:00",   // time portion of updateAt
            5 to "40000.000",  // opening
            6 to "42000.000",  // high
            7 to "39000.000",  // low
            8 to "41000.000",  // current
            9 to "Bitcoin",    // name
            11 to "2024-01-02" // date portion of updateAt
        )
        val response = "var hq_str_btc_btcusd=\"$payload\";"

        val quotes = StockerQuoteParser.parseQuoteResponse(
            StockerQuoteProvider.SINA, StockerMarketType.Crypto, response
        )

        assertEquals(1, quotes.size)
        val quote = quotes[0]
        assertEquals("BTCUSD", quote.code)
        assertEquals("Bitcoin", quote.name)
        assertEquals(41000.0, quote.current)
        assertEquals(40000.0, quote.opening)
        // For crypto the parser uses current as the close value.
        assertEquals(41000.0, quote.close)
        assertEquals(42000.0, quote.high)
        assertEquals(39000.0, quote.low)
        assertEquals(1000.0, quote.change)
        assertEquals(2.5, quote.percentage)
        assertEquals("2024-01-02 15:00:00", quote.updateAt)
    }

    @Test
    fun `parses Tencent A-share quote`() {
        // Tencent A-share fields (after code prepend):
        // [0]=code [2]=name [4]=current [5]=close [6]=open [31]=date [33]=percent [34]=high [35]=low
        // The payload between the quotes omits the code, shifting everything down by one.
        val payload = record(
            35, "~",
            0 to "1",        // market flag
            1 to "贵州茅台", // name
            2 to "600519",   // numeric code
            3 to "1700.00",  // current
            4 to "1680.00",  // close
            5 to "1690.00",  // open
            30 to "20240102150000", // date (yyyyMMddHHmmss)
            32 to "1.19",    // percentage
            33 to "1710.00", // high
            34 to "1670.00"  // low
        )
        val response = "v_sh600519=\"$payload\";"

        val quotes = StockerQuoteParser.parseQuoteResponse(
            StockerQuoteProvider.TENCENT, StockerMarketType.AShare, response
        )

        assertEquals(1, quotes.size)
        val quote = quotes[0]
        assertEquals("SH600519", quote.code)
        assertEquals("贵州茅台", quote.name)
        assertEquals(1700.0, quote.current)
        assertEquals(1690.0, quote.opening)
        assertEquals(1680.0, quote.close)
        assertEquals(1710.0, quote.high)
        assertEquals(1670.0, quote.low)
        assertEquals(20.0, quote.change)
        assertEquals(1.19, quote.percentage)
        assertEquals("2024-01-02 15:00:00", quote.updateAt)
    }

    @Test
    fun `parses multiple Sina lines and skips blank lines`() {
        val first = record(
            32, ",",
            0 to "平安银行", 1 to "10.000", 2 to "10.000", 3 to "11.000",
            4 to "11.500", 5 to "9.800", 30 to "2024-01-02", 31 to "15:00:00"
        )
        val second = record(
            32, ",",
            0 to "万科A", 1 to "20.000", 2 to "20.000", 3 to "19.000",
            4 to "21.000", 5 to "18.500", 30 to "2024-01-02", 31 to "15:00:00"
        )
        val response = buildString {
            append("var hq_str_sz000001=\"$first\";\n")
            append("\n") // blank line must be ignored
            append("var hq_str_sz000002=\"$second\";\n")
        }

        val quotes = StockerQuoteParser.parseQuoteResponse(
            StockerQuoteProvider.SINA, StockerMarketType.AShare, response
        )

        assertEquals(2, quotes.size)
        assertEquals("SZ000001", quotes[0].code)
        assertEquals("SZ000002", quotes[1].code)
    }

    @Test
    fun `returns empty list for blank response`() {
        val quotes = StockerQuoteParser.parseQuoteResponse(
            StockerQuoteProvider.SINA, StockerMarketType.AShare, ""
        )
        assertTrue(quotes.isEmpty())
    }
}
