package com.vermouthx.stocker.utils

import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider

object StockerQuoteParser {
    fun parse(provider: StockerQuoteProvider, marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        return when (provider) {
            StockerQuoteProvider.SINA -> parseSinaResponseText(marketType, responseText)
            StockerQuoteProvider.TENCENT -> parseTencentResponseText(marketType, responseText)
        }
    }

    private fun Double.round(): String {
        return String.format("%.2f", this)
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
                            val opening = textArray[2].toDouble()
                            val close = textArray[3].toDouble()
                            val current = textArray[4].toDouble()
                            val high = textArray[5].toDouble()
                            val low = textArray[6].toDouble()
                            val percentage = if (current > close) {
                                "+${((current - close) / close * 100).round()}%"
                            } else {
                                "${((current - close) / close * 100).round()}%"
                            }
                            val updateAt = textArray[31] + " " + textArray[32]
                            StockerQuote(
                                    code = code,
                                    name = name,
                                    current = current.round(),
                                    opening = opening.round(),
                                    close = close.round(),
                                    low = low.round(),
                                    high = high.round(),
                                    percentage = percentage,
                                    updateAt = updateAt
                            )
                        }
                        StockerMarketType.HKStocks -> {
                            val code = textArray[0].toUpperCase()
                            val name = textArray[2]
                            val opening = textArray[3].toDouble()
                            val close = textArray[4].toDouble()
                            val high = textArray[5].toDouble()
                            val low = textArray[6].toDouble()
                            val current = textArray[7].toDouble()
                            val percentage = if (textArray[9].startsWith("-")) {
                                "${textArray[9].toDouble().round()}%"
                            } else {
                                "+${textArray[9].toDouble().round()}%"
                            }
                            val updateAt = textArray[18] + " " + textArray[19]
                            StockerQuote(
                                    code = code,
                                    name = name,
                                    current = current.round(),
                                    opening = opening.round(),
                                    close = close.round(),
                                    low = low.round(),
                                    high = high.round(),
                                    percentage = percentage,
                                    updateAt = updateAt
                            )
                        }
                        StockerMarketType.USStocks -> {
                            val code = textArray[0].toUpperCase()
                            val name = textArray[1]
                            val current = textArray[2].toDouble()
                            val percentage = if (textArray[3].startsWith("-")) {
                                "${textArray[3]}%"
                            } else {
                                "+${textArray[3]}%"
                            }
                            val updateAt = textArray[4]
                            val opening = textArray[6].toDouble()
                            val high = textArray[7].toDouble()
                            val low = textArray[8].toDouble()
                            val close = textArray[27].toDouble()
                            StockerQuote(
                                    code = code,
                                    name = name,
                                    current = current.round(),
                                    opening = opening.round(),
                                    close = close.round(),
                                    low = low.round(),
                                    high = high.round(),
                                    percentage = percentage,
                                    updateAt = updateAt
                            )
                        }
                    }
                }.toList()
    }

    private fun parseTencentResponseText(marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        return emptyList()
    }
}