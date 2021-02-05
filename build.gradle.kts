import org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("org.jetbrains.intellij") version "0.6.3"
    id("stocker-gradle-helper")
    id("org.kordamp.gradle.markdown") version "2.2.0"
}

group = "com.vermouthx"
version = "1.3.6"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

intellij {
    version = "2019.2"
    type = "IC"
}

tasks {
    compileJava {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    compileTestJava {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    buildSearchableOptions {
        enabled = false
    }
    copyReadme {
        dependsOn("createDirectory")
    }
    copyChangelog {
        dependsOn("createDirectory")
    }
    markdownToHtml {
        sourceDir = file("$projectDir/build/markdown")
        outputDir = file("$projectDir/build/html")

        dependsOn("copyReadme", "copyChangelog")
    }
    patchHtml {
        dependsOn("markdownToHtml")
    }
    patchPluginXml {
        sinceBuild("192")
        untilBuild("211.*")
        val changelogPath = "$projectDir/build/html/CHANGELOG.html"
        val readmePath = "$projectDir/build/html/README.html"
        if (file(changelogPath).exists()) {
            changeNotes(file(changelogPath).readText())
        }
        if (file(readmePath).exists()) {
            pluginDescription(file(readmePath).readText())
        }

        dependsOn("patchHtml")
    }
    runPluginVerifier {
        ideVersions(listOf("192.7142.36", "193.7288.26", "201.8743.12", "202.8194.7", "203.7148.57"))
        setFailureLevel(FailureLevel.COMPATIBILITY_PROBLEMS)
    }
    publishPlugin {
        token(System.getProperty("jetbrains.token"))
    }
}
