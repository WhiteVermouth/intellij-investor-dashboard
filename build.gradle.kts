import org.jetbrains.intellij.tasks.PatchPluginXmlTask

plugins {
    java
    id("org.jetbrains.intellij") version "0.4.21"
    kotlin("jvm") version "1.4.10"
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
}
tasks.getByName<PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
      Add change notes here.<br>
      <em>most HTML tags may be used</em>"""
    )
}