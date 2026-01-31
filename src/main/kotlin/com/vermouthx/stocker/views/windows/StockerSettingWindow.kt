package com.vermouthx.stocker.views.windows

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.*
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.enums.StockerTableColumn
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.views.StockerTableView
import javax.swing.JCheckBox
import javax.swing.JLabel

class StockerSettingWindow : BoundConfigurable("Stocker") {

    private val setting = StockerSetting.instance

    private var colorPattern: StockerQuoteColorPattern = setting.quoteColorPattern
    private var quoteProviderTitle: String = setting.quoteProvider.title
    private var cryptoQuoteProviderTitle: String = setting.cryptoQuoteProvider.title
    private var displayNameWithPinyin: Boolean = setting.displayNameWithPinyin
    private var showSymbol: Boolean = setting.isTableColumnVisible(StockerTableColumn.SYMBOL.title)
    private var showName: Boolean = setting.isTableColumnVisible(StockerTableColumn.NAME.title)
    private var showCurrent: Boolean = setting.isTableColumnVisible(StockerTableColumn.CURRENT.title)
    private var showChangePercent: Boolean = setting.isTableColumnVisible(StockerTableColumn.CHANGE_PERCENT.title)
    
    private var symbolCheckBox: JCheckBox? = null
    private var nameCheckBox: JCheckBox? = null
    private var currentCheckBox: JCheckBox? = null
    private var changePercentCheckBox: JCheckBox? = null
    private var columnWarningLabel: JLabel? = null

    override fun createPanel(): DialogPanel {
        return panel {
            group("Data Provider") {
                row {
                    label("Stock quote source:")
                        .widthGroup("labels")
                    comboBox(StockerQuoteProvider.values().map { it.title })
                        .bindItem(::quoteProviderTitle.toNullableProperty())
                        .widthGroup("comboboxes")
                        .comment("Select the data source for stock quotes (A-Share, HK, US)")
                }.layout(RowLayout.LABEL_ALIGNED)
                
                row {
                    label("Crypto quote source:")
                        .widthGroup("labels")
                    comboBox(listOf(StockerQuoteProvider.SINA.title))
                        .bindItem(::cryptoQuoteProviderTitle.toNullableProperty())
                        .widthGroup("comboboxes")
                        .comment("Crypto quotes are only available from Sina")
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

                separator()

                row {
                    label("Table columns:")
                        .widthGroup("labels")
                }.layout(RowLayout.LABEL_ALIGNED)

                indent {
                    row {
                        symbolCheckBox = checkBox(StockerTableColumn.SYMBOL.title)
                            .bindSelected(::showSymbol.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                        nameCheckBox = checkBox(StockerTableColumn.NAME.title)
                            .bindSelected(::showName.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        currentCheckBox = checkBox(StockerTableColumn.CURRENT.title)
                            .bindSelected(::showCurrent.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                        changePercentCheckBox = checkBox(StockerTableColumn.CHANGE_PERCENT.title)
                            .bindSelected(::showChangePercent.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        columnWarningLabel = label("Please keep at least one visible column.")
                            .applyToComponent { 
                                foreground = JBColor.RED
                                isVisible = false
                            }
                            .component
                    }
                }
            }

            onApply {
                val visibleColumns = buildVisibleColumns()
                val columnsModified = visibleColumns != setting.visibleTableColumns
                val colorPatternModified = colorPattern != setting.quoteColorPattern
                val providerModified = quoteProviderTitle != setting.quoteProvider.title
                val cryptoProviderModified = cryptoQuoteProviderTitle != setting.cryptoQuoteProvider.title
                val pinyinModified = displayNameWithPinyin != setting.displayNameWithPinyin

                setting.quoteProvider = StockerQuoteProvider.fromTitle(quoteProviderTitle)
                setting.cryptoQuoteProvider = StockerQuoteProvider.fromTitle(cryptoQuoteProviderTitle)
                setting.quoteColorPattern = colorPattern
                setting.displayNameWithPinyin = displayNameWithPinyin
                setting.visibleTableColumns = visibleColumns

                if (columnsModified) {
                    StockerTableView.refreshAllColumnVisibility()
                }
                if (colorPatternModified) {
                    StockerTableView.refreshAllColorPatterns()
                }
                // Refresh all active projects when quote provider or pinyin setting changes
                if (providerModified || cryptoProviderModified || pinyinModified) {
                    refreshAllWindows()
                }
            }
            onIsModified {
                quoteProviderTitle != setting.quoteProvider.title ||
                        cryptoQuoteProviderTitle != setting.cryptoQuoteProvider.title ||
                        colorPattern != setting.quoteColorPattern ||
                        displayNameWithPinyin != setting.displayNameWithPinyin ||
                        buildVisibleColumns() != setting.visibleTableColumns
            }
            onReset {
                quoteProviderTitle = setting.quoteProvider.title
                cryptoQuoteProviderTitle = setting.cryptoQuoteProvider.title
                colorPattern = setting.quoteColorPattern
                displayNameWithPinyin = setting.displayNameWithPinyin
                showSymbol = setting.isTableColumnVisible(StockerTableColumn.SYMBOL.title)
                showName = setting.isTableColumnVisible(StockerTableColumn.NAME.title)
                showCurrent = setting.isTableColumnVisible(StockerTableColumn.CURRENT.title)
                showChangePercent = setting.isTableColumnVisible(StockerTableColumn.CHANGE_PERCENT.title)
                columnWarningLabel?.isVisible = false
            }
        }
    }

    private fun buildVisibleColumns(): MutableList<String> {
        val visibleColumns = mutableListOf<String>()
        if (showSymbol) visibleColumns.add(StockerTableColumn.SYMBOL.title)
        if (showName) visibleColumns.add(StockerTableColumn.NAME.title)
        if (showCurrent) visibleColumns.add(StockerTableColumn.CURRENT.title)
        if (showChangePercent) visibleColumns.add(StockerTableColumn.CHANGE_PERCENT.title)
        return visibleColumns
    }

    private fun handleColumnToggle(changed: JCheckBox) {
        val allCheckboxes = listOfNotNull(symbolCheckBox, nameCheckBox, currentCheckBox, changePercentCheckBox)
        val selectedCount = allCheckboxes.count { it.isSelected }
        
        if (selectedCount == 0) {
            // Prevent unchecking the last checkbox
            changed.isSelected = true
            columnWarningLabel?.isVisible = true
        } else {
            columnWarningLabel?.isVisible = false
        }
    }

    private fun refreshAllWindows() {
        // Restart all active applications to reload with new settings
        // This will clear tables and reload data with updated settings
        StockerAppManager.getAllApplications().forEach { app ->
            app.shutdownThenClear()
            app.schedule()
        }
    }

}
