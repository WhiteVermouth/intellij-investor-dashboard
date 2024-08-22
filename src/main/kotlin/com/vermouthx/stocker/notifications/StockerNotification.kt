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

    private const val DONATE_LINK = "https://www.buymeacoffee.com/nszihan"

    @Language("HTML")
    private val inviteSupporterMessage: String = """
        <p>Your support helps me continue creating and improving the plugin.</p>
    """.trimIndent()

    private const val NOTIFICATION_GROUP_ID = "Stocker"

    @JvmField
    val logoIcon = IconLoader.getIcon("/icons/logo.png", javaClass)

    fun notifyInviteSupporter(project: Project) {
        val notification = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification("Become Stocker Supporter", inviteSupporterMessage, NotificationType.INFORMATION)
        addNotificationActions(notification)
        notification.icon = logoIcon
        notification.notify(project)
    }

    private fun addNotificationActions(notification: Notification) {
        notification.addAction(NotificationAction.createSimple("Support Now") {
            BrowserUtil.browse(DONATE_LINK)
        })
    }
}
