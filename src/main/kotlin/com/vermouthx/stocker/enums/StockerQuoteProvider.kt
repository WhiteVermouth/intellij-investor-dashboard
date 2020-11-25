package com.vermouthx.stocker.enums

enum class StockerQuoteProvider(val title: String, val host: String, val providerPrefixMap: Map<StockerMarketType, String>) {
    SINA("Sina", "http://hq.sinajs.cn/",
            mapOf(StockerMarketType.AShare to "",
                    StockerMarketType.HKStocks to "hk",
                    StockerMarketType.USStocks to "gb_"
            )
    ),
    TENCENT("Tencent", "http://qt.gtimg.cn/",
            mapOf(StockerMarketType.AShare to "",
                    StockerMarketType.HKStocks to "r_hk",
                    StockerMarketType.USStocks to "s_us"
            )
    )
}