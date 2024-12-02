// build.gradle.kts
plugins {
    `java-library`
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    id("maven-publish")
    id("idea")
    kotlin("jvm") version "2.0.21"
    jacoco
}

group = "io.confluent.developer.drift"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation("org.apache.avro:avro:1.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.21")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
}

// Configure source sets
sourceSets {
    main {
        resources {
            srcDir("src/main/avro")
        }
    }
}

// Configure Avro plugin
avro {
    isCreateSetters.set(true)
    isCreateOptionalGetters.set(false)
    isGettersReturnOptional.set(false)
    fieldVisibility.set("PRIVATE")
    outputCharacterEncoding.set("UTF-8")
    stringType.set("String")
}

// Configure generateAvroJava task
tasks.named<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask>("generateAvroJava") {
    source("src/main/avro")
}

// Configure test execution
tasks.test {
    useJUnitPlatform()
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("DRIFT Schema")
                description.set("Schema definitions for DRIFT ride sharing application")
            }
        }
    }

    repositories {
        maven {
            name = "LocalRepo"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

// IDEA plugin configuration
idea {
    module {
        generatedSourceDirs.add(file("build/generated-main-avro-java"))
    }
}

// Optional: Add a task to validate schemas
tasks.register("validateSchemas") {
    group = "verification"
    description = "Validates Avro schema files"

    doLast {
        fileTree("src/main/avro").matching {
            include("**/*.avsc")
        }.forEach { schemaFile ->
            logger.info("Validating schema: ${schemaFile.name}")
            org.apache.avro.Schema.Parser().parse(schemaFile)
        }
    }
}

// Make sure generateAvroJava runs before compile
tasks.named("compileJava") {
    dependsOn("generateAvroJava")
}

tasks.named("compileKotlin") {
    dependsOn("generateAvroJava")
}

kotlin {
    jvmToolchain(17)
}

tasks.jacocoTestReport {
    reports {
        csv.required.set(true)
        xml.required.set(true)
        html.required.set(true)
    }
    dependsOn(tasks.test)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)

    // Enable JaCoCo coverage
    extensions.configure(JacocoTaskExtension::class) {
        classDumpDir = layout.buildDirectory.dir("jacoco/classpathdumps").get().asFile
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.5".toBigDecimal()  // 50% coverage minimum
            }
        }
    }
}