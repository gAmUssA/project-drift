// build.gradle.kts
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.21" apply false
}

allprojects {
    group = "io.confluent.developer.drift"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}