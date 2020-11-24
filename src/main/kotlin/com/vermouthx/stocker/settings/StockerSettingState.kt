package com.vermouthx.stocker.settings

import com.vermouthx.stocker.enum.StockerQuoteProvider

class StockerSettingState {
    var version: String = ""
    var quoteProvider: StockerQuoteProvider = StockerQuoteProvider.SINA
    var aShareList: MutableList<String> = mutableListOf()
    var hkStocksList: MutableList<String> = mutableListOf()
    var usStocksList: MutableList<String> = mutableListOf()
}