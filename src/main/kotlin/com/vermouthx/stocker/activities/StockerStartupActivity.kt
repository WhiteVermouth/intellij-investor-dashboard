package com.vermouthx.stocker.activities

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.vermouthx.stocker.settings.StockerSetting

class StockerStartupActivity : StartupActivity {

    companion object {
        private const val pluginId = "com.vermouthx.intellij-investor-dashboard"
        private val setting = StockerSetting.instance
    }

    override fun runActivity(project: Project) {
        val currentVersion = PluginManagerCore.getPlugin(PluginId.getId(pluginId))?.version ?: ""
        if (setting.version != currentVersion) {
            setting.version = currentVersion
        }
    }
}