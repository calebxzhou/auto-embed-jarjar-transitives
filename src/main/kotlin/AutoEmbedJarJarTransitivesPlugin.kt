package com.calebxzhou.gradle.autoembed

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.dsl.DependencyHandler
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class AutoEmbedJarJarTransitivesPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.extensions.create(
			EXTENSION_NAME,
			AutoEmbedJarJarTransitivesExtension::class.java,
			project
		)
	}

	companion object {
		const val EXTENSION_NAME = "autoEmbedJarJarTransitives"
	}
}

open class AutoEmbedJarJarTransitivesExtension @Inject constructor(
	private val project: Project
) {
	val jarJarExcludeCoords: MutableSet<String> = LinkedHashSet()

	private val visitedNotations = ConcurrentHashMap.newKeySet<String>()

	fun include(dependencyNotation: String) {
		includeInternal(dependencyNotation)
	}

	fun includeAll(notations: Iterable<String>) {
		notations.forEach { includeInternal(it) }
	}

	private fun includeInternal(notation: String) {
		val coordinateKey = notation.split(":").take(2).joinToString(":").ifBlank { notation }
		if (!visitedNotations.add(notation)) {
			project.logger.info("$coordinateKey 有了")
			return
		}

		val directArtifacts = resolveArtifacts(notation, transitive = false)
		val shouldSkip = coordinateKey in jarJarExcludeCoords ||
			directArtifacts.any { artifact ->
				artifact.moduleKey() in jarJarExcludeCoords
			}

		if (shouldSkip) {
			project.logger.info("$coordinateKey 不要")
			return
		}

		addJarJarDependency(notation)
		project.logger.info("$coordinateKey 要")

		val transitiveArtifacts = resolveArtifacts(notation, transitive = true)
		transitiveArtifacts.forEach { artifact ->
			val artifactKey = artifact.moduleKey()
			if (artifactKey in jarJarExcludeCoords) {
				project.logger.info("$artifactKey 不要")
				return@forEach
			}
			val classifierSuffix = artifact.classifier?.let { ":$it" } ?: ""
			val artifactNotation = "${artifact.moduleVersion.id.group}:${artifact.moduleVersion.id.name}:${artifact.moduleVersion.id.version}$classifierSuffix"
			includeInternal(artifactNotation)
		}
	}

	private fun addJarJarDependency(notation: String) {
		val dependency = project.dependencies.create(notation)
		if (dependency is ModuleDependency) {
			dependency.isTransitive = false
		}
		project.dependencies.add("jarJar", dependency)
	}

	private fun resolveArtifacts(
		notation: String,
		transitive: Boolean
	): Set<ResolvedArtifact> {
		val dependency = project.dependencies.create(notation)
		val configuration = project.configurations.detachedConfiguration(dependency)
		configuration.isTransitive = transitive
		return configuration.resolvedConfiguration.resolvedArtifacts
	}

	private fun ResolvedArtifact.moduleKey(): String {
		val moduleId = moduleVersion.id
		return "${moduleId.group}:${moduleId.name}"
	}
}

fun DependencyHandler.autoEmbedJarJarTransitives(
	extension: AutoEmbedJarJarTransitivesExtension,
	notation: String
) {
	extension.include(notation)
}
