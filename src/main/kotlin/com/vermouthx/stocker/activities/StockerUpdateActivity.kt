package com.vermouthx.stocker.activities

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.vermouthx.stocker.settings.StockerSetting

class StockerUpdateActivity : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        val settings = StockerSetting.instance
        settings.version = "1.0.0"
    }
}