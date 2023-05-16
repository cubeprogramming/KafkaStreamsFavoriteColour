import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "com.cubeprogramming"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // dependency versions
    val kafkaVersion = "3.4.0"
    val log4jVersion= "2.20.0"

    // kafka
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    implementation("org.apache.kafka:kafka-streams:$kafkaVersion")

    // Logging
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
//    implementation("org.slf4j:slf4j-reload4j:2.0.7")
//    implementation("org.slf4j:slf4j-log4j13:1.0.1")
//    implementation("org.slf4j:log4j-over-slf4j:2.0.7")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}