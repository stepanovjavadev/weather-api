plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Internal SDK dependencies
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.slf4jApi)

    // Tests: JUnit via BOM
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)

    // Mockito
    testImplementation(libs.mockitoCore)
    testImplementation(libs.mockitoJunitJupiter)
}

tasks.test {
    useJUnitPlatform()
}