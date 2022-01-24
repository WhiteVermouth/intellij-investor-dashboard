package com.vermouthx.stocker.entities

import com.vermouthx.stocker.enums.StockerMarketType

data class StockerSuggestion(val code: String, val name: String, val market: StockerMarketType)
