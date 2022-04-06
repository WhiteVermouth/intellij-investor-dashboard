package com.vermouthx.stocker.notifications

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.intellij.lang.annotations.Language

object StockerNotification {

    @Language("HTML")
    private val whatsNew: String = """
        <ul>
            <li>新的自选股票管理界面：可批量删除、重排序自选股票</li>
        </ul>
    """.trimIndent()

    @Language("HTML")
    private val footnote: String = """
        <p>Thank you for choosing Stocker.</p>
        <br/>
        <p>
            <a href="https://nszihan.com/posts/stocker">How to Use</a> | 
            <a href="https://github.com/WhiteVermouth/intellij-investor-dashboard/blob/master/CHANGELOG.md">Changelog</a> | 
            <a href="https://github.com/WhiteVermouth/intellij-investor-dashboard">Repository</a>
        </p>
    """.trimIndent()

    @Language("HTML")
    private val releaseNote: String = """
        <div>
            <h3>What's new?</h3>
            $whatsNew
            $footnote
        </div>
    """.trimIndent()

    @Language("HTML")
    private val welcomeMessage: String = """
        <div>
            $footnote
        </div>
    """.trimIndent()

    private const val notificationGroupId = "Stocker"

    @JvmField
    val logoIcon = IconLoader.getIcon("/icons/logo.svg", javaClass)

    fun notifyWelcome(project: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup(notificationGroupId).createNotification(
            "Stocker is installed",
            welcomeMessage,
            NotificationType.INFORMATION,
            NotificationListener.URL_OPENING_LISTENER
        )
            .setIcon(logoIcon)
            .notify(project)
    }

    fun notifyReleaseNote(project: Project, version: String) {
        NotificationGroupManager.getInstance().getNotificationGroup(notificationGroupId).createNotification(
            "Stocker updated to v$version",
            releaseNote,
            NotificationType.INFORMATION,
            NotificationListener.URL_OPENING_LISTENER
        )
            .setIcon(logoIcon)
            .notify(project)
    }
}
