plugins {
    java
    id("org.jetbrains.intellij") version "0.4.21"
    kotlin("jvm") version "1.4.10"
}

group = "com.vermouthx"
version = "1.0.1"

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
    patchPluginXml {
        sinceBuild("202")
        untilBuild("203.*")
        pluginDescription("""
            Stocker is a dashboard, which helps investor envision realtime market conditions in IntelliJ based IDEs.
        """.trimIndent())
        changeNotes("""
            1.0.1 <br>
                - Bug fix
            1.0.0 <br>
                - Stocker: a stock quote dashboard
        """.trimIndent())
    }
}