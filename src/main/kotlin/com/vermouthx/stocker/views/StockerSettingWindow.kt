package com.vermouthx.stocker.views

import com.intellij.openapi.options.Configurable
import com.vermouthx.stocker.settings.StockerSetting
import javax.swing.JComponent

class StockerSettingWindow : Configurable {

    companion object {
        val view = StockerSettingView()
        val setting = StockerSetting.instance
    }

    override fun createComponent(): JComponent? {
        return view.content
    }

    override fun isModified(): Boolean {
        val colorPattern = view.colorPattern
        return colorPattern != setting.quoteColorPattern
    }

    override fun apply() {
        setting.quoteColorPattern = view.colorPattern
    }

    override fun getDisplayName(): String {
        return "Stocker"
    }

    override fun reset() {
        view.resetColorPattern(setting.quoteColorPattern)
    }
}