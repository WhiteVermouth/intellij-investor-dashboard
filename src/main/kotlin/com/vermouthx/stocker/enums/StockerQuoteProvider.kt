package com.vermouthx.stocker.enums

enum class StockerQuoteProvider(
    val title: String,
    val host: String,
    val suggestHost: String,
    val providerPrefixMap: Map<StockerMarketType, String>
) {
    /**
     * Sina API is banned
     */
    SINA(
        title = "Sina", host = "https://hq.sinajs.cn/list=",
        suggestHost = "https://suggest3.sinajs.cn/suggest/key=",
        providerPrefixMap = mapOf(
            StockerMarketType.AShare to "",
            StockerMarketType.HKStocks to "hk",
            StockerMarketType.USStocks to "gb_",
            StockerMarketType.Crypto to "btc_"
        )
    ),

    /**
     * Tencent API
     */
    TENCENT(
        title = "Tencent", host = "https://qt.gtimg.cn/q=",
        suggestHost = "https://smartbox.gtimg.cn/s3/?v=2&t=all&c=1&q=",
        providerPrefixMap = mapOf(
            StockerMarketType.AShare to "",
            StockerMarketType.HKStocks to "hk",
            StockerMarketType.USStocks to "us",
        )
    ),

    /**
     * Snowball API
     */
    SNOWBALL(
        title = "Snowball", host = "https://stock.xueqiu.com/v5/stock/quote.json?symbol=",
        suggestHost = "https://xueqiu.com/stock/search.json?code=",
        providerPrefixMap = mapOf()
    )
}
