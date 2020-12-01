package com.vermouthx.stocker.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Paths

open class StockerCreateDirectoryTask : DefaultTask() {
    init {
        group = "documentation"
        description = "Create markdown and html directories"
    }

    @TaskAction
    fun run() {
        val markdownPath = Paths.get(project.rootDir.absolutePath, "build", "markdown")
        Files.createDirectories(markdownPath)
        val htmlPath = Paths.get(project.rootDir.absolutePath, "build", "html")
        Files.createDirectories(htmlPath)
    }
}