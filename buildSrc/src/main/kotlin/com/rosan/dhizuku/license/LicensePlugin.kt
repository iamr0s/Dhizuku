package com.rosan.dhizuku.license

import groovy.json.JsonOutput

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

private data class PomInfo(
    val name: String,
    val website: String,
    val source: String,
    val licenses: List<Pair<String, String>>
)

class LicensePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val outputDir = project.layout.buildDirectory.dir("generated/licenseData")

        val task = project.tasks.register("generateLicenseData") {
            notCompatibleWithConfigurationCache("Resolves the dependency graph at execution time")
            outputs.dir(outputDir)

            doLast {
                val root = project.configurations.getByName("releaseRuntimeClasspath")
                    .incoming.resolutionResult.root
                val parents = linkedMapOf<String, MutableSet<String>>()
                val components = linkedMapOf<String, ModuleComponentIdentifier>()
                val visited = mutableSetOf<String>()

                fun idOf(component: ResolvedComponentResult): String? =
                    (component.id as? ModuleComponentIdentifier)
                        ?.let { "${it.group}:${it.module}" }

                fun walk(component: ResolvedComponentResult, parent: String?) {
                    val current = idOf(component)
                    if (current != null) {
                        components[current] = component.id as ModuleComponentIdentifier
                        if (!visited.add(current)) return
                    }

                    component.dependencies.forEach { dependency ->
                        if (dependency !is ResolvedDependencyResult) return@forEach
                        val child = idOf(dependency.selected) ?: return@forEach
                        parents.getOrPut(child) { mutableSetOf() }
                            .add(parent ?: "__app__")
                        walk(dependency.selected, child)
                    }
                }

                walk(root, null)

                val poms = resolvePoms(project, components.values)
                val libraries = components.entries.map { (id, component) ->
                    val info = poms[id]?.let(::parsePom)
                    val name: String = info?.name?.takeIf { it.isNotBlank() } ?: component.module
                    val scm: Map<String, String> = mapOf("url" to info?.source.orEmpty())
                    val licenses: List<Map<String, String>> = info?.licenses.orEmpty().map {
                        mapOf("name" to it.first, "url" to it.second)
                    }
                    linkedMapOf<String, Any>(
                        "uniqueId" to id,
                        "name" to name,
                        "artifactVersion" to component.version,
                        "website" to info?.website.orEmpty(),
                        "scm" to scm,
                        "licenses" to licenses
                    )
                }.sortedBy { it["name"] as String }

                val output = outputDir.get().asFile.resolve("raw")
                output.mkdirs()
                output.resolve("licenses.json").writeText(
                    JsonOutput.toJson(mapOf("libraries" to libraries))
                )
                output.resolve("dependencies.json").writeText(
                    JsonOutput.toJson(
                        mapOf(
                            "parents" to parents.toSortedMap()
                                .mapValues { (_, values) -> values.sorted() }
                        )
                    )
                )
            }
        }

        val android = project.extensions.findByName("android") ?: return
        val sourceSets = android.javaClass.getMethod("getSourceSets").invoke(android)
        val main = sourceSets.javaClass.getMethod("getByName", String::class.java)
            .invoke(sourceSets, "main")
        val res = main.javaClass.getMethod("getRes").invoke(main)
        res.javaClass.getMethod("srcDir", Any::class.java)
            .invoke(res, outputDir.get().asFile)

        project.tasks.matching {
            it.name != "generateLicenseData" && (
                it.name.contains("Resources") ||
                    it.name.startsWith("map") ||
                    it.name.startsWith("extract") ||
                    it.name.startsWith("package")
                )
        }.configureEach {
            dependsOn(task)
        }
    }

    private fun resolvePoms(
        project: Project,
        components: Collection<ModuleComponentIdentifier>
    ): Map<String, File> {
        if (components.isEmpty()) return emptyMap()

        val query = project.dependencies.createArtifactResolutionQuery()
            .forComponents(components)
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()

        return buildMap {
            query.resolvedComponents.forEach { component ->
                val id = component.id as? ModuleComponentIdentifier ?: return@forEach
                val artifact = component.getArtifacts(MavenPomArtifact::class.java)
                    .filterIsInstance<ResolvedArtifactResult>()
                    .firstOrNull() ?: return@forEach
                put("${id.group}:${id.module}", artifact.file)
            }
        }
    }

    private fun parsePom(file: File): PomInfo {
        val project = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
            .documentElement

        fun Element.child(tag: String): Element? =
            (0 until childNodes.length)
                .map { childNodes.item(it) }
                .filterIsInstance<Element>()
                .firstOrNull { it.tagName == tag }

        fun Element.text(tag: String): String =
            child(tag)?.textContent?.trim().orEmpty()

        val licenses = project.child("licenses")
            ?.let { node ->
                (0 until node.childNodes.length)
                    .map { node.childNodes.item(it) }
                    .filterIsInstance<Element>()
                    .filter { it.tagName == "license" }
                    .map {
                        it.text("name") to it.text("url")
                    }
            }
            .orEmpty()

        return PomInfo(
            name = project.text("name"),
            website = project.text("url"),
            source = project.child("scm")?.text("url").orEmpty(),
            licenses = licenses
        )
    }
}
