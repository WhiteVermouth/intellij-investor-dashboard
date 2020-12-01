package com.vermouthx.stocker.enums

enum class StockerQuoteProvider(val title: String, val host: String, val providerPrefixMap: Map<StockerMarketType, String>) {
    SINA("Sina", "http://hq.sinajs.cn/list=",
            mapOf(StockerMarketType.AShare to "",
                    StockerMarketType.HKStocks to "hk",
                    StockerMarketType.USStocks to "gb_"
            )
    ),
    TENCENT("Tencent", "http://qt.gtimg.cn/q=",
            mapOf(StockerMarketType.AShare to "",
                    StockerMarketType.HKStocks to "hk",
                    StockerMarketType.USStocks to "us"
            )
    )
}