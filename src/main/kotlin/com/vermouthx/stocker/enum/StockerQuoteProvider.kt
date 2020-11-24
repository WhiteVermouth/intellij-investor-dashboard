package com.vermouthx.stocker.enum

enum class StockerQuoteProvider(val host: String, val providerPrefixMap: Map<StockerMarketType, String>) {
    SINA("http://hq.sinajs.cn/",
            mapOf(StockerMarketType.AShare to "",
                    StockerMarketType.HKStocks to "hk",
                    StockerMarketType.USStocks to "gb_"
            )
    ),
    TENCENT("http://qt.gtimg.cn/",
            mapOf(StockerMarketType.AShare to "",
                    StockerMarketType.HKStocks to "r_hk",
                    StockerMarketType.USStocks to "s_us"
            )
    )
}