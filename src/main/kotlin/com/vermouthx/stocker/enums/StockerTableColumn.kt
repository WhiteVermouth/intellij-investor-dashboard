package com.vermouthx.stocker.enums

import com.vermouthx.stocker.StockerBundle
import java.util.*

enum class StockerTableColumn(val titleKey: String) {
    SYMBOL("column.symbol"),
    NAME("column.name"),
    CURRENT("column.current"),
    OPENING("column.opening"),
    CLOSE("column.close"),
    LOW("column.low"),
    HIGH("column.high"),
    CHANGE("column.change"),
    CHANGE_PERCENT("column.change.percent"),
    COST_PRICE("column.cost.price"),
    HOLDINGS("column.holdings");

    val title: String
        get() = StockerBundle.message(titleKey)

    companion object {
        @JvmStatic
        fun defaultTitles(): List<String> = entries.map { it.title }

        @JvmStatic
        fun defaultVisibleNames(): List<String> = listOf(
            NAME.name,
            CURRENT.name,
            CHANGE_PERCENT.name
        )

        @JvmStatic
        fun fromName(enumName: String): StockerTableColumn? = entries.find { it.name == enumName }

        /**
         * Migrate a localized title (from any known locale) to its enum name.
         * Returns null if no match is found.
         */
        @JvmStatic
        fun migrateLocalizedTitle(title: String): String? {
            entries.find { it.title == title }?.let { return it.name }
            val knownLocales = listOf(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)
            for (locale in knownLocales) {
                try {
                    val bundle = ResourceBundle.getBundle(
                        "messages.StockerBundle", locale,
                        StockerTableColumn::class.java.classLoader
                    )
                    entries.find { bundle.getString(it.titleKey) == title }?.let { return it.name }
                } catch (_: Exception) {
                    // skip
                }
            }
            return null
        }
    }
}
