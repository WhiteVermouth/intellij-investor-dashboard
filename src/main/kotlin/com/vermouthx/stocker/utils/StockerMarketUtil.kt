package com.vermouthx.stocker.utils

import com.vermouthx.stocker.enum.StockerMarketProvider
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

object StockerMarketUtil {

    private val httpClientPool = run {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 20
        val requestConfig = RequestConfig.custom().setConnectionRequestTimeout(100).setSocketTimeout(500).build()
        HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).build()
    }

    fun get(marketProvider: StockerMarketProvider, codes: List<String>): String? {
        val url = "${marketProvider.host}list=" + codes.joinToString(",")
        val httpGet = HttpGet(url)
        val response = httpClientPool.execute(httpGet)
        return EntityUtils.toString(response.entity)
    }
}