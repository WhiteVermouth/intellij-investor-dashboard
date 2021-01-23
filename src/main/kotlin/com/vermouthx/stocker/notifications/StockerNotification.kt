package com.vermouthx.stocker.notifications

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.intellij.lang.annotations.Language

object StockerNotification {
    @Language("HTML")
    private val whatsNew: String = """
        <li>Bug fix</li>
    """.trimIndent()

    @Language("HTML")
    private val releaseNote: String = """
        <div>
            <h3>What's new</h3>
            <ul>
                $whatsNew
            </ul>
            <p>Please visit the <a href="https://github.com/WhiteVermouth/intellij-investor-dashboard/blob/master/CHANGELOG.md">Changelog</a> for more details.</p>>
            <p>Thank you for choosing Stocker.</p>
        </div>
    """.trimIndent()

    private val notificationGroup = NotificationGroup(
        displayId = "Stocker",
        displayType = NotificationDisplayType.STICKY_BALLOON,
        isLogByDefault = true
    )

    @JvmField
    var logoIcon = IconLoader.getIcon("/icons/logo.svg", javaClass)

    fun notifyReleaseNote(project: Project, version: String) {
        notificationGroup.createNotification(
            title = "Stocker Updated to v$version",
            content = releaseNote,
            type = NotificationType.INFORMATION,
            listener = NotificationListener.URL_OPENING_LISTENER
        )
            .setIcon(logoIcon)
            .notify(project)
    }
}