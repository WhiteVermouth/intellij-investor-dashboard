package com.vermouthx.stocker.settings

import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.enums.StockerTableColumn

class StockerSettingState {
    var version: String = ""
    var refreshInterval: Long = 5
    var quoteProvider: StockerQuoteProvider = StockerQuoteProvider.SINA
    var cryptoQuoteProvider: StockerQuoteProvider = StockerQuoteProvider.SINA
    var quoteColorPattern: StockerQuoteColorPattern = StockerQuoteColorPattern.RED_UP_GREEN_DOWN
    var displayNameWithPinyin: Boolean = false
    var languageOverride: String = "" // Empty string means follow system language
    var visibleTableColumns: MutableList<String> = mutableListOf() // Empty list will be populated with defaults on first access
    var aShareList: MutableList<String> = mutableListOf()
    var hkStocksList: MutableList<String> = mutableListOf()
    var usStocksList: MutableList<String> = mutableListOf()
    var cryptoList: MutableList<String> = mutableListOf()
    var customStockNames: MutableMap<String, String> = mutableMapOf()
    var stockCostPrices: MutableMap<String, Double> = mutableMapOf()
    var stockHoldings: MutableMap<String, Int> = mutableMapOf()
}