repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.9.23"
    application
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("io.ktor:ktor-client-core:2.3.9")
    implementation("io.ktor:ktor-client-cio:2.3.9")
    implementation("org.slf4j:slf4j-simple:2.0.12")
}

application {
    mainClass.set("com.example.stocktracker.loadtester.MainKt")
}
