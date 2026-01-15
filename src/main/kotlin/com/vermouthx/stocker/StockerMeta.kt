package com.vermouthx.stocker

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

object StockerMeta {
    val currentVersion: String
        get() = PluginManagerCore.getPlugin(PluginId.getId("com.vermouthx.intellij-investor-dashboard"))?.version ?: ""
}
