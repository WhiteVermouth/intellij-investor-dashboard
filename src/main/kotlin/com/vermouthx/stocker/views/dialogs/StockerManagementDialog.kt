package com.vermouthx.stocker.views.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.entities.StockerSuggestion
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerStockOperation
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerActionUtil
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import javax.swing.*

class StockerManagementDialog(val project: Project?) : DialogWrapper(project) {

    private val tabMap: MutableMap<Int, JBScrollPane> = mutableMapOf()

    private var currentMarketSelection: StockerMarketType = StockerMarketType.AShare

    init {
        title = "Manage Stock Symbols"
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val tabbedPane = JBTabbedPane()
        tabbedPane.add("CN", createTabContent(0))
        tabbedPane.add("HK", createTabContent(1))
        tabbedPane.add("US", createTabContent(2))
//        tabbedPane.add("Crypto", createTabContent(3))
        tabbedPane.addChangeListener {
            val setting = StockerSetting.instance
            when (tabbedPane.selectedIndex) {
                0 -> {
                    currentMarketSelection = StockerMarketType.AShare
                    val quotes = StockerQuoteHttpUtil.get(
                        StockerMarketType.AShare,
                        setting.quoteProvider,
                        setting.aShareList
                    )
                    tabMap[0]?.let { sp -> refreshTabPane(sp, quotes) }
                }
                1 -> {
                    currentMarketSelection = StockerMarketType.HKStocks
                    val quotes = StockerQuoteHttpUtil.get(
                        StockerMarketType.HKStocks,
                        setting.quoteProvider,
                        setting.hkStocksList
                    )
                    tabMap[1]?.let { sp -> refreshTabPane(sp, quotes) }
                }
                2 -> {
                    currentMarketSelection = StockerMarketType.USStocks
                    val quotes = StockerQuoteHttpUtil.get(
                        StockerMarketType.USStocks,
                        setting.quoteProvider,
                        setting.usStocksList
                    )
                    tabMap[2]?.let { sp -> refreshTabPane(sp, quotes) }
                }
                3 -> {
                    currentMarketSelection = StockerMarketType.Crypto
                    val quotes = StockerQuoteHttpUtil.get(
                        StockerMarketType.Crypto,
                        setting.quoteProvider,
                        setting.cryptoList
                    )
                    tabMap[3]?.let { sp -> refreshTabPane(sp, quotes) }
                }
                else -> return@addChangeListener
            }
        }
        tabMap[0]?.let { sp ->
            val setting = StockerSetting.instance
            refreshTabPane(
                sp,
                StockerQuoteHttpUtil.get(StockerMarketType.AShare, setting.quoteProvider, setting.aShareList)
            )
        }

        return panel {
            row {
                tabbedPane(CCFlags.grow)
            }
        }.withPreferredWidth(400).withPreferredHeight(500)
    }

    override fun createActions(): Array<Action> {
        return emptyArray()
    }

    private fun createTabContent(index: Int): JComponent {
        val scrollPane = JBScrollPane()
        tabMap[index] = scrollPane
        return panel {
            row {
                scrollPane(scrollPane)
            }
        }.withPreferredHeight(500)
    }

    private fun refreshTabPane(scrollPane: JBScrollPane, symbols: List<StockerQuote>) {
        scrollPane.setViewportView(
            panel {
                symbols.forEach { symbol ->
                    val actionButton = JButton(StockerStockOperation.STOCK_DELETE.operation)
                    val separator = JSeparator().also {
                        it.foreground = JBColor.border()
                        it.background = JBColor.background()
                    }
                    row {
                        label(symbol.code)
                        label(
                            if (symbol.name.length <= 20) {
                                symbol.name
                            } else {
                                "${symbol.name.substring(0, 20)}..."
                            }
                        )
                        actionButton.addActionListener {
                            val myApplication = StockerAppManager.myApplication(project)
                            if (myApplication != null) {
                                myApplication.shutdown()
                                when (StockerStockOperation.mapOf(actionButton.text)) {
                                    StockerStockOperation.STOCK_ADD -> {
                                        if (StockerActionUtil.addStock(
                                                currentMarketSelection,
                                                StockerSuggestion(symbol.code, symbol.name, currentMarketSelection),
                                                project
                                            )
                                        ) {
                                            actionButton.text = StockerStockOperation.STOCK_DELETE.operation
                                        }
                                    }
                                    StockerStockOperation.STOCK_DELETE -> {
                                        if (StockerActionUtil.removeStock(
                                                currentMarketSelection,
                                                StockerSuggestion(symbol.code, symbol.name, currentMarketSelection)
                                            )
                                        ) {
                                            actionButton.text = StockerStockOperation.STOCK_ADD.operation
                                        }
                                    }
                                    else -> {
                                        myApplication.schedule()
                                        return@addActionListener
                                    }
                                }
                                myApplication.schedule()
                            }
                        }
                        right {
                            actionButton()
                        }
                    }
                    row {
                        separator(CCFlags.growX)
                    }
                }
            }.withBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16))
        )
    }

}
