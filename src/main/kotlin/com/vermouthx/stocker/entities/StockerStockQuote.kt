package com.vermouthx.stocker.entities

data class StockerStockQuote(
    var code: String,
    var name: String,
    var current: String,
    var opening: String,
    var close: String,
    var low: String,
    var high: String,
    var percentage: String,
    var updateAt: String
)