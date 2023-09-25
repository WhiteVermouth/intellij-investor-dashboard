package com.vermouthx.stocker.views.windows

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toMutableProperty
import com.intellij.ui.dsl.builder.toNullableProperty
import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.settings.StockerSetting

class StockerSettingWindow : BoundConfigurable("Stocker") {

    private val setting = StockerSetting.instance

    private var colorPattern: StockerQuoteColorPattern = setting.quoteColorPattern
    private var quoteProviderTitle: String = setting.quoteProvider.title

    override fun createPanel(): DialogPanel {
        return panel {
            group("General") {
                row("Provider: ") {
                    comboBox(
                        StockerQuoteProvider.values()
                            .map { it.title }).bindItem(::quoteProviderTitle.toNullableProperty())
                }
            }

            group("Appearance") {
                buttonsGroup("Color Pattern: ") {
                    row {
                        radioButton("Red up and green down", StockerQuoteColorPattern.RED_UP_GREEN_DOWN)
                    }
                    row {
                        radioButton("Green up and red down", StockerQuoteColorPattern.GREEN_UP_RED_DOWN)
                    }
                    row {
                        radioButton("None", StockerQuoteColorPattern.NONE)
                    }
                }.bind(::colorPattern.toMutableProperty(), StockerQuoteColorPattern::class.java)
            }

            onApply {
                setting.quoteProvider = setting.quoteProvider.fromTitle(quoteProviderTitle)
                setting.quoteColorPattern = colorPattern
            }
            onIsModified {
                quoteProviderTitle != setting.quoteProvider.title
                colorPattern != setting.quoteColorPattern
            }
            onReset {
                quoteProviderTitle = setting.quoteProvider.title
                colorPattern = setting.quoteColorPattern
            }
        }
    }

}
