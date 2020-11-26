package com.vermouthx.stocker.views

import com.intellij.openapi.options.Configurable
import com.vermouthx.stocker.settings.StockerSetting
import javax.swing.JComponent

class StockerSettingWindow : Configurable {

    private lateinit var view: StockerSettingView

    companion object {
        val setting = StockerSetting.instance
    }

    override fun createComponent(): JComponent? {
        view = StockerSettingView()
        view.resetQuoteProvider(setting.quoteProvider)
        return view.content
    }

    override fun isModified(): Boolean {
        val quoteColorPattern = view.selectedQuoteColorPattern
        val quoteProvider = view.selectedQuoteProvider
        return quoteProvider != setting.quoteProvider || quoteColorPattern != setting.quoteColorPattern
    }

    override fun apply() {
        setting.quoteProvider = view.selectedQuoteProvider
        setting.quoteColorPattern = view.selectedQuoteColorPattern
    }

    override fun getDisplayName(): String {
        return "Stocker"
    }

    override fun reset() {
        view.resetQuoteProvider(setting.quoteProvider)
        view.resetQuoteColorPattern(setting.quoteColorPattern)
    }
}