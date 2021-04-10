package com.vermouthx.stocker.utils

import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object StockerQuoteParser {

    private fun Double.twoDigits(): Double {
        return (this * 100.0).roundToInt() / 100.0
    }

    fun parseSinaResponseText(marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        val regex = Regex("var hq_str_(\\w+?)=\"(.*?)\";")
        return responseText.split("\n").asSequence()
            .filter { text -> text.isNotEmpty() }
            .map { text ->
                val matchResult = regex.find(text)
                val (_, code, quote) = matchResult!!.groupValues
                "${code},${quote}"
            }
            .map { text -> text.split(",") }
            .map { textArray ->
                when (marketType) {
                    StockerMarketType.AShare -> {
                        val code = textArray[0].toUpperCase()
                        val name = textArray[1]
                        val opening = textArray[2].toDouble().twoDigits()
                        val close = textArray[3].toDouble().twoDigits()
                        val current = textArray[4].toDouble().twoDigits()
                        val high = textArray[5].toDouble().twoDigits()
                        val low = textArray[6].toDouble().twoDigits()
                        val change = (current - close).twoDigits()
                        val percentage = ((current - close) / close * 100).twoDigits()
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
                        val opening = textArray[3].toDouble().twoDigits()
                        val close = textArray[4].toDouble().twoDigits()
                        val high = textArray[5].toDouble().twoDigits()
                        val low = textArray[6].toDouble().twoDigits()
                        val current = textArray[7].toDouble().twoDigits()
                        val change = (current - close).twoDigits()
                        val percentage = textArray[9].toDouble().twoDigits()
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
                        val code = textArray[0].substring(3).toUpperCase()
                        val name = textArray[1]
                        val current = textArray[2].toDouble().twoDigits()
                        val updateAt = textArray[4]
                        val opening = textArray[6].toDouble().twoDigits()
                        val high = textArray[7].toDouble().twoDigits()
                        val low = textArray[8].toDouble().twoDigits()
                        val close = textArray[27].toDouble().twoDigits()
                        val change = (current - close).twoDigits()
                        val percentage = textArray[3].toDouble().twoDigits()
                        StockerQuote(
                            code = code, name = name,
                            current = current, opening = opening, close = close,
                            low = low, high = high, change = change, percentage = percentage,
                            updateAt = updateAt
                        )
                    }
                    StockerMarketType.Crypto -> {
                        val code = textArray[0].substring(4).toUpperCase()
                        val name = textArray[10]
                        val current = textArray[9].toDouble().twoDigits()
                        val low = textArray[8].toDouble().twoDigits()
                        val high = textArray[7].toDouble().twoDigits()
                        val opening = textArray[6].toDouble().twoDigits()
                        val change = (current - opening).twoDigits()
                        val percentage = ((current - opening) / opening * 100).twoDigits()
                        val updateAt = "${textArray[12]} ${textArray[1]}"
                        StockerQuote(
                            code = code, name = name,
                            current = current, opening = opening, close = current,
                            low = low, high = high, change = change, percentage = percentage,
                            updateAt = updateAt
                        )
                    }
                }
            }.toList()
    }

}