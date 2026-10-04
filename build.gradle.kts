import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("org.jetbrains.compose") version "1.6.10"
    application
}

group = "com.metamonjurul"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    // Compose for Desktop
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.3.0")
    implementation("org.jetbrains.compose.material3:material3-window-size-class:1.3.0")
    
    // Serialization for JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // HTTP Client for Gemini API
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
    implementation("io.ktor:ktor-client-logging:3.0.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")
    
    // CSV handling
    implementation("com.opencsv:opencsv:5.9")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")
    
    // File type detection
    implementation("com.github.jai-imageio:jai-imageio-core:1.4.0")
}

compose.desktop {
    application {
        mainClass = "com.metamonjurul.MainKt"
        nativeDistributions {
            packageTypes = setOf("dmg", "msi", "deb", "tar")
            packageName = "MetaMonjurul"
            description = "Meta Monjurul - Stock Marketplace Metadata Automation"
            vendor = "Meta Monjurul"
            // App icons (replaces the Compose defaults).
            // These files ship in the repo: assets/icon/app_icon.ico (.ico)
            // and assets/icon/app_icon_256.png (.png).
            windows {
                iconFile.set(project.file("assets/icon/app_icon.ico"))
            }
            linux {
                iconFile.set(project.file("assets/icon/app_icon_256.png"))
            }
        }
    }
}

application {
    mainClass.set("com.metamonjurul.MainKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}