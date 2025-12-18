plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.calebxzhou.gradle"
version = "0.1.0"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("autoEmbedJarJarTransitives") {
            id = "com.calebxzhou.auto-embed-jarjar-transitives"
            implementationClass = "com.calebxzhou.gradle.autoembed.AutoEmbedJarJarTransitivesPlugin"
        }
    }
}

kotlin {
    jvmToolchain(21)
}
