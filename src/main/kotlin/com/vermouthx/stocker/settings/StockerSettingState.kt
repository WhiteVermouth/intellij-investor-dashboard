package com.vermouthx.stocker.settings

import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.enums.StockerTableColumn

class StockerSettingState {
    var version: String = ""
    var refreshInterval: Long = 5
    var quoteProvider: StockerQuoteProvider = StockerQuoteProvider.SINA
    var quoteColorPattern: StockerQuoteColorPattern = StockerQuoteColorPattern.RED_UP_GREEN_DOWN
    var displayNameWithPinyin: Boolean = false
    var visibleTableColumns: MutableList<String> = StockerTableColumn.defaultTitles().toMutableList()
    var aShareList: MutableList<String> = mutableListOf()
    var hkStocksList: MutableList<String> = mutableListOf()
    var usStocksList: MutableList<String> = mutableListOf()
    var cryptoList: MutableList<String> = mutableListOf()
    var customStockNames: MutableMap<String, String> = mutableMapOf()
}