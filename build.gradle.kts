plugins {
    java
    id("org.springframework.boot") version "4.1.2"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// Set to test an unreleased spring-cloud-context installed in the local Maven repository,
// e.g. -PspringCloudContextVersion=5.0.5-SNAPSHOT
val springCloudContextVersion = findProperty("springCloudContextVersion") as String?

repositories {
    if (springCloudContextVersion != null) {
        mavenLocal()
    }
    mavenCentral()
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (springCloudContextVersion != null && requested.group == "org.springframework.cloud" && requested.name == "spring-cloud-context") {
            useVersion(springCloudContextVersion)
        }
    }
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${findProperty("springCloudVersion") ?: "2025.1.4"}"))

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.cloud:spring-cloud-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
