package com.vermouthx.stocker.entity

data class StockerStockEntity(
        var code: String,
        var name: String,
        var current: Double,
        var opening: Double,
        var close: Double,
        var low: Double,
        var high: Double,
        var percentage: Double,
        var time: String
)