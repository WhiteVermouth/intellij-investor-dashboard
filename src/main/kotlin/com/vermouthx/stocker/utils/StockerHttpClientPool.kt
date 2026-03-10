package com.vermouthx.stocker.utils

import com.intellij.openapi.diagnostic.Logger
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager

internal class StockerHttpClientPool(
    private val log: Logger,
) {

    @Volatile
    private var connectionManager: PoolingHttpClientConnectionManager? = null

    @Volatile
    private var httpClient: CloseableHttpClient? = null

    @Synchronized
    fun client(): CloseableHttpClient {
        httpClient?.let { return it }

        val connectionManager = PoolingHttpClientConnectionManager().apply {
            maxTotal = 20
            defaultMaxPerRoute = 10
        }
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(10000)
            .setSocketTimeout(15000)
            .setConnectionRequestTimeout(5000)
            .build()
        val httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .useSystemProperties()
            .build()

        this.connectionManager = connectionManager
        this.httpClient = httpClient
        return httpClient
    }

    @Synchronized
    fun close() {
        val client = httpClient
        val manager = connectionManager
        httpClient = null
        connectionManager = null

        runCatching { client?.close() }
            .onFailure(log::warn)
        runCatching { manager?.shutdown() }
            .onFailure(log::warn)
    }
}
