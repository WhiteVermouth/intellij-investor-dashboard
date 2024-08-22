package com.vermouthx.stocker.activities

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.vermouthx.stocker.notifications.StockerNotification
import com.vermouthx.stocker.settings.StockerSetting

class StockerStartupActivity : ProjectActivity, DumbAware {

    private val setting = StockerSetting.instance
    private val pluginId = "com.vermouthx.intellij-investor-dashboard"

    override suspend fun execute(project: Project) {
        val currentVersion = PluginManagerCore.getPlugin(PluginId.getId(pluginId))?.version ?: ""
        if (setting.version != currentVersion) {
            setting.version = currentVersion
            StockerNotification.notifyInviteSupporter(project)
        }
    }
}
