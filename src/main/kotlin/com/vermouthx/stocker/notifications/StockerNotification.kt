package com.vermouthx.stocker.notifications

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object StockerNotification {
    private val notificationGroup = NotificationGroup(
        displayId = "Stocker",
        displayType = NotificationDisplayType.BALLOON,
        isLogByDefault = true
    )

    private val releaseNote: String = """
        What's new?
    """.trimIndent()

    fun notifyReleaseNote(project: Project) {
        notificationGroup.createNotification(
            title = "Stocker Updated",
            content = releaseNote,
            type = NotificationType.INFORMATION,
            listener = NotificationListener.URL_OPENING_LISTENER
        )
            .setIcon(AllIcons.General.NotificationInfo)
            .notify(project)
    }
}