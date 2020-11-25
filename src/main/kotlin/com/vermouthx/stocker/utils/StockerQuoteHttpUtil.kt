package com.vermouthx.stocker.utils

import com.vermouthx.stocker.entities.StockerStockQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

object StockerQuoteHttpUtil {

    private val httpClientPool = run {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 20
        val requestConfig = RequestConfig.custom().setConnectionRequestTimeout(1000).setSocketTimeout(1000).build()
        HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).build()
    }

    private fun Double.round(): String {
        return String.format("%.2f", this)
    }

    fun get(
            marketType: StockerMarketType,
            quoteProvider: StockerQuoteProvider,
            codes: List<String>
    ): List<StockerStockQuote> {
        val result = mutableListOf<StockerStockQuote>()
        val url =
                "${quoteProvider.host}list=${codes.joinToString(",") { code -> "${quoteProvider.providerPrefixMap[marketType]}${code.toLowerCase()}" }}"
        val httpGet = HttpGet(url)
        val response = httpClientPool.execute(httpGet)
        val responseText = EntityUtils.toString(response.entity, "UTF-8")
        responseText.split("\n")
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
                            val code = textArray[0]
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
                            StockerStockQuote(
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
                            val code = textArray[0]
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
                            StockerStockQuote(
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
                            val code = textArray[0]
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
                            StockerStockQuote(
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
                }
                .forEach(result::add)
        return result
    }

    fun validateCode(
            marketType: StockerMarketType,
            quoteProvider: StockerQuoteProvider,
            code: String
    ): Boolean {
        val url = "${quoteProvider.host}list=${quoteProvider.providerPrefixMap[marketType]}${code.toLowerCase()}"
        val httpGet = HttpGet(url)
        val response = httpClientPool.execute(httpGet)
        val responseText = EntityUtils.toString(response.entity, "UTF-8")
        val firstLine = responseText.split("\n")[0]
        val start = firstLine.indexOfFirst { c -> c == '"' } + 1
        val end = firstLine.indexOfLast { c -> c == '"' }
        if (start == end) {
            return false
        }
        return firstLine.subSequence(start, end).contains(",")
    }

}