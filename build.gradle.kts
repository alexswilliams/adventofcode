import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    kotlin("jvm") version "1.7.21"
}

group = "io.github.alexswilliams"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("test"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

sourceSets.main {
    kotlin.srcDirs("2022/src")
    resources.srcDirs("2022/src")
    kotlin.exclude("2019")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}