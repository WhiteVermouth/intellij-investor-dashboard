import com.vermouthx.stocker.gradle.StockerCopyChangelogTask
import com.vermouthx.stocker.gradle.StockerCopyReadmeTask
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.kordamp.gradle.plugin.markdown.tasks.MarkdownToHtmlTask

plugins {
    java
    kotlin("jvm") version "1.4.10"
    id("org.jetbrains.intellij") version "0.4.21"
    id("stocker-gradle-helper")
    id("org.kordamp.gradle.markdown") version "2.2.0"
}

group = "com.vermouthx"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

intellij {
    version = "2020.2"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
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
    dependsOn("patchHtml")
}

tasks.withType<StockerCopyChangelogTask> {
    dependsOn("patchHtml")
}

tasks.withType<MarkdownToHtmlTask> {
    dependsOn("copyChangelog")
    dependsOn("copyReadme")
}

tasks.withType<PatchPluginXmlTask> {
    dependsOn("markdownToHtml")
}