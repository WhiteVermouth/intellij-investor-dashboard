package com.vermouthx.stocker.views.windows

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.StockerBundle
import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider
import com.vermouthx.stocker.enums.StockerTableColumn
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.views.StockerTableView
import javax.swing.JCheckBox
import javax.swing.JLabel

class StockerSettingWindow : BoundConfigurable(StockerBundle.message("plugin.name")) {

    private val setting = StockerSetting.instance

    private var colorPattern: StockerQuoteColorPattern = setting.quoteColorPattern
    private var selectedProvider: StockerQuoteProvider = setting.quoteProvider
    private var selectedCryptoProvider: StockerQuoteProvider = setting.cryptoQuoteProvider
    private var displayNameWithPinyin: Boolean = setting.displayNameWithPinyin
    private var languageOverride: String = setting.languageOverride
    private var showSymbol: Boolean = setting.isTableColumnVisible(StockerTableColumn.SYMBOL)
    private var showName: Boolean = setting.isTableColumnVisible(StockerTableColumn.NAME)
    private var showCurrent: Boolean = setting.isTableColumnVisible(StockerTableColumn.CURRENT)
    private var showOpening: Boolean = setting.isTableColumnVisible(StockerTableColumn.OPENING)
    private var showClose: Boolean = setting.isTableColumnVisible(StockerTableColumn.CLOSE)
    private var showLow: Boolean = setting.isTableColumnVisible(StockerTableColumn.LOW)
    private var showHigh: Boolean = setting.isTableColumnVisible(StockerTableColumn.HIGH)
    private var showChange: Boolean = setting.isTableColumnVisible(StockerTableColumn.CHANGE)
    private var showChangePercent: Boolean = setting.isTableColumnVisible(StockerTableColumn.CHANGE_PERCENT)
    private var showCostPrice: Boolean = setting.isTableColumnVisible(StockerTableColumn.COST_PRICE)
    private var showHoldings: Boolean = setting.isTableColumnVisible(StockerTableColumn.HOLDINGS)

    private var symbolCheckBox: JCheckBox? = null
    private var nameCheckBox: JCheckBox? = null
    private var currentCheckBox: JCheckBox? = null
    private var openingCheckBox: JCheckBox? = null
    private var closeCheckBox: JCheckBox? = null
    private var lowCheckBox: JCheckBox? = null
    private var highCheckBox: JCheckBox? = null
    private var changeCheckBox: JCheckBox? = null
    private var changePercentCheckBox: JCheckBox? = null
    private var costPriceCheckBox: JCheckBox? = null
    private var holdingsCheckBox: JCheckBox? = null
    private var columnWarningLabel: JLabel? = null

    companion object {
        private val LANGUAGE_CODES = listOf("", "en", "zh_CN")

        private fun languageDisplayName(code: String): String = when (code) {
            "" -> StockerBundle.message("settings.language.system")
            "en" -> StockerBundle.message("settings.language.english")
            "zh_CN" -> StockerBundle.message("settings.language.chinese")
            else -> code
        }
    }

