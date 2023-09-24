package com.vermouthx.stocker.notifications

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.intellij.lang.annotations.Language

object StockerNotification {

    @Language("HTML")
    private val whatsNew: String = """
        <ul>
            <li>恢复新浪行情接口的支持</li>
        </ul>
    """.trimIndent()

    private const val TUTORIAL_LINK = "https://nszihan.com/2021/04/11/stocker"
    private const val GITHUB_REPO_LINK = "https://github.com/WhiteVermouth/intellij-investor-dashboard"
    private const val DONATE_LINK = "https://www.buymeacoffee.com/nszihan"

    @Language("HTML")
    private val releaseNote: String = """
        <p>What's new?</p>
        $whatsNew
    """.trimIndent()

    @Language("HTML")
    private val welcomeMessage: String = """
        <p>Thank you for choosing Stocker.</p>
    """.trimIndent()

    private const val NOTIFICATION_GROUP_ID = "Stocker"

    @JvmField
    val logoIcon = IconLoader.getIcon("/icons/logo.png", javaClass)

    fun notifyWelcome(project: Project) {
        val notification =
            NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID).createNotification(
                "Stocker is installed", welcomeMessage, NotificationType.INFORMATION
            )
        addNotificationActions(notification)
        notification.icon = logoIcon
        notification.notify(project)
    }

    fun notifyReleaseNote(project: Project, version: String) {
        val notification =
            NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID).createNotification(
                "Stocker updated to v$version", releaseNote, NotificationType.INFORMATION
            )
        addNotificationActions(notification)
        notification.icon = logoIcon
        notification.notify(project)
    }

    private fun addNotificationActions(notification: Notification) {
        notification.addAction(NotificationAction.createSimple("Tutorial") {
            BrowserUtil.browse(TUTORIAL_LINK)
        })
        notification.addAction(NotificationAction.createSimple("GitHub") {
            BrowserUtil.browse(GITHUB_REPO_LINK)
        })
        notification.addAction(NotificationAction.createSimple("Donate") {
            BrowserUtil.browse(DONATE_LINK)
        })
    }
}
