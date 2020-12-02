package com.vermouthx.stocker.notifications

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader

object StockerNotification {
    private val notificationGroup = NotificationGroup(
        displayId = "Stocker",
        displayType = NotificationDisplayType.STICKY_BALLOON,
        isLogByDefault = true
    )

    @JvmField
    var logoIcon = IconLoader.getIcon("/icons/logo.svg", javaClass)

    private val releaseNote: String = """
        What's new?<br>
            - Add a tab: ALL<br>
            - Enhanced UI<br>
        Please visit the <a href="https://github.com/WhiteVermouth/intellij-investor-dashboard/blob/master/CHANGELOG.md">Changelog</a> for more details.<br>
        Thank you for choosing Stocker
    """.trimIndent()

    fun notifyReleaseNote(project: Project, version: String) {
        notificationGroup.createNotification(
            title = "Stocker Updated to $version",
            content = releaseNote,
            type = NotificationType.INFORMATION,
            listener = NotificationListener.URL_OPENING_LISTENER
        )
            .setIcon(logoIcon)
            .notify(project)
    }
}