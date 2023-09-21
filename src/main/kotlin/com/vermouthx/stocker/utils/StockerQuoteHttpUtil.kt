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
            val response = httpClientPool.execute(httpGet)
            val responseText = EntityUtils.toString(response.entity, "UTF-8")
            StockerQuoteParser.parseQuoteResponse(quoteProvider, marketType, responseText)
        } catch (e: Exception) {
            log.warn(e)
            emptyList()
        }
    }

    fun validateCode(
        marketType: StockerMarketType, quoteProvider: StockerQuoteProvider, code: String
    ): Boolean {
        when (quoteProvider) {
            StockerQuoteProvider.SINA -> {
                val url = if (marketType == StockerMarketType.HKStocks) {
                    "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                } else {
                    "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                }
                val httpGet = HttpGet(url)
                httpGet.setHeader("Referer", "https://finance.sina.com.cn") // Sina API requires this header
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

            StockerQuoteProvider.TENCENT -> {
                val url = if (marketType == StockerMarketType.HKStocks || marketType == StockerMarketType.USStocks) {
                    "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                } else {
                    "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                }
                val httpGet = HttpGet(url)
                val response = httpClientPool.execute(httpGet)
                val responseText = EntityUtils.toString(response.entity, "UTF-8")
                return !responseText.startsWith("v_pv_none_match")
            }
        }
    }
}
