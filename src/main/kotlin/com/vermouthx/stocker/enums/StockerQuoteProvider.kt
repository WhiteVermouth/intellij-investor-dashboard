package com.vermouthx.stocker.enums

enum class StockerQuoteProvider(
    val title: String,
    val host: String,
    val suggestHost: String,
    val providerPrefixMap: Map<StockerMarketType, String>
) {
    SINA(
        title = "Sina", host = "http://hq.sinajs.cn/list=",
        suggestHost = "http://suggest3.sinajs.cn/suggest/key=",
        providerPrefixMap = mapOf(
            StockerMarketType.AShare to "",
            StockerMarketType.HKStocks to "hk",
            StockerMarketType.USStocks to "gb_"
        )
    ),
    TENCENT(
        title = "Tencent", host = "http://qt.gtimg.cn/q=",
        suggestHost = "https://smartbox.gtimg.cn/s3/?v=2&t=all&c=1&q=",
        providerPrefixMap = mapOf(
            StockerMarketType.AShare to "",
            StockerMarketType.HKStocks to "hk",
            StockerMarketType.USStocks to "us"
        )
    )
}