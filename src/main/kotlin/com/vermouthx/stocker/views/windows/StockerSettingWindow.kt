package com.vermouthx.stocker.views.windows

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.settings.StockerSetting

class StockerSettingWindow : BoundConfigurable("Stocker") {

    private val setting = StockerSetting.instance

    private var colorPattern: StockerQuoteColorPattern = setting.quoteColorPattern
    private var quoteProviderTitle: String = setting.quoteProvider.title
    private var displayNameWithPinyin: Boolean = setting.displayNameWithPinyin

    override fun createPanel(): DialogPanel {
        return panel {
            group("Data Provider") {
                row {
                    label("Quote source:")
                        .widthGroup("labels")
                    comboBox(StockerQuoteProvider.values().map { it.title })
                        .bindItem(::quoteProviderTitle.toNullableProperty())
                        .comment("Select the data source for stock quotes")
                }.layout(RowLayout.LABEL_ALIGNED)
            }

            group("Display Settings") {
                buttonsGroup {
                    row {
                        label("Color pattern:")
                            .widthGroup("labels")
                    }
                    indent {
                        row {
                            radioButton("Red up, green down", StockerQuoteColorPattern.RED_UP_GREEN_DOWN)
                                .comment("Traditional Asian market color scheme")
                        }
                        row {
                            radioButton("Green up, red down", StockerQuoteColorPattern.GREEN_UP_RED_DOWN)
                                .comment("Western market color scheme")
                        }
                        row {
                            radioButton("No color coding", StockerQuoteColorPattern.NONE)
                                .comment("Display all values in default text color")
                        }
                    }
                }.bind(::colorPattern.toMutableProperty(), StockerQuoteColorPattern::class.java)

                separator()

                row {
                    label("Name format:")
                        .widthGroup("labels")
                }.layout(RowLayout.LABEL_ALIGNED)

                indent {
                    row {
                        checkBox("Convert Chinese names to Pinyin")
                            .bindSelected(::displayNameWithPinyin.toMutableProperty())
                    }.rowComment("Example: 平安银行 → PingAnYinHang")
                }
            }

            onApply {
                val wasModified = quoteProviderTitle != setting.quoteProvider.title ||
                        colorPattern != setting.quoteColorPattern ||
                        displayNameWithPinyin != setting.displayNameWithPinyin
                
                setting.quoteProvider = setting.quoteProvider.fromTitle(quoteProviderTitle)
                setting.quoteColorPattern = colorPattern
                setting.displayNameWithPinyin = displayNameWithPinyin
                
                // Refresh all active projects when settings change
                if (wasModified) {
                    refreshAllWindows()
                }
            }
            onIsModified {
                quoteProviderTitle != setting.quoteProvider.title ||
                        colorPattern != setting.quoteColorPattern ||
                        displayNameWithPinyin != setting.displayNameWithPinyin
            }
            onReset {
                quoteProviderTitle = setting.quoteProvider.title
                colorPattern = setting.quoteColorPattern
                displayNameWithPinyin = setting.displayNameWithPinyin
            }
        }
    }

    private fun refreshAllWindows() {
        // Restart all active applications to reload with new settings
        // This will clear tables and reload data with updated settings
        StockerAppManager.myApplicationMap.values.forEach { app ->
            app.shutdownThenClear()
            app.schedule()
        }
    }

}
