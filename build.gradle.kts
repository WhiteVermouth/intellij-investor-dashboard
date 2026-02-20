import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("org.jetbrains.changelog") version "2.5.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.apache.commons:commons-text:1.14.0")
    implementation("com.belerweb:pinyin4j:2.5.1")
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
        pluginVerifier()
    }
}

changelog {
    version.set(properties("pluginVersion"))
    path.set("${project.projectDir}/CHANGELOG.md")
    groups.set(emptyList())
}

val pluginDescription = """
    <div>
      <p>
        Stocker is a JetBrains IDE extension dashboard for investors to track
        real-time stock market conditions.
      </p>
      <h2>Features</h2>
      <ul>
        <li>📊 Real-time market data for stocks and cryptocurrencies</li>
        <li>🌐 Support for A-Shares, Hong Kong stocks, US stocks, and cryptocurrencies</li>
        <li>🎨 Customizable display with multiple color patterns and table columns</li>
        <li>🔤 Pinyin support for stock names</li>
        <li>📈 Sortable columns with three-state sorting</li>
        <li>🎯 Custom stock names and smart search</li>
        <li>📋 Batch operations for stock management</li>
      </ul>
      <h2>Quick Start</h2>
      <ol>
        <li>Open the Stocker tool window from the sidebar</li>
        <li>Click "Add Favorite Stocks" to search and add stocks</li>
        <li>Customize settings at Settings → Tools → Stocker</li>
        <li>Track your investments in real-time!</li>
      </ol>
      <h2>Documentation</h2>
      <ul>
        <li><a href="https://www.vermouthx.com/posts/2021/stocker">Getting Started Guide</a></li>
        <li><a href="https://github.com/WhiteVermouth/intellij-investor-dashboard/blob/master/CHANGELOG.md">Changelog</a></li>
        <li><a href="https://github.com/WhiteVermouth/intellij-investor-dashboard/issues">Report Issues</a></li>
      </ul>
      <h2>License</h2>
      <a href="https://raw.githubusercontent.com/WhiteVermouth/intellij-investor-dashboard/master/LICENSE">Apache 2.0 License</a>
      <h2>Donation</h2>
      <p>If you like this plugin, you can <a href="https://www.buymeacoffee.com/nszihan">buy me a cup of coffee</a>. Thank you!</p>
    </div>
""".trimIndent()

intellijPlatform {
    buildSearchableOptions = false
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")
        description = pluginDescription
        changeNotes = provider {
            changelog.renderItem(changelog.getLatest(), Changelog.OutputType.HTML)
        }
        ideaVersion {
            untilBuild = provider { null }
        }
    }
    publishing {
        token = System.getProperty("jetbrains.token")
    }
    pluginVerification {
        ides {
            recommended()
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaCommunity, IntelliJPlatformType.IntellijIdeaUltimate)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = "241"
                untilBuild = "242.*"
            }
        }
        failureLevel = listOf(
            VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS, VerifyPluginTask.FailureLevel.INVALID_PLUGIN
        )
    }
}
