package com.vermouthx.stocker.enums

enum class StockerMarketIndex(val codes: List<String>) {
    CN(listOf("SH000001", "SZ399001", "SZ399006")),
    HK(listOf("HSI", "HSTECH")),
    US(listOf("DJI", "IXIC", "INX")),
    Crypto(listOf("BTCBTCUSD"))  // Bitcoin/USD index (correct Sina code format)
}
