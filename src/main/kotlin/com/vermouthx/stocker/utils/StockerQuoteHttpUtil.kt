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
        val requestConfig = RequestConfig.custom().setConnectionRequestTimeout(1000).setSocketTimeout(1000).build()
        HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).build()
    }

    fun get(
        marketType: StockerMarketType,
        quoteProvider: StockerQuoteProvider,
        codes: List<String>
    ): List<StockerQuote>? {
        val codesParam = codes.joinToString(",") { code ->
            "${quoteProvider.providerPrefixMap[marketType]}${code.toLowerCase()}"
        }
        val url = "${quoteProvider.host}${codesParam}"
        val httpGet = HttpGet(url)
        return try {
            val response = httpClientPool.execute(httpGet)
            val responseText = EntityUtils.toString(response.entity, "UTF-8")
            StockerQuoteParser.parse(quoteProvider, marketType, responseText)
        } catch (e: Exception) {
            log.warn(e)
            null
        }
    }

    fun validateCode(
        marketType: StockerMarketType,
        quoteProvider: StockerQuoteProvider,
        code: String
    ): Boolean {
        val url = "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.toLowerCase()}"
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