package com.vermouthx.stocker.utils

import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object StockerQuoteParser {

    private fun Double.twoDigits(): Double {
        return (this * 100.0).roundToInt() / 100.0
    }

    // A zero base (e.g. a listing that has not traded yet) would yield NaN, which roundToInt rejects.
    private fun percentChange(current: Double, base: Double): Double {
        return if (base == 0.0) 0.0 else ((current - base) / base * 100).twoDigits()
    }

    /**
     * Providers answer an unknown or delisted symbol with an empty or placeholder record in the
     * middle of an otherwise valid batch. Parse each line on its own and drop the ones that do not
     * fit, so one bad symbol cannot discard the quotes of every other symbol in the request.
     */
    private fun parseLines(responseText: String, parseLine: (String) -> StockerQuote): List<StockerQuote> {
        return responseText.split("\n").filter { text -> text.isNotBlank() }.mapNotNull { text ->
            runCatching { parseLine(text) }.getOrNull()
        }
    }

    fun parseQuoteResponse(
        provider: StockerQuoteProvider, marketType: StockerMarketType, responseText: String
    ): List<StockerQuote> {
        return when (provider) {
            StockerQuoteProvider.SINA -> parseSinaQuoteResponse(marketType, responseText)
            StockerQuoteProvider.TENCENT -> parseTencentQuoteResponse(marketType, responseText)
        }
    }

    private fun parseSinaQuoteResponse(marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        val regex = Regex("var hq_str_(\\w+?)=\"(.*?)\";")
        return parseLines(responseText) { text ->
            val (_, symbol, payload) = regex.find(text)!!.groupValues
            val textArray = "${symbol},${payload}".split(",")
            when (marketType) {
                StockerMarketType.AShare -> {
                    val code = textArray[0].uppercase()
                    val name = textArray[1]
                    val opening = textArray[2].toDouble()
                    val close = textArray[3].toDouble()
                    val current = textArray[4].toDouble()
                    val high = textArray[5].toDouble()
                    val low = textArray[6].toDouble()
                    val change = (current - close).twoDigits()
                    val percentage = percentChange(current, close)
                    val updateAt = textArray[31] + " " + textArray[32]
                    StockerQuote(
                        code = code,
                        name = name,
                        current = current,
                        opening = opening,
                        close = close,
                        low = low,
                        high = high,
                        change = change,
                        percentage = percentage,
                        updateAt = updateAt
                    )
                }

                StockerMarketType.HKStocks -> {
                    val code = textArray[0].substring(2).uppercase()
                    val name = textArray[2]
                    val opening = textArray[3].toDouble()
                    val close = textArray[4].toDouble()
                    val high = textArray[5].toDouble()
                    val low = textArray[6].toDouble()
                    val current = textArray[7].toDouble()
                    val change = (current - close).twoDigits()
                    val percentage = textArray[9].toDouble().twoDigits()
                    val sourceFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                    val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val datetime = LocalDateTime.parse(textArray[18] + " " + textArray[19], sourceFormatter)
                    val updateAt = targetFormatter.format(datetime)
                    StockerQuote(
                        code = code,
                        name = name,
                        current = current,
                        opening = opening,
                        close = close,
                        low = low,
                        high = high,
                        change = change,
                        percentage = percentage,
                        updateAt = updateAt
                    )
                }

                StockerMarketType.USStocks -> {
                    val code = textArray[0].substring(3).uppercase()
                    val name = textArray[1]
                    val current = textArray[2].toDouble()
                    val updateAt = textArray[4]
                    val opening = textArray[6].toDouble()
                    val high = textArray[7].toDouble()
                    val low = textArray[8].toDouble()
                    val close = textArray[27].toDouble()
                    val change = (current - close).twoDigits()
                    val percentage = textArray[3].toDouble().twoDigits()
                    StockerQuote(
                        code = code,
                        name = name,
                        current = current,
                        opening = opening,
                        close = close,
                        low = low,
                        high = high,
                        change = change,
                        percentage = percentage,
                        updateAt = updateAt
                    )
                }

                StockerMarketType.Crypto -> {
                    val code = textArray[0].substring(4).uppercase()
                    val name = textArray[10]
                    val current = textArray[9].toDouble()
                    val low = textArray[8].toDouble()
                    val high = textArray[7].toDouble()
                    val opening = textArray[6].toDouble()
                    val change = (current - opening).twoDigits()
                    val percentage = percentChange(current, opening)
                    val updateAt = "${textArray[12]} ${textArray[1]}"
                    StockerQuote(
                        code = code,
                        name = name,
                        current = current,
                        opening = opening,
                        close = current,
                        low = low,
                        high = high,
                        change = change,
                        percentage = percentage,
                        updateAt = updateAt
                    )
                }
            }
        }
    }

    private fun parseTencentQuoteResponse(marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        return parseLines(responseText) { text ->
            val symbol = when (marketType) {
                StockerMarketType.AShare -> text.subSequence(2, text.indexOfFirst { c -> c == '=' })
                StockerMarketType.HKStocks, StockerMarketType.USStocks -> text.subSequence(4,
                    text.indexOfFirst { c -> c == '=' })

                StockerMarketType.Crypto -> ""
            }
            val textArray =
                "$symbol~${text.subSequence(text.indexOfFirst { c -> c == '"' } + 1, text.indexOfLast { c -> c == '"' })}"
                    .split("~")
            val code = textArray[0].uppercase()
            val name = textArray[2]
            val opening = textArray[6].toDouble()
            val close = textArray[5].toDouble()
            val current = textArray[4].toDouble()
            val high = textArray[34].toDouble()
            val low = textArray[35].toDouble()
            val change = (current - close).twoDigits()
            val percentage = textArray[33].toDouble().twoDigits()
            val updateAt = when (marketType) {
                StockerMarketType.AShare -> {
                    val sourceFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val datetime = LocalDateTime.parse(textArray[31], sourceFormatter)
                    targetFormatter.format(datetime)
                }

                StockerMarketType.HKStocks -> {
                    val sourceFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                    val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val datetime = LocalDateTime.parse(textArray[31], sourceFormatter)
                    targetFormatter.format(datetime)
                }

                StockerMarketType.USStocks -> textArray[31]
                StockerMarketType.Crypto -> ""
            }
            StockerQuote(
                code = code,
                name = name,
                current = current,
                opening = opening,
                close = close,
                low = low,
                high = high,
                change = change,
                percentage = percentage,
                updateAt = updateAt
            )
        }
    }
}
