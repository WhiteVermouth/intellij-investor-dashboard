package com.vermouthx.stocker

import com.intellij.ide.plugins.PluginManager

object StockerMeta {
    val currentVersion: String
        get() = PluginManager.getPluginByClass(StockerMeta::class.java)?.version ?: ""
}
