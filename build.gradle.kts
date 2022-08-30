import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.tasks.BuildSearchableOptionsTask

plugins {
    // Java support
    id("java")
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.8.1"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    compileOnly("io.thundra.merloc:merloc-aws-lambda-runtime-embedded:0.0.5")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(pluginName_)
    version.set(platformVersion)
    type.set(platformType)
    downloadSources.set(platformDownloadSources.toBoolean())
    updateSinceUntilBuild.set(true)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    //  https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html
    plugins.set(platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true
    autoCorrect = true

    reports {
        html.enabled = false
        xml.enabled = false
        txt.enabled = false
    }
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDirs("src/main/java")
    }
}

changelog {
    version.set(pluginVersion)
    header.set(provider { "[${version.get()}] - ${date("yyyy-MM-dd")}" })
}

tasks {
    // Set the compatibility versions to 11
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    withType<BuildSearchableOptionsTask> {
        enabled = false
    }

    withType<Detekt> {
        jvmTarget = "11"
    }

    task("copyResources", Copy::class) {
        configurations.compileClasspath.get()
            .filter {
                it.name.startsWith("merloc-aws-lambda-runtime-embedded")
                        && it.name.endsWith(".jar") }
            .forEach {
                from(it.absolutePath)
                    .rename {  "merloc-aws-lambda-runtime-embedded.jar"}
                    .into(layout.buildDirectory.dir("idea-sandbox/plugins/merloc/resources")) }
    }

    prepareSandbox {
        finalizedBy(named("copyResources"))
    }

    verifyPlugin {
        dependsOn(named("copyResources"))
    }

    buildPlugin {
        dependsOn(named("copyResources"))
    }

    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)
        untilBuild.set(pluginUntilBuild)

        var descriptionContent = project.file("./README.md").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n")

        descriptionContent = descriptionContent.replace(
                "assets/",
                "https://raw.githubusercontent.com/thundra-io/merloc-intellij-plugin/master/assets/")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(descriptionContent.run { markdownToHTML(this) })

        // Get the latest available change notes from the changelog file
        changeNotes.set(
            provider {
                changelog.getLatest().toHTML()
            }
        )
    }

    runPluginVerifier {
        ideVersions.set(pluginVerifierIdeVersions.split(',').map(String::trim).filter(String::isNotEmpty))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
