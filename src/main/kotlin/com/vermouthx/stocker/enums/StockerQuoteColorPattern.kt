package com.vermouthx.stocker.enums

import com.vermouthx.stocker.StockerBundle

enum class StockerQuoteColorPattern(val titleKey: String) {
    RED_UP_GREEN_DOWN("color.pattern.rugd"),
    GREEN_UP_RED_DOWN("color.pattern.gurd"),
    NONE("color.pattern.none");

    val title: String
        get() = StockerBundle.message(titleKey)
}