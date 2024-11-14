plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
}

group = "io.confluent.developer.drift"
version = "0.1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven")
}

dependencies {
    implementation(project(":drift-schema"))
    //implementation("org.apache.avro:avro:1.12.0")
    implementation("io.confluent:kafka-schema-registry-client:7.7.1")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.testcontainers:kafka:1.19.3")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    
}

tasks.withType<Test> {
    useJUnitPlatform()
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
    
    extensions.configure(JacocoTaskExtension::class) {
        classDumpDir = layout.buildDirectory.dir("jacoco/classpathdumps").get().asFile
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.5".toBigDecimal()
            }
        }
    }
}