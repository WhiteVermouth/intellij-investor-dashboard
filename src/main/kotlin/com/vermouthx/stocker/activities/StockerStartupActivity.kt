package com.vermouthx.stocker.activities

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.vermouthx.stocker.StockerMeta
import com.vermouthx.stocker.notifications.StockerNotification
import com.vermouthx.stocker.settings.StockerSetting

class StockerStartupActivity : ProjectActivity, DumbAware {
    override suspend fun execute(project: Project) {
        val settings = StockerSetting.instance
        if (settings.version.isEmpty()) {
            settings.version = StockerMeta.currentVersion
            StockerNotification.notifyWelcome(project)
            return
        }
        if (StockerMeta.currentVersion != settings.version) {
            settings.version = StockerMeta.currentVersion
            StockerNotification.notifyReleaseNote(project)
        }
    }
}
