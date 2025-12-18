- Designed for Minecraft modding
- When you add external libraries to your mod,
you will use jarJar task to embed them in your built jar, but the jarJar task will not add transitive dependencies (dependency of a dependency) to the built jar which causing ClassNotFoundException as you run the game in release.
- This plugin will automatically resolve and add transitive dependencies to your built jar.
- Usage: clone the code, run publishToMavenLocal then add id("auto-embed-jarjar-transitives") to plugin section of build.gradle in your mod project. make sure your settings.gradle has mavenLocal repository for pluginManagement.