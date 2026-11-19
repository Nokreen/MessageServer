plugins {
    kotlin("jvm") version "2.1.0" // если у тебя тут другая версия, оставь свою!
    kotlin("plugin.serialization") version "2.1.0" // Добавили плагин для JSON
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3") // Сама библиотека
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21) // Оставь ту версию, которая у тебя была (17 или 21)
}