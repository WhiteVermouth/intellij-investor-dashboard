package com.vermouthx.stocker.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider

@State(name = "Stocker", storages = [Storage("stocker-config.xml")])
class StockerSetting : PersistentStateComponent<StockerSettingState> {
    private var myState = StockerSettingState()

    private val log = Logger.getInstance(javaClass)

    companion object {
        val instance
            get() = service<StockerSetting>()
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
            log.info("Stocker quote provider switched to ${value.title}")
        }

    var quoteColorPattern: StockerQuoteColorPattern
        get() = myState.quoteColorPattern
        set(value) {
            myState.quoteColorPattern = value
            log.info("Stocker quote color pattern switched to ${value.title}")
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

    override fun getState(): StockerSettingState? {
        return myState
    }

    override fun loadState(state: StockerSettingState) {
        myState = state
    }

}