package com.vermouthx.stocker.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.jsoup.Jsoup
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

open class StockerPatchHtmlTask : DefaultTask() {
    init {
        group = "documentation"
        description = "Patch generated readme and changelog HTML"
    }

    @TaskAction
    fun run() {
        val readmeHtmlPath = Paths.get(project.rootDir.absolutePath, "build", "html", "README.html")
        val readmeHtml = Jsoup.parse(readmeHtmlPath.toFile(), Charsets.UTF_8.name())
        readmeHtml.getElementsByTag("img")
            .forEach {
                if (it.attr("alt") == "Dashboard" || it.attr("alt") == "Install") {
                    it.attr("width", "650")
                }
            }
        readmeHtml.getElementsByTag("h1")
            .forEach {
                if (it.attr("align") == "center") {
                    it.attr("align", "left")
                }
            }
        readmeHtml.getElementsByTag("p")
            .forEach {
                if (it.attr("align") == "center") {
                    it.attr("align", "left")
                }
            }
        Files.newBufferedWriter(readmeHtmlPath, StandardOpenOption.TRUNCATE_EXISTING)
            .use {
                it.write(readmeHtml.html())
            }
    }
}