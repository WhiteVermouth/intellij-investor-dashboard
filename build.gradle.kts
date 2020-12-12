plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("org.jetbrains.intellij") version "0.6.3"
    id("stocker-gradle-helper")
    id("org.kordamp.gradle.markdown") version "2.2.0"
}

group = "com.vermouthx"
version = "1.3.2"

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

tasks {
    compileJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    compileTestJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
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

        dependsOn("patchHtml")
    }
    publishPlugin {
        token(System.getProperty("jetbrains.token"))
    }
}
