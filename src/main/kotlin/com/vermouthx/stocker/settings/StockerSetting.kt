package com.vermouthx.stocker.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.vermouthx.stocker.enum.StockerQuoteProvider

@State(name = "Stocker", storages = [Storage("stocker-config.xml")])
class StockerSetting : PersistentStateComponent<StockerSettingState> {
    private var myState = StockerSettingState()

    companion object {
        val instance
            get() = service<StockerSetting>()
    }

    var version: String
        get() = myState.version
        set(value) {
            myState.version = value
        }

    var quoteProvider: StockerQuoteProvider
        get() = myState.quoteProvider
        set(value) {
            myState.quoteProvider = value
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