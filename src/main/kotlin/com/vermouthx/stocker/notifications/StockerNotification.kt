package com.vermouthx.stocker.notifications

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.intellij.lang.annotations.Language

object StockerNotification {
    private val NOTIFICATION_GROUP = NotificationGroup("Stocker", NotificationDisplayType.STICKY_BALLOON, true)

    @Language("HTML")
    private val whatsNew: String = """
        <ul>
            <li>Fixed Android Studio compatibility</li>
            <li>Fixed missed ETF in search results</li>
        </ul>
    """.trimIndent()

    @Language("HTML")
    private val releaseNote: String = """
        <h3>What's new</h3>
        $whatsNew
        <p>Thank you for choosing Stocker.</p>
        <p><a href="https://github.com/WhiteVermouth/intellij-investor-dashboard/blob/master/CHANGELOG.md">Changelog</a> | <a href='https://github.com/WhiteVermouth/intellij-investor-dashboard'>Star Repository</a></p>
    """.trimIndent()

    @JvmField
    val logoIcon = IconLoader.getIcon("/icons/logo.svg", javaClass)

    fun notifyReleaseNote(project: Project, version: String) {
        NOTIFICATION_GROUP.createNotification(
            "Stocker Updated to v$version",
            releaseNote,
            NotificationType.INFORMATION,
            NotificationListener.URL_OPENING_LISTENER
        )
            .setIcon(logoIcon)
            .notify(project)
    }
}