plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "calebxzhou.gradle.plugins"
version = "0.1.0"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("autoEmbedJarJarTransitives") {
            id = "auto-embed-jarjar-transitives"
            implementationClass = "calebxzhou.gradle.plugins.AutoEmbedJarJarTransitivesPlugin"
        }
    }
}

kotlin {
    jvmToolchain(21)
}
// Optional but recommended: Better artifact name
tasks.jar {
    archiveBaseName.set("auto-embed-jarjar-transitives")  // Or your mod name
}
// === Register sourcesJar and javadocJar tasks FIRST ===
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

// === Publishing configuration ===
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            // Now we can safely reference the registered tasks
            artifact(sourcesJar.get()) {
                classifier = "sources"  // Optional: some tools expect this
            }
            artifact(javadocJar.get()) {
                classifier = "javadoc"
            }
        }
    }
}