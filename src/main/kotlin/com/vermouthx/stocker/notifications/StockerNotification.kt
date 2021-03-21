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
            <li>Enhanced stock management dialogs</li>
        </ul>
    """.trimIndent()

    @Language("HTML")
    private val releaseNote: String = """
        <h3>What's new</h3>
        $whatsNew
        <p>Visit the <a href="https://github.com/WhiteVermouth/intellij-investor-dashboard/blob/master/CHANGELOG.md">Changelog</a> for more details.</p>
        <p>Enjoy this plugin? Consider <a href='https://github.com/WhiteVermouth/intellij-investor-dashboard'>STAR</a> this project.</p>
        <p>Thank you for choosing Stocker.</p>
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