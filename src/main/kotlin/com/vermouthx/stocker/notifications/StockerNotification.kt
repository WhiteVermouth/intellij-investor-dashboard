package com.vermouthx.stocker.notifications

import com.intellij.notification.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.intellij.lang.annotations.Language

object StockerNotification {
    @Language("HTML")
    private val whatsNew: String = """
        <ul>
            <li>Support disable Red/Green color pattern. Go to <em>Preferences/Settings -> Tools -> Stocker</em>, pick <em>Color Pattern</em> option <em>None</em>, then apply.</li>
            <li>Fixed compatibility issue</li>
        </ul>
    """.trimIndent()

    @Language("HTML")
    private val releaseNote: String = """
        <h3>What's new</h3>
        $whatsNew
        <p>Please visit the <a href="https://github.com/WhiteVermouth/intellij-investor-dashboard/blob/master/CHANGELOG.md">Changelog</a> for more details.</p>
        <p>Enjoy this plugin? Consider <a href='https://github.com/WhiteVermouth/intellij-investor-dashboard'>STAR</a> this project.</p>
        <p>Thank you for choosing Stocker.</p>
    """.trimIndent()

    @JvmField
    var logoIcon = IconLoader.getIcon("/icons/logo.svg", javaClass)

    fun notifyReleaseNote(project: Project, version: String) {
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("Stocker")
            .createNotification(
                title = "Stocker Updated to v$version",
                content = releaseNote,
                type = NotificationType.INFORMATION,
                listener = NotificationListener.URL_OPENING_LISTENER
            )
            .setIcon(logoIcon)
            .notify(project)
    }
}