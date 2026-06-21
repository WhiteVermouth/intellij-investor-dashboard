package com.vermouthx.stocker

import com.intellij.ide.plugins.cl.PluginAwareClassLoader

object StockerMeta {
    val currentVersion: String
        get() = (StockerMeta::class.java.classLoader as? PluginAwareClassLoader)
            ?.pluginDescriptor?.version ?: ""
}
