package com.vermouthx.stocker.settings

import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider

class StockerSettingState {
    var version: String = ""
    var quoteProvider: StockerQuoteProvider = StockerQuoteProvider.SINA
    var quoteColorPattern: StockerQuoteColorPattern = StockerQuoteColorPattern.RED_UP_GREEN_DOWN
    var refreshInterval: Long = 1000
    var aShareList: MutableList<String> = mutableListOf("SZ000050", "SZ002475", "SZ002511")
    var hkStocksList: MutableList<String> = mutableListOf()
    var usStocksList: MutableList<String> = mutableListOf("AAPL", "GOOGL", "MSFT")
}