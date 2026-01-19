package com.vermouthx.stocker.enums

enum class StockerTableColumn(val title: String) {
    SYMBOL("Symbol"),
    NAME("Name"),
    CURRENT("Current"),
    CHANGE_PERCENT("Change%");

    companion object {
        @JvmStatic
        fun defaultTitles(): List<String> = values().map { it.title }
    }
}
