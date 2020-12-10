import com.vermouthx.stocker.gradle.StockerCopyChangelogTask
import com.vermouthx.stocker.gradle.StockerCopyReadmeTask
import com.vermouthx.stocker.gradle.StockerPatchHtmlTask
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.kordamp.gradle.plugin.markdown.tasks.MarkdownToHtmlTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("org.jetbrains.intellij") version "0.6.3"
    id("stocker-gradle-helper")
    id("org.kordamp.gradle.markdown") version "2.2.0"
}

group = "com.vermouthx"
version = "1.3.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

intellij {
    version = "2020.2"
    type = "IU"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

listOf("compileKotlin", "compileTestKotlin").forEach {
    tasks.getByName<KotlinCompile>(it) {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks {
    markdownToHtml {
        sourceDir = file("$projectDir/build/markdown")
        outputDir = file("$projectDir/build/html")
    }
    patchPluginXml {
        sinceBuild("202")
        untilBuild("203.*")
        val changelogPath = "$projectDir/build/html/CHANGELOG.html"
        val readmePath = "$projectDir/build/html/README.html"
        if (file(changelogPath).exists()) {
            changeNotes(file(changelogPath).readText())
        }
        if (file(readmePath).exists()) {
            pluginDescription(file(readmePath).readText())
        }
    }
    publishPlugin {
        token(System.getProperty("jetbrains.token"))
    }
}

tasks.withType<StockerCopyReadmeTask> {
    dependsOn("createDirectory")
}

tasks.withType<StockerCopyChangelogTask> {
    dependsOn("createDirectory")
}

tasks.withType<MarkdownToHtmlTask> {
    dependsOn("copyChangelog")
    dependsOn("copyReadme")
}

tasks.withType<StockerPatchHtmlTask> {
    dependsOn("markdownToHtml")
}

tasks.withType<PatchPluginXmlTask> {
    dependsOn("patchHtml")
}