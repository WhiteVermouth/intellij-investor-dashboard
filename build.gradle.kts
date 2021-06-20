import org.jetbrains.changelog.closure
import org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.0"
    id("org.jetbrains.intellij") version "1.0"
    id("org.jetbrains.changelog") version "1.1.2"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
}

changelog {
    version = properties("pluginVersion")
    path = "${project.projectDir}/CHANGELOG.md"
    header = closure { version }
    itemPrefix = "-"
    keepUnreleasedSection = false
    groups = emptyList()
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    buildSearchableOptions {
        enabled = false
    }
    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

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
        changeNotes.set(provider { changelog.getLatest().toHTML() })
    }
    runPluginVerifier {
        ideVersions.set(
            properties("pluginVerifierIdeVersions")
                .split(",")
                .map(String::trim)
                .filter(String::isNotEmpty)
        )
        failureLevel.set(
            listOf(
                FailureLevel.COMPATIBILITY_PROBLEMS,
                FailureLevel.INVALID_PLUGIN
            )
        )
    }
    publishPlugin {
        token.set(System.getProperty("jetbrains.token"))
    }
}
