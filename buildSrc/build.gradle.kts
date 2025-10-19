plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", "2.3.0-Beta1"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.3.0-Beta1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testImplementation(kotlin("gradle-plugin", "2.3.0-Beta1"))
    testImplementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.3.0-Beta1")
}

tasks.test {
    useJUnitPlatform()
}
