package com.vermouthx.stocker.notifications

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.vermouthx.stocker.StockerMeta
import org.intellij.lang.annotations.Language

object StockerNotification {

    // Stocker Color Palette
    private object Colors {
        const val PRIMARY = "#4CAF50"          // Green (for stock market theme)
        const val SECONDARY = "#2196F3"        // Blue
        const val ACCENT = "#FF9800"           // Orange (for highlights)
        const val BACKGROUND = "rgba(33, 150, 243, 0.08)" // Subtle background
        const val BORDER = "#2196F3"           // Border color
    }

    // Common CSS styles for consistency
    private object Styles {
        const val CONTAINER = "margin: 8px 0; line-height: 1.4;"
        const val HEADING = "margin: 0 0 8px 0; color: ${Colors.PRIMARY}; font-size: 14px; font-weight: 600;"
        const val PARAGRAPH = "margin: 0 0 12px 0; font-size: 13px;"
        const val SMALL_TEXT = "margin: 12px 0 0 0; font-size: 12px; font-style: italic; opacity: 0.7;"
        const val LIST_ITEM = "margin: 6px 0;"
        const val INFO_BOX = "background: ${Colors.BACKGROUND}; border-left: 3px solid ${Colors.BORDER}; padding: 10px 12px; margin: 12px 0; border-radius: 3px;"
        const val HIGHLIGHT = "color: ${Colors.ACCENT}; font-weight: 500;"
    }

    @Language("HTML")
    private val whatsNew = """
        <div style="${Styles.CONTAINER}">
            <h4 style="${Styles.HEADING}">✨ What's New / 新功能</h4>
            <ul style="margin: 0; padding-left: 18px;">
                <li style="${Styles.LIST_ITEM}">📊 <strong>Cost Price & Holdings Columns</strong> / 成本价与持仓列
                    <ul style="margin: 4px 0 0 0; padding-left: 18px; font-size: 12px;">
                        <li>Added cost price and holdings columns with visibility toggling for enhanced portfolio tracking / 添加成本价和持仓列，支持显示切换，增强投资组合跟踪</li>
                    </ul>
                </li>
                <li style="${Styles.LIST_ITEM}">🎨 <strong>UI Improvements</strong> / 界面改进
                    <ul style="margin: 4px 0 0 0; padding-left: 18px; font-size: 12px;">
                        <li>Refined table rendering with improved padding and border styling / 优化表格渲染，改进内边距和边框样式</li>
                        <li>Adopted IDE theme colors for table selection for better visual consistency / 采用 IDE 主题颜色用于表格选中状态，确保更好的视觉一致性</li>
                    </ul>
                </li>
            </ul>
        </div>
    """.trimIndent()

    @Language("HTML")
    private val releaseNote = """
        <div style="${Styles.CONTAINER}">
            <p style="${Styles.PARAGRAPH}">
                🎉 <strong>Welcome to Stocker v${StockerMeta.currentVersion}!</strong> Here's what's new in this release:<br/>
                欢迎使用 Stocker v${StockerMeta.currentVersion}！本次更新内容：
            </p>
            $whatsNew
            <div style="${Styles.INFO_BOX}">
                <p style="margin: 0; font-size: 12px;">
                    💡 <strong>Pro tip / 小贴士：</strong> You can now track your cost price and holdings directly in the table. Toggle column visibility in Settings → Tools → Stocker.<br/>
                    您现在可以直接在表格中跟踪成本价和持仓。在设置 → 工具 → Stocker 中切换列的显示。
                </p>
            </div>
            <p style="${Styles.SMALL_TEXT}">
                💖 If you find this plugin helpful, please consider clicking the <strong>Donate</strong> button below to support its development. Thank you! 📈<br/>
                如果您觉得这个插件有帮助，请考虑点击下方的<strong>Donate</strong>按钮以支持开发。谢谢！
            </p>
        </div>
    """.trimIndent()

    @Language("HTML")
    private val welcomeMessage = """
        <div style="${Styles.CONTAINER}">
            <p style="${Styles.PARAGRAPH}">
                🎉 <strong>Welcome to Stocker!</strong> Your investment dashboard is now installed and ready to track your favorite stocks.<br/>
                欢迎使用 Stocker！您的投资仪表板已安装完成，可以开始跟踪您喜爱的股票了。
            </p>
            <div style="${Styles.INFO_BOX}">
                <p style="margin: 0 0 8px 0; font-size: 12px;">
                    💡 <strong>Quick Setup / 快速设置：</strong>
                </p>
                <ul style="margin: 0; padding-left: 16px; font-size: 12px;">
                    <li style="margin: 4px 0;">Open the <span style="${Styles.HIGHLIGHT}">Stocker</span> tool window from the left panel<br/>从左侧面板打开 <span style="${Styles.HIGHLIGHT}">Stocker</span> 工具窗口</li>
                    <li style="margin: 4px 0;">Click <span style="${Styles.HIGHLIGHT}">Add Favorite Stocks</span> to search and add stocks<br/>点击<span style="${Styles.HIGHLIGHT}">添加自选股票</span>来搜索和添加股票</li>
                    <li style="margin: 4px 0;">Configure settings at <span style="${Styles.HIGHLIGHT}">Settings → Tools → Stocker</span><br/>在<span style="${Styles.HIGHLIGHT}">设置 → 工具 → Stocker</span>中配置选项</li>
                    <li style="margin: 4px 0;">Start tracking your investments in real-time!<br/>开始实时跟踪您的投资！</li>
                </ul>
            </div>
            <p style="${Styles.SMALL_TEXT}">
                💖 If you find this plugin helpful, please consider clicking the <strong>Donate</strong> button below to support its development. Thank you! 📊<br/>
                如果您觉得这个插件有帮助，请考虑点击下方的<strong>Donate</strong>按钮以支持开发。谢谢！
            </p>
        </div>
    """.trimIndent()

    private const val NOTIFICATION_GROUP_ID = "Stocker"

    @JvmField
    val notificationIcon = IconLoader.getIcon("/icons/logo.png", javaClass)

    private const val GITHUB_LINK = "https://github.com/WhiteVermouth/intellij-investor-dashboard"
    private const val DONATE_LINK = "https://www.buymeacoffee.com/nszihan"

    fun notifyReleaseNote(project: Project) {
        val title = "Stocker v${StockerMeta.currentVersion} - Release Notes / 版本说明"
        val notification = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(title, releaseNote, NotificationType.INFORMATION)
        addNotificationActions(notification)
        notification.icon = notificationIcon
        notification.notify(project)
    }

    fun notifyWelcome(project: Project) {
        val title = "Stocker Successfully Installed / 安装成功"
        val notification = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(title, welcomeMessage, NotificationType.INFORMATION)
        addNotificationActions(notification)
        notification.icon = notificationIcon
        notification.notify(project)
    }

    private fun addNotificationActions(notification: Notification) {
        val github = NotificationAction.createSimple("📖 GitHub") {
            BrowserUtil.browse(GITHUB_LINK)
        }
        val actionDonate = NotificationAction.createSimple("☕ Donate") {
            BrowserUtil.browse(DONATE_LINK)
        }
        notification.addAction(github)
        notification.addAction(actionDonate)
    }
}
