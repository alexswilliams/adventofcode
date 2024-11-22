// https://gradle.org/releases/
// ./gradlew wrapper --gradle-version=8.11 --distribution-type=BIN

plugins {
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin.jvm/org.jetbrains.kotlin.jvm.gradle.plugin
    kotlin("jvm") version "2.0.21"
}

group = "io.github.alexswilliams"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("test"))
    testImplementation(kotlin("reflect"))

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-collections-immutable
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // https://mvnrepository.com/artifact/com.github.ajalt.mordant/mordant
    implementation("com.github.ajalt.mordant:mordant:3.0.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

sourceSets.main {
    kotlin.srcDirs("2019/src", "2022", "2023", "2024", "common/src/main/kotlin")
    resources.srcDirs("2019/resources", "2022", "2023", "2024")
    kotlin.exclude("2020")
}

sourceSets.test {
    kotlin.srcDirs("common/src/test/kotlin")
}

kotlin {
    jvmToolchain(21)
}
