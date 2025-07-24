

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    // java application run from Itellej idea and via run command
    id("application")
    jacoco
}


group = "com.loglass"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
    
    // Mockito for mocking
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    
    // MockK for Kotlin testing
    testImplementation("io.mockk:mockk:1.13.8")
    
    // Additional test utilities
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)   // run report after tests
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    // optional: exclude generated / adapter code
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/adapter/**", "**/Main*.class")
            }
        })
    )
}

kotlin {
    jvmToolchain(17)
}
application {
    mainClass.set("javaversion.Main") // For Java main class run

}