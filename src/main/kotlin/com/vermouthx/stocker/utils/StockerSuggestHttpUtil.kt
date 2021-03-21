package com.vermouthx.stocker.utils

import com.intellij.openapi.diagnostic.Logger
import com.vermouthx.stocker.entities.StockerSuggest
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

object StockerSuggestHttpUtil {

    private val log = Logger.getInstance(javaClass)

    private val httpClientPool = run {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 20
        val requestConfig = RequestConfig.custom().build()
        HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig)
            .useSystemProperties().build()

    }

    fun suggest(key: String): List<StockerSuggest> {
        if (key.contains(" ")) {
            return emptyList()
        }
        val url = "${StockerQuoteProvider.SINA.suggestHost}$key"
        val httpGet = HttpGet(url)
        return try {
            val response = httpClientPool.execute(httpGet)
            val responseText = EntityUtils.toString(response.entity, "UTF-8")
            parse(responseText)
        } catch (e: Exception) {
            log.warn(e)
            emptyList()
        }
    }

    private fun parse(responseText: String): List<StockerSuggest> {
        val result = mutableListOf<StockerSuggest>()
        val startLoc = responseText.indexOfFirst { c -> c == '"' } + 1
        val endLoc = responseText.indexOfLast { c -> c == '"' }
        if (startLoc == endLoc) {
            return result
        }
        val snippets = responseText.subSequence(startLoc, endLoc).split(";")
        snippets.forEach { snippet ->
            val columns = snippet.split(",")
            when (columns[1]) {
                "11" -> result.add(StockerSuggest(columns[3].toUpperCase(), columns[4], StockerMarketType.AShare))
                "31" -> result.add(StockerSuggest(columns[3].toUpperCase(), columns[4], StockerMarketType.HKStocks))
                "41" -> result.add(StockerSuggest(columns[3].toUpperCase(), columns[4], StockerMarketType.USStocks))
            }
        }
        return result
    }
}