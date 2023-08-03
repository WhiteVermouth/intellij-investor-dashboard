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
            <li>修复部分品种不能正常显示三位小数价格</li>
        </ul>
    """.trimIndent()

    private const val howToUseLink = "https://nszihan.com/2021/04/11/stocker"
    private const val changelogLink =
        "https://github.com/WhiteVermouth/intellij-investor-dashboard/blob/master/CHANGELOG.md"
    private const val githubRepoLink = "https://github.com/WhiteVermouth/intellij-investor-dashboard"

    @Language("HTML")
    private val releaseNote: String = """
        <p>What's new?</p>
        $whatsNew
    """.trimIndent()

    @Language("HTML")
    private val welcomeMessage: String = """
        <p>Thank you for choosing Stocker.</p>
    """.trimIndent()

    private const val notificationGroupId = "Stocker"

    @JvmField
    val logoIcon = IconLoader.getIcon("/icons/logo.svg", javaClass)

    fun notifyWelcome(project: Project) {
        val notification =
            NotificationGroupManager.getInstance().getNotificationGroup(notificationGroupId).createNotification(
                "Stocker is installed", welcomeMessage, NotificationType.INFORMATION
            )
        addNotificationActions(notification)
        notification.icon = logoIcon
        notification.notify(project)
    }

    fun notifyReleaseNote(project: Project, version: String) {
        val notification =
            NotificationGroupManager.getInstance().getNotificationGroup(notificationGroupId).createNotification(
                "Stocker updated to v$version", releaseNote, NotificationType.INFORMATION
            )
        addNotificationActions(notification)
        notification.icon = logoIcon
        notification.notify(project)
    }

    private fun addNotificationActions(notification: Notification) {
        notification.addAction(NotificationAction.createSimple("Changelog") {
            BrowserUtil.browse(changelogLink)
        })
        notification.addAction(NotificationAction.createSimple("Usage") {
            BrowserUtil.browse(howToUseLink)
        })
        notification.addAction(NotificationAction.createSimple("GitHub") {
            BrowserUtil.browse(githubRepoLink)
        })
    }
}
