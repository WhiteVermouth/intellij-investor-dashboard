package com.vermouthx.stocker.enums

enum class StockerTableColumn(val title: String) {
    SYMBOL("Symbol"),
    NAME("Name"),
    CURRENT("Current"),
    OPENING("Opening"),
    CLOSE("Close"),
    LOW("Low"),
    HIGH("High"),
    CHANGE("Change"),
    CHANGE_PERCENT("Change%");

    companion object {
        @JvmStatic
        fun defaultTitles(): List<String> = values().map { it.title }
        
        @JvmStatic
        fun defaultVisibleTitles(): List<String> = listOf(
            NAME.title,
            CURRENT.title,
            CHANGE_PERCENT.title
        )
    }
}
