package com.vermouthx.stocker.views

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.buttonGroup
import com.intellij.ui.layout.panel
import com.intellij.util.containers.map2Array
import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.settings.StockerSetting
import javax.swing.DefaultComboBoxModel

class StockerSettingWindow : BoundConfigurable("Stocker") {

    companion object {
        val setting = StockerSetting.instance
    }

    private var colorPattern: StockerQuoteColorPattern = setting.quoteColorPattern
    private var quoteProvider: String = setting.quoteProvider.title

    override fun createPanel(): DialogPanel {
        return panel {
            titledRow("General") {
                row {
                    cell {
                        label("Provider: ")
                        comboBox(
                            DefaultComboBoxModel(StockerQuoteProvider.values().map2Array { it.title }), ::quoteProvider
                        ).enabled(false)
                    }
                }
            }
            titledRow("Appearance") {
                row {
                    label("Color Pattern: ")
                    buttonGroup(::colorPattern) {
                        row {
                            radioButton("Red up and green down", StockerQuoteColorPattern.RED_UP_GREEN_DOWN)
                        }
                        row {
                            radioButton("Green up and red down", StockerQuoteColorPattern.GREEN_UP_RED_DOWN)
                        }
                        row {
                            radioButton("None", StockerQuoteColorPattern.NONE)
                        }
                    }
                }
            }.onGlobalIsModified {
                colorPattern != setting.quoteColorPattern
            }.onGlobalReset {
                colorPattern = setting.quoteColorPattern
            }.onGlobalApply {
                setting.quoteColorPattern = colorPattern
            }
        }
    }

}