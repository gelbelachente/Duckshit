import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
}
repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
}



tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}