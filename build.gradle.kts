import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}
configure<ApplicationPluginConvention> {
    mainClassName = "ru.smirnov.muteworkingchats.Main"
}

group = "ru.smirnov"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.tongfei:progressbar:0.10.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("tdlib-mute-working-chats")
    archiveClassifier.set("")
    archiveVersion.set("")
}
