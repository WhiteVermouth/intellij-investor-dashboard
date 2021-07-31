package com.vermouthx.stocker.entities

data class StockerQuote(
    var code: String, var name: String,
    var current: Double, var opening: Double, var close: Double,
    var low: Double, var high: Double, var change: Double, var percentage: Double,
    var buys: Array<Double> = emptyArray(), var sells: Array<Double> = emptyArray(),
    var updateAt: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StockerQuote

        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }
}