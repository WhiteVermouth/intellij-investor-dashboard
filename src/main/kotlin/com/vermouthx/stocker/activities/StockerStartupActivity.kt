package com.vermouthx.stocker.activities

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.vermouthx.stocker.notifications.StockerNotification
import com.vermouthx.stocker.settings.StockerSetting

class StockerStartupActivity : StartupActivity, DumbAware {

    companion object {
        private val setting = StockerSetting.instance
        private const val pluginId = "com.vermouthx.intellij-investor-dashboard"
    }

    override fun runActivity(project: Project) {
        val currentVersion = PluginManagerCore.getPlugin(PluginId.getId(pluginId))?.version ?: ""
        if (setting.version.isEmpty()) {
            setting.version = currentVersion
            StockerNotification.notifyWelcome(project)
            return
        }
        if (setting.version != currentVersion) {
            setting.version = currentVersion
            StockerNotification.notifyReleaseNote(project, currentVersion)
        }
    }
}