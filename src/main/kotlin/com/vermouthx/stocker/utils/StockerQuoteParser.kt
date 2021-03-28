package com.vermouthx.stocker.utils

import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object StockerQuoteParser {
    fun parse(provider: StockerQuoteProvider, marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        return when (provider) {
            StockerQuoteProvider.SINA -> parseSinaResponseText(marketType, responseText)
            StockerQuoteProvider.TENCENT -> parseTencentResponseText(marketType, responseText)
        }
    }

    private fun Double.round(): Double {
        return (this * 100.0).roundToInt() / 100.0
    }

    private fun parseSinaResponseText(marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        return responseText.split("\n")
            .asSequence()
            .filter { text -> text.isNotEmpty() }
            .map { text ->
                val code = text.subSequence(text.indexOfLast { c -> c == '_' } + 1, text.indexOfFirst { c -> c == '=' })
                val start = text.indexOfFirst { c -> c == '"' } + 1
                val end = text.indexOfLast { c -> c == '"' }
                "${code},${text.subSequence(start, end)}"
            }
            .map { text -> text.split(",") }
            .map { textArray ->
                when (marketType) {
                    StockerMarketType.AShare -> {
                        val code = textArray[0].toUpperCase()
                        val name = textArray[1]
                        val opening = textArray[2].toDouble().round()
                        val close = textArray[3].toDouble().round()
                        val current = textArray[4].toDouble().round()
                        val high = textArray[5].toDouble().round()
                        val low = textArray[6].toDouble().round()
                        val change = (current - close).round()
                        val percentage = ((current - close) / close * 100).round()
                        val updateAt = textArray[31] + " " + textArray[32]
                        StockerQuote(
                            code = code, name = name,
                            current = current, opening = opening, close = close,
                            low = low, high = high, change = change, percentage = percentage,
                            updateAt = updateAt
                        )
                    }
                    StockerMarketType.HKStocks -> {
                        val code = textArray[0].substring(2).toUpperCase()
                        val name = textArray[2]
                        val opening = textArray[3].toDouble().round()
                        val close = textArray[4].toDouble().round()
                        val high = textArray[5].toDouble().round()
                        val low = textArray[6].toDouble().round()
                        val current = textArray[7].toDouble().round()
                        val change = (current - close).round()
                        val percentage = textArray[9].toDouble().round()
                        val sourceFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                        val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val datetime = LocalDateTime.parse(textArray[18] + " " + textArray[19], sourceFormatter)
                        val updateAt = targetFormatter.format(datetime)
                        StockerQuote(
                            code = code, name = name,
                            current = current, opening = opening, close = close,
                            low = low, high = high, change = change, percentage = percentage,
                            updateAt = updateAt
                        )
                    }
                    StockerMarketType.USStocks -> {
                        val code = textArray[0].toUpperCase()
                        val name = textArray[1]
                        val current = textArray[2].toDouble().round()
                        val updateAt = textArray[4]
                        val opening = textArray[6].toDouble().round()
                        val high = textArray[7].toDouble().round()
                        val low = textArray[8].toDouble().round()
                        val close = textArray[27].toDouble().round()
                        val change = (current - close).round()
                        val percentage = textArray[3].toDouble().round()
                        StockerQuote(
                            code = code, name = name,
                            current = current, opening = opening, close = close,
                            low = low, high = high, change = change, percentage = percentage,
                            updateAt = updateAt
                        )
                    }
                }
            }.toList()
    }

    private fun parseTencentResponseText(marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        return responseText.split("\n")
            .asSequence()
            .filter { text -> text.isNotEmpty() }
            .map { text ->
                val code = when (marketType) {
                    StockerMarketType.AShare -> text.subSequence(2, text.indexOfFirst { c -> c == '=' })
                    StockerMarketType.HKStocks, StockerMarketType.USStocks -> text.subSequence(
                        4,
                        text.indexOfFirst { c -> c == '=' })
                }
                "$code~${text.subSequence(text.indexOfFirst { c -> c == '"' } + 1, text.indexOfLast { c -> c == '"' })}"
            }
            .map { text -> text.split("~") }
            .map { textArray ->
                val code = textArray[0].toUpperCase()
                val name = textArray[2]
                val opening = textArray[6].toDouble().round()
                val close = textArray[5].toDouble().round()
                val current = textArray[4].toDouble().round()
                val high = textArray[34].toDouble().round()
                val low = textArray[35].toDouble().round()
                val change = (current - close).round()
                val percentage = textArray[33].toDouble().round()
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
                }
                StockerQuote(
                    code = code, name = name,
                    current = current, opening = opening, close = close,
                    low = low, high = high, change = change, percentage = percentage,
                    updateAt = updateAt
                )
            }
            .toList()
    }
}