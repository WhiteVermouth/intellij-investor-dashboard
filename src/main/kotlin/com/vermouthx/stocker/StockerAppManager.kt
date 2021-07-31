package com.vermouthx.stocker

import com.intellij.openapi.project.Project

object StockerAppManager {
    val myApplicationMap: MutableMap<Project, StockerApp> = mutableMapOf()

    fun myApplication(project: Project?): StockerApp? {
        return myApplicationMap[project]
    }
}