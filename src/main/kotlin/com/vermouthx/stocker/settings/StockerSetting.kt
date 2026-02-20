package com.vermouthx.stocker.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.enums.StockerTableColumn
import com.vermouthx.stocker.utils.StockerPinyinUtil

@State(name = "Stocker", storages = [Storage("stocker-config.xml")])
class StockerSetting : PersistentStateComponent<StockerSettingState> {
    private var myState = StockerSettingState()

    private val log = Logger.getInstance(javaClass)

    companion object {
        val instance: StockerSetting
            get() = ApplicationManager.getApplication().getService(StockerSetting::class.java)
    }

    var version: String
        get() = myState.version
        set(value) {
            myState.version = value
            log.info("Stocker updated to $value")
        }

    var quoteProvider: StockerQuoteProvider
        get() = myState.quoteProvider
        set(value) {
            myState.quoteProvider = value
            log.info("Stocker stock quote provider switched to ${value.title}")
        }

    var cryptoQuoteProvider: StockerQuoteProvider
        get() = myState.cryptoQuoteProvider
        set(value) {
            myState.cryptoQuoteProvider = value
            log.info("Stocker crypto quote provider switched to ${value.title}")
        }

    var quoteColorPattern: StockerQuoteColorPattern
        get() = myState.quoteColorPattern
        set(value) {
            myState.quoteColorPattern = value
            log.info("Stocker quote color pattern switched to ${value.title}")
        }

    var displayNameWithPinyin: Boolean
        get() = myState.displayNameWithPinyin
        set(value) {
            myState.displayNameWithPinyin = value
            log.info("Stocker display name with pinyin set to $value")
        }

    var languageOverride: String
        get() = myState.languageOverride
        set(value) {
            myState.languageOverride = value
            log.info("Stocker language override set to $value")
        }

    var visibleTableColumns: List<String>
        get() {
            val stored = myState.visibleTableColumns
            if (stored.isEmpty()) return StockerTableColumn.defaultVisibleNames()
            if (stored.all { name -> StockerTableColumn.fromName(name) != null }) return stored
            val migrated = stored.mapNotNull { StockerTableColumn.migrateLocalizedTitle(it) }
            if (migrated.isNotEmpty()) {
                myState.visibleTableColumns = migrated.toMutableList()
                log.info("Migrated visibleTableColumns from localized titles to enum names: $migrated")
                return migrated
            }
            return StockerTableColumn.defaultVisibleNames()
        }
        set(value) {
            myState.visibleTableColumns = value.toMutableList()
            log.info("Stocker visible table columns updated: $value")
        }

    var refreshInterval: Long
        get() = myState.refreshInterval
        set(value) {
            myState.refreshInterval = value
            log.info("Stocker refresh interval set to $value")
        }

    var aShareList: MutableList<String>
        get() = myState.aShareList
        set(value) {
            myState.aShareList = value
        }

    var hkStocksList: MutableList<String>
        get() = myState.hkStocksList
        set(value) {
            myState.hkStocksList = value
        }

    var usStocksList: MutableList<String>
        get() = myState.usStocksList
        set(value) {
            myState.usStocksList = value
        }

    var cryptoList: MutableList<String>
        get() = myState.cryptoList
        set(value) {
            myState.cryptoList = value
        }

    var customStockNames: MutableMap<String, String>
        get() = myState.customStockNames
        set(value) {
            myState.customStockNames = value
        }

    var stockCostPrices: MutableMap<String, Double>
        get() = myState.stockCostPrices
        set(value) {
            myState.stockCostPrices = value
        }

    var stockHoldings: MutableMap<String, Int>
        get() = myState.stockHoldings
        set(value) {
            myState.stockHoldings = value
        }

    val allStockListSize: Int
        get() = aShareList.size + hkStocksList.size + usStocksList.size + cryptoList.size

    fun setCustomName(code: String, customName: String) {
        customStockNames[code] = customName
        log.info("Custom name set for $code: $customName")
    }

    fun getCustomName(code: String): String? {
        return customStockNames[code]
    }

    fun removeCustomName(code: String) {
        customStockNames.remove(code)
        log.info("Custom name removed for $code")
    }

    fun setCostPrice(code: String, costPrice: Double) {
        val rounded = Math.round(costPrice * 1000.0) / 1000.0
        stockCostPrices[code] = rounded
        log.info("Cost price set for $code: $rounded")
    }

    fun getCostPrice(code: String): Double? {
        return stockCostPrices[code]
    }

    fun removeCostPrice(code: String) {
        stockCostPrices.remove(code)
        log.info("Cost price removed for $code")
    }

    fun setHoldings(code: String, holdings: Int) {
        stockHoldings[code] = holdings
        log.info("Holdings set for $code: $holdings")
    }

    fun getHoldings(code: String): Int? {
        return stockHoldings[code]
    }

    fun removeHoldings(code: String) {
        stockHoldings.remove(code)
        log.info("Holdings removed for $code")
    }

    fun getDisplayName(code: String, originalName: String): String {
        // Priority: Custom name > Pinyin mode > Original name
        customStockNames[code]?.let { return it }
        if (displayNameWithPinyin) {
            return StockerPinyinUtil.toPinyin(originalName)
        }
        return originalName
    }

    fun isTableColumnVisible(column: StockerTableColumn): Boolean {
        return visibleTableColumns.contains(column.name)
    }

    fun containsCode(code: String): Boolean {
        return aShareList.contains(code) ||
                hkStocksList.contains(code) ||
                usStocksList.contains(code) ||
                cryptoList.contains(code)
    }

    fun marketOf(code: String): StockerMarketType? {
        if (aShareList.contains(code)) {
            return StockerMarketType.AShare
        }
        if (hkStocksList.contains(code)) {
            return StockerMarketType.HKStocks
        }
        if (usStocksList.contains(code)) {
            return StockerMarketType.USStocks
        }
        if (cryptoList.contains(code)) {
            return StockerMarketType.Crypto
        }
        return null
    }

    fun removeCode(market: StockerMarketType, code: String) {
        when (market) {
            StockerMarketType.AShare -> {
                synchronized(aShareList) {
                    aShareList.remove(code)
                }
            }

            StockerMarketType.HKStocks -> {
                synchronized(hkStocksList) {
                    hkStocksList.remove(code)
                }
            }

            StockerMarketType.USStocks -> {
                synchronized(usStocksList) {
                    usStocksList.remove(code)
                }
            }

            StockerMarketType.Crypto -> {
                synchronized(cryptoList) {
                    cryptoList.remove(code)
                }
            }
        }
    }

    override fun getState(): StockerSettingState {
        return myState
    }

    override fun loadState(state: StockerSettingState) {
        myState = state
    }

}
