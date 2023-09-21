import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.13.3"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.0.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    updateSinceUntilBuild.set(false)
}

changelog {
    version.set(properties("pluginVersion"))
    path.set("${project.projectDir}/CHANGELOG.md")
    groups.set(emptyList())
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
    patchPluginXml {
        version.set(properties("pluginVersion"))

        val description = """
            <div>
              <p>
                Stocker is a JetBrains IDE extension dashboard for investors to track
                realtime stock market conditions.
              </p>
              <br />
              <p>
                <img
                  src="https://raw.githubusercontent.com/WhiteVermouth/intellij-investor-dashboard/master/screenshots/Dashboard.png"
                  alt="Dashboard"
                  width="650"
                />
              </p>
              <h2>Installation</h2>
              <p>
                Search <strong>Stocker</strong> in <code>Plugin Marketplace</code> and click
                <code>Install</code>.
              </p>
              <p>
                <img
                  src="https://raw.githubusercontent.com/WhiteVermouth/intellij-investor-dashboard/master/screenshots/Install.png"
                  alt="Install"
                  width="650"
                />
              </p>
              <h2>Usage</h2>
              <p>
                All instructions can be found at
                <a href="https://nszihan.com/posts/stocker">here</a>.
              </p>
              <h2>Licence</h2>
              <p>Apache License</p>
            </div>
        """.trimIndent()

        pluginDescription.set(description)
        changeNotes.set(provider { changelog.renderItem(changelog.getLatest(), Changelog.OutputType.HTML) })
    }
    runPluginVerifier {
        ideVersions.set(
            properties("pluginVerifierIdeVersions").split(",").map(String::trim).filter(String::isNotEmpty)
        )
        failureLevel.set(
            listOf(
                FailureLevel.COMPATIBILITY_PROBLEMS, FailureLevel.INVALID_PLUGIN
            )
        )
    }
    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getProperty("jetbrains.token"))
    }
}
