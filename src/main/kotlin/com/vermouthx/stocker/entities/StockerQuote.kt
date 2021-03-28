package com.vermouthx.stocker.entities

data class StockerQuote(
    var code: String, var name: String,
    var current: Double, var opening: Double, var close: Double,
    var low: Double, var high: Double, var change: Double, var percentage: Double,
    var updateAt: String
)