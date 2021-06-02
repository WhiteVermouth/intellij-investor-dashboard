package com.vermouthx.stocker.activities

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.vermouthx.stocker.StockerApp
import com.vermouthx.stocker.notifications.StockerNotification
import com.vermouthx.stocker.settings.StockerSetting

class StockerStartupActivity : StartupActivity, DumbAware {

    companion object {
        private val setting = StockerSetting.instance
    }

    override fun runActivity(project: Project) {
        val currentVersion = PluginManagerCore.getPlugin(PluginId.getId(StockerApp.PLUGIN_ID))?.version ?: ""
        if (setting.version.isEmpty()) {
            setting.version = currentVersion
            StockerNotification.notifyWelcome(project, currentVersion)
            return
        }
        if (setting.version != currentVersion) {
            setting.version = currentVersion
            StockerNotification.notifyReleaseNote(project, currentVersion)
        }
    }
}