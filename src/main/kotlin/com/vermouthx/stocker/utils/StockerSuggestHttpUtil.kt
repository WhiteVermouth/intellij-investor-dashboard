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

    fun suggest(key: String, provider: StockerQuoteProvider): List<StockerSuggest> {
        if (key.contains(" ")) {
            return emptyList()
        }
        val url = "${provider.suggestHost}$key"
        val httpGet = HttpGet(url)
        return try {
            val response = httpClientPool.execute(httpGet)
            val responseText = EntityUtils.toString(response.entity, "UTF-8")
            parseSinaResponse(responseText)
        } catch (e: Exception) {
            log.warn(e)
            emptyList()
        }
    }

    private fun parseSinaResponse(responseText: String): List<StockerSuggest> {
        val result = mutableListOf<StockerSuggest>()
        val snippetsText = responseText
            .replace("var suggestvalue=\"", "")
            .replace("\";", "")
        if (snippetsText.isEmpty()) {
            return result
        }
        val snippets = snippetsText.split(";")
        snippets.forEach { snippet ->
            val columns = snippet.split(",")
            when (columns[1]) {
                "11" -> result.add(StockerSuggest(columns[3].toUpperCase(), columns[4], StockerMarketType.AShare))
                "22" -> {
                    val code = columns[3].replace("of", "")
                    when {
                        code.startsWith("15") || code.startsWith("16") || code.startsWith("18") ->
                            result.add(StockerSuggest("SZ$code", columns[4], StockerMarketType.AShare))
                        code.startsWith("50") || code.startsWith("51") ->
                            result.add(StockerSuggest("SH$code", columns[4], StockerMarketType.AShare))
                    }
                }
                "31" -> result.add(StockerSuggest(columns[3].toUpperCase(), columns[4], StockerMarketType.HKStocks))
                "41" -> result.add(StockerSuggest(columns[3].toUpperCase(), columns[4], StockerMarketType.USStocks))
            }
        }
        return result
    }
}