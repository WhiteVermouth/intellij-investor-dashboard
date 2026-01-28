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
        connectionManager.defaultMaxPerRoute = 10
        // Configure timeouts to prevent hanging requests
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(10000) // 10 seconds to establish connection
            .setSocketTimeout(15000) // 15 seconds to read data
            .setConnectionRequestTimeout(5000) // 5 seconds to get connection from pool
            .build()
        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .useSystemProperties()
            .build()
    }

    fun get(
        marketType: StockerMarketType, quoteProvider: StockerQuoteProvider, codes: List<String>
    ): List<StockerQuote> {
        if (codes.isEmpty()) {
            return emptyList()
        }
        val codesParam = when (quoteProvider) {
            StockerQuoteProvider.SINA -> {
                if (marketType == StockerMarketType.HKStocks) {
                    codes.joinToString(",") { code ->
                        "${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                    }
                } else {
                    codes.joinToString(",") { code ->
                        "${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                    }
                }
            }

            StockerQuoteProvider.TENCENT -> {
                if (marketType == StockerMarketType.HKStocks || marketType == StockerMarketType.USStocks) {
                    codes.joinToString(",") { code ->
                        "${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                    }
                } else {
                    codes.joinToString(",") { code ->
                        "${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                    }
                }
            }
        }

        val url = "${quoteProvider.host}${codesParam}"
        val httpGet = HttpGet(url)
        if (quoteProvider == StockerQuoteProvider.SINA) {
            httpGet.setHeader("Referer", "https://finance.sina.com.cn") // Sina API requires this header
        }
        return try {
            httpClientPool.execute(httpGet).use { response ->
                val responseText = EntityUtils.toString(response.entity, "UTF-8")
                StockerQuoteParser.parseQuoteResponse(quoteProvider, marketType, responseText)
            }
        } catch (e: Exception) {
            log.warn(e)
            emptyList()
        }
    }

    fun validateCode(
        marketType: StockerMarketType, quoteProvider: StockerQuoteProvider, code: String
    ): Boolean {
        return try {
            when (quoteProvider) {
                StockerQuoteProvider.SINA -> {
                    val url = if (marketType == StockerMarketType.HKStocks) {
                        "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                    } else {
                        "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                    }
                    val httpGet = HttpGet(url)
                    httpGet.setHeader("Referer", "https://finance.sina.com.cn") // Sina API requires this header
                    httpClientPool.execute(httpGet).use { response ->
                        val responseText = EntityUtils.toString(response.entity, "UTF-8")
                        val firstLine = responseText.split("\n")[0]
                        val start = firstLine.indexOfFirst { c -> c == '"' } + 1
                        val end = firstLine.indexOfLast { c -> c == '"' }
                        if (start == end) {
                            return false
                        }
                        firstLine.subSequence(start, end).contains(",")
                    }
                }

                StockerQuoteProvider.TENCENT -> {
                    val url = if (marketType == StockerMarketType.HKStocks || marketType == StockerMarketType.USStocks) {
                        "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                    } else {
                        "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                    }
                    val httpGet = HttpGet(url)
                    httpClientPool.execute(httpGet).use { response ->
                        val responseText = EntityUtils.toString(response.entity, "UTF-8")
                        !responseText.startsWith("v_pv_none_match")
                    }
                }
            }
        } catch (e: Exception) {
            log.warn(e)
            false
        }
    }
}
