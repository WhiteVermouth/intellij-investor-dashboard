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

    fun notifyInvalidCode(project: Project, code: String) {
        notificationGroup.createNotification(
                title = "Stocker",
                content = "You entered an invalid stock code: ${code}.",
                type = NotificationType.ERROR,
                listener = NotificationListener.URL_OPENING_LISTENER
        )
                .setIcon(AllIcons.General.NotificationError)
                .notify(project)
    }
}