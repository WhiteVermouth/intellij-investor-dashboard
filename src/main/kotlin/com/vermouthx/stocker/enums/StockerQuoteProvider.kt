package com.vermouthx.stocker.enums

enum class StockerQuoteProvider(
    val title: String,
    val host: String,
    val suggestHost: String,
    val providerPrefixMap: Map<StockerMarketType, String>
) {
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
     * Tencent API is deprecated
     * Keep this item only for compatibility concern
     */
    TENCENT(
        title = "Tencent", host = "https://qt.gtimg.cn/q=",
        suggestHost = "https://smartbox.gtimg.cn/s3/?v=2&t=all&c=1&q=",
        providerPrefixMap = mapOf(
            StockerMarketType.AShare to "",
            StockerMarketType.HKStocks to "hk",
            StockerMarketType.USStocks to "us",
            StockerMarketType.Crypto to "btc_"
        )
    )
}