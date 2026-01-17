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
            <h4 style="${Styles.HEADING}">✨ What's New</h4>
            <ul style="margin: 0; padding-left: 18px;">
                <li style="${Styles.LIST_ITEM}">✏️ Custom stock name feature with edit functionality (custom names take highest priority)</li>
                <li style="${Styles.LIST_ITEM}">📊 Enhanced management dialog with three-column layout (Code, Original Name, Custom Name)</li>
                <li style="${Styles.LIST_ITEM}">🔍 Enhanced suggestion dialog with improved search results layout</li>
            </ul>
        </div>
    """.trimIndent()

    @Language("HTML")
    private val releaseNote = """
        <div style="${Styles.CONTAINER}">
            <p style="${Styles.PARAGRAPH}">
                🎉 <strong>Welcome to Stocker v${StockerMeta.currentVersion}!</strong> Here's what's new in this release:
            </p>
            $whatsNew
            <div style="${Styles.INFO_BOX}">
                <p style="margin: 0; font-size: 12px;">
                    💡 <strong>Pro tip:</strong> Edit stock names in <span style="${Styles.HIGHLIGHT}">Tools → Manage Favorite Stocks</span>
                </p>
            </div>
            <p style="${Styles.SMALL_TEXT}">
                💖 Your support helps me continue creating and improving the plugin. Thank you! 📈
            </p>
        </div>
    """.trimIndent()

    @Language("HTML")
    private val welcomeMessage = """
        <div style="${Styles.CONTAINER}">
            <p style="${Styles.PARAGRAPH}">
                🎉 <strong>Welcome to Stocker!</strong> Your investment dashboard is now installed and ready to track your favorite stocks.
            </p>
            <div style="${Styles.INFO_BOX}">
                <p style="margin: 0 0 8px 0; font-size: 12px;">
                    💡 <strong>Quick Setup:</strong>
                </p>
                <ul style="margin: 0; padding-left: 16px; font-size: 12px;">
                    <li style="margin: 4px 0;">Open the <span style="${Styles.HIGHLIGHT}">Stocker</span> tool window from the left panel</li>
                    <li style="margin: 4px 0;">Click <span style="${Styles.HIGHLIGHT}">Add Favorite Stocks</span> to search and add stocks</li>
                    <li style="margin: 4px 0;">Configure settings at <span style="${Styles.HIGHLIGHT}">Settings → Tools → Stocker</span></li>
                    <li style="margin: 4px 0;">Start tracking your investments in real-time!</li>
                </ul>
            </div>
            <p style="${Styles.SMALL_TEXT}">
                💖 Your support helps me continue creating and improving the plugin. Thank you! 📊
            </p>
        </div>
    """.trimIndent()

    private const val NOTIFICATION_GROUP_ID = "Stocker"

    @JvmField
    val notificationIcon = IconLoader.getIcon("/icons/logo.png", javaClass)

    private const val GITHUB_LINK = "https://github.com/WhiteVermouth/intellij-investor-dashboard"
    private const val DONATE_LINK = "https://www.buymeacoffee.com/nszihan"

    fun notifyReleaseNote(project: Project) {
        val title = "Stocker v${StockerMeta.currentVersion} - Release Notes"
        val notification = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(title, releaseNote, NotificationType.INFORMATION)
        addNotificationActions(notification)
        notification.icon = notificationIcon
        notification.notify(project)
    }

    fun notifyWelcome(project: Project) {
        val title = "Stocker Successfully Installed"
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
