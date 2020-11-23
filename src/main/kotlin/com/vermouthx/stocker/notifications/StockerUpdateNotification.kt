package com.vermouthx.stocker.notifications

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup

class StockerUpdateNotification {
    private val notificationGroup = NotificationGroup(
        displayId = "Stocker",
        displayType = NotificationDisplayType.STICKY_BALLOON,
        isLogByDefault = true
    )
}