    override fun createPanel(): DialogPanel {
        val providerRenderer = SimpleListCellRenderer.create<StockerQuoteProvider>("") { it.title }
        val languageRenderer = SimpleListCellRenderer.create<String>("") { languageDisplayName(it) }

        return panel {
            group(StockerBundle.message("settings.group.general")) {
                row {
                    label(StockerBundle.message("settings.language"))
                        .widthGroup("labels")
                    comboBox(LANGUAGE_CODES, languageRenderer)
                        .bindItem(
                            { languageOverride },
                            { languageOverride = it ?: "" }
                        )
                        .widthGroup("comboboxes")
                        .comment(StockerBundle.message("settings.language.comment"))
                }.layout(RowLayout.LABEL_ALIGNED)
            }

            group(StockerBundle.message("settings.group.data.provider")) {
                row {
                    label(StockerBundle.message("settings.stock.quote.source"))
                        .widthGroup("labels")
                    comboBox(StockerQuoteProvider.entries.toList(), providerRenderer)
                        .bindItem(::selectedProvider.toNullableProperty())
                        .widthGroup("comboboxes")
                        .comment(StockerBundle.message("settings.stock.quote.source.comment"))
                }.layout(RowLayout.LABEL_ALIGNED)

                row {
                    label(StockerBundle.message("settings.crypto.quote.source"))
                        .widthGroup("labels")
                    comboBox(listOf(StockerQuoteProvider.SINA), providerRenderer)
                        .bindItem(::selectedCryptoProvider.toNullableProperty())
                        .widthGroup("comboboxes")
                        .comment(StockerBundle.message("settings.crypto.quote.source.comment"))
                }.layout(RowLayout.LABEL_ALIGNED)
            }

            group(StockerBundle.message("settings.group.table.display")) {
                buttonsGroup {
                    row {
                        label(StockerBundle.message("settings.color.pattern"))
                            .widthGroup("labels")
                    }
                    indent {
                        row {
                            radioButton(StockerBundle.message("settings.color.pattern.red.up"), StockerQuoteColorPattern.RED_UP_GREEN_DOWN)
                                .comment(StockerBundle.message("settings.color.pattern.red.up.comment"))
                        }
                        row {
                            radioButton(StockerBundle.message("settings.color.pattern.green.up"), StockerQuoteColorPattern.GREEN_UP_RED_DOWN)
                                .comment(StockerBundle.message("settings.color.pattern.green.up.comment"))
                        }
                        row {
                            radioButton(StockerBundle.message("settings.color.pattern.none"), StockerQuoteColorPattern.NONE)
                                .comment(StockerBundle.message("settings.color.pattern.none.comment"))
                        }
                    }
                }.bind(::colorPattern.toMutableProperty(), StockerQuoteColorPattern::class.java)

                row {
                    label(StockerBundle.message("settings.name.format"))
                        .widthGroup("labels")
                }.layout(RowLayout.LABEL_ALIGNED)

                indent {
                    row {
                        checkBox(StockerBundle.message("settings.name.format.pinyin"))
                            .bindSelected(::displayNameWithPinyin.toMutableProperty())
                    }.rowComment(StockerBundle.message("settings.name.format.pinyin.comment"))
                }

                row {
                    label(StockerBundle.message("settings.table.columns"))
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
                    }
                    row {
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
                    }
                    row {
                        openingCheckBox = checkBox(StockerTableColumn.OPENING.title)
                            .bindSelected(::showOpening.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        closeCheckBox = checkBox(StockerTableColumn.CLOSE.title)
                            .bindSelected(::showClose.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        lowCheckBox = checkBox(StockerTableColumn.LOW.title)
                            .bindSelected(::showLow.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        highCheckBox = checkBox(StockerTableColumn.HIGH.title)
                            .bindSelected(::showHigh.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        changeCheckBox = checkBox(StockerTableColumn.CHANGE.title)
                            .bindSelected(::showChange.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        changePercentCheckBox = checkBox(StockerTableColumn.CHANGE_PERCENT.title)
                            .bindSelected(::showChangePercent.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        costPriceCheckBox = checkBox(StockerTableColumn.COST_PRICE.title)
                            .bindSelected(::showCostPrice.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        holdingsCheckBox = checkBox(StockerTableColumn.HOLDINGS.title)
                            .bindSelected(::showHoldings.toMutableProperty())
                            .applyToComponent {
                                addItemListener { handleColumnToggle(this) }
                            }
                            .component
                    }
                    row {
                        columnWarningLabel = label(StockerBundle.message("settings.table.columns.warning"))
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
                val providerModified = selectedProvider != setting.quoteProvider
                val cryptoProviderModified = selectedCryptoProvider != setting.cryptoQuoteProvider
                val pinyinModified = displayNameWithPinyin != setting.displayNameWithPinyin
                val languageModified = languageOverride != setting.languageOverride

                setting.quoteProvider = selectedProvider
                setting.cryptoQuoteProvider = selectedCryptoProvider
                setting.quoteColorPattern = colorPattern
                setting.displayNameWithPinyin = displayNameWithPinyin
                setting.visibleTableColumns = visibleColumns
                setting.languageOverride = languageOverride

                if (columnsModified || languageModified) {
                    StockerTableView.refreshAllColumnVisibility()
                }
                if (colorPatternModified) {
                    StockerTableView.refreshAllColorPatterns()
                }
                if (providerModified || cryptoProviderModified || pinyinModified || languageModified) {
                    refreshAllWindows()
                }
            }
            onIsModified {
                selectedProvider != setting.quoteProvider ||
                        selectedCryptoProvider != setting.cryptoQuoteProvider ||
                        colorPattern != setting.quoteColorPattern ||
                        displayNameWithPinyin != setting.displayNameWithPinyin ||
                        languageOverride != setting.languageOverride ||
                        buildVisibleColumns() != setting.visibleTableColumns
            }
            onReset {
                selectedProvider = setting.quoteProvider
                selectedCryptoProvider = setting.cryptoQuoteProvider
                colorPattern = setting.quoteColorPattern
                displayNameWithPinyin = setting.displayNameWithPinyin
                languageOverride = setting.languageOverride
                showSymbol = setting.isTableColumnVisible(StockerTableColumn.SYMBOL)
                showName = setting.isTableColumnVisible(StockerTableColumn.NAME)
                showCurrent = setting.isTableColumnVisible(StockerTableColumn.CURRENT)
                showOpening = setting.isTableColumnVisible(StockerTableColumn.OPENING)
                showClose = setting.isTableColumnVisible(StockerTableColumn.CLOSE)
                showLow = setting.isTableColumnVisible(StockerTableColumn.LOW)
                showHigh = setting.isTableColumnVisible(StockerTableColumn.HIGH)
                showChange = setting.isTableColumnVisible(StockerTableColumn.CHANGE)
                showChangePercent = setting.isTableColumnVisible(StockerTableColumn.CHANGE_PERCENT)
                showCostPrice = setting.isTableColumnVisible(StockerTableColumn.COST_PRICE)
                showHoldings = setting.isTableColumnVisible(StockerTableColumn.HOLDINGS)
                columnWarningLabel?.isVisible = false
            }
        }
    }

    private fun buildVisibleColumns(): MutableList<String> {
        val visibleColumns = mutableListOf<String>()
        if (showSymbol) visibleColumns.add(StockerTableColumn.SYMBOL.name)
        if (showName) visibleColumns.add(StockerTableColumn.NAME.name)
        if (showCurrent) visibleColumns.add(StockerTableColumn.CURRENT.name)
        if (showOpening) visibleColumns.add(StockerTableColumn.OPENING.name)
        if (showClose) visibleColumns.add(StockerTableColumn.CLOSE.name)
        if (showLow) visibleColumns.add(StockerTableColumn.LOW.name)
        if (showHigh) visibleColumns.add(StockerTableColumn.HIGH.name)
        if (showChange) visibleColumns.add(StockerTableColumn.CHANGE.name)
        if (showChangePercent) visibleColumns.add(StockerTableColumn.CHANGE_PERCENT.name)
        if (showCostPrice) visibleColumns.add(StockerTableColumn.COST_PRICE.name)
        if (showHoldings) visibleColumns.add(StockerTableColumn.HOLDINGS.name)
        return visibleColumns
    }

    private fun handleColumnToggle(changed: JCheckBox) {
        val allCheckboxes = listOfNotNull(
            symbolCheckBox,
            nameCheckBox,
            currentCheckBox,
            openingCheckBox,
            closeCheckBox,
            lowCheckBox,
            highCheckBox,
            changeCheckBox,
            changePercentCheckBox,
            costPriceCheckBox,
            holdingsCheckBox
        )
        val selectedCount = allCheckboxes.count { it.isSelected }

        if (selectedCount == 0) {
            changed.isSelected = true
            columnWarningLabel?.isVisible = true
        } else {
            columnWarningLabel?.isVisible = false
        }
    }

    private fun refreshAllWindows() {
        StockerAppManager.getAllApplications().forEach { app ->
            app.shutdownThenClear()
            app.schedule()
        }
    }

}
