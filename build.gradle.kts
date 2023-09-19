import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    kotlin("jvm") version "1.9.10"
}

group = "io.github.alexswilliams"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
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
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
//        freeCompilerArgs += "-opt-in=kotlin.time.ExperimentalTime"
        jvmTarget = "20"
    }
}
