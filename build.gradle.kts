import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.Constants
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.providers.ProductReleasesValueSource
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.jetbrains.intellij.platform") version "2.12.0"
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.apache.commons:commons-text:1.14.0")
    implementation("com.belerweb:pinyin4j:2.5.1")
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // The IntelliJ Platform on the test classpath references JUnit 4's TestRule, which the
    // 2025.3+ platform no longer provides transitively. Our own tests run on JUnit 5 via
    // useJUnitPlatform(); this only satisfies the platform's runtime reference.
    testRuntimeOnly("junit:junit:4.13.2")
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
        pluginVerifier()
    }
}

tasks.test {
    useJUnitPlatform()
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
                types = listOf(IntelliJPlatformType.IntellijIdeaUltimate)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = "253"
            }
        }
        failureLevel = listOf(
            VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS, VerifyPluginTask.FailureLevel.INVALID_PLUGIN
        )
    }
}

// Resolve the latest stable IntelliJ IDEA *release* version (RELEASE channel only, so
// EAP/beta builds are excluded). The feed returns coordinates such as "IU-2026.1.3",
// ordered newest-first; we take the newest and strip the product-code prefix to get the
// bare version (e.g. "2026.1.3"). We query the Ultimate product because it is always
// published and shares release versions with the unified IDEA distribution used below.
// Resolution is lazy and only happens when the runIdeLatest task actually runs.
val latestIdeaReleaseVersion = providers.of(ProductReleasesValueSource::class) {
    parameters {
        jetbrainsIdesUrl = Constants.Locations.PRODUCTS_RELEASES_JETBRAINS_IDES
        androidStudioUrl = Constants.Locations.PRODUCTS_RELEASES_ANDROID_STUDIO
        channels = listOf(ProductRelease.Channel.RELEASE)
        types = listOf(IntelliJPlatformType.IntellijIdeaUltimate)
        sinceBuild = "253"
        untilBuild = "999.*"
    }
}.map { releases -> releases.first().substringAfter('-') }

// `./gradlew runIdeLatest` launches a sandbox IDE on the latest stable IntelliJ IDEA
// release instead of the build target declared in gradle.properties (platformVersion).
// Uses the unified IntelliJ IDEA distribution (IntellijIdea), since the separate
// Community (IC) artifact is no longer published as of 2025.3 / build 253.
intellijPlatformTesting {
    runIde {
        register("runIdeLatest") {
            type = IntelliJPlatformType.IntellijIdea
            version = latestIdeaReleaseVersion
        }
    }
}
