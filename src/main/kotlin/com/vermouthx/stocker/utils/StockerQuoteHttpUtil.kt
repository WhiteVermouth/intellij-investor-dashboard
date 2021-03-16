package com.vermouthx.stocker.utils

import com.intellij.openapi.diagnostic.Logger
import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

object StockerQuoteHttpUtil {

    private val log = Logger.getInstance(javaClass)

    private val httpClientPool = run {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 20
        val requestConfig = RequestConfig.custom().build()
        HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig)
            .useSystemProperties().build()
    }

    fun get(
        marketType: StockerMarketType,
        quoteProvider: StockerQuoteProvider,
        codes: List<String>
    ): List<StockerQuote> {
        if (codes.isEmpty()) {
            return emptyList()
        }
        val codesParam =
            if (isUpperCase(marketType, quoteProvider)) {
                codes.joinToString(",") { code ->
                    "${quoteProvider.providerPrefixMap[marketType]}${code.toUpperCase()}"
                }
            } else {
                codes.joinToString(",") { code ->
                    "${quoteProvider.providerPrefixMap[marketType]}${code.toLowerCase()}"
                }
            }
        val url = "${quoteProvider.host}${codesParam}"
        val httpGet = HttpGet(url)
        return try {
            val response = httpClientPool.execute(httpGet)
            val responseText = EntityUtils.toString(response.entity, "UTF-8")
            StockerQuoteParser.parse(quoteProvider, marketType, responseText)
        } catch (e: Exception) {
            log.warn(e)
            emptyList()
        }
    }

    private fun isUpperCase(marketType: StockerMarketType, quoteProvider: StockerQuoteProvider): Boolean {
        return marketType == StockerMarketType.HKStocks || (marketType == StockerMarketType.USStocks && quoteProvider == StockerQuoteProvider.TENCENT)
    }

}