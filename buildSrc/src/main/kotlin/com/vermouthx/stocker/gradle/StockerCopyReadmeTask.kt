package com.vermouthx.stocker.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.streams.toList

open class StockerCopyReadmeTask : DefaultTask() {
    init {
        group = "documentation"
        description = "Copy the readme markdown file to a place where the markdownToHtml sourceDir locates at"
    }

    @TaskAction
    fun run() {
        val readmeFilename = "README.md"
        val markdownPath = Paths.get(project.rootDir.absolutePath, "build", "markdown", readmeFilename)
        Files.copy(
            Paths.get(project.rootDir.absolutePath, readmeFilename),
            markdownPath,
            StandardCopyOption.REPLACE_EXISTING
        )
        val content = Files.readAllLines(markdownPath)
            .stream()
            .skip(5)
            .filter {
                !it.contains("shields.io") && !it.contains("azure.com")
            }
            .toList()
            .joinToString("\n")
        Files.newBufferedWriter(
            markdownPath,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use {
            it.write(content)
        }
    }
}