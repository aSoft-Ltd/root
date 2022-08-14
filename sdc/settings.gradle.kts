pluginManagement {
    enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    dependencyResolutionManagement {
        versionCatalogs {
            file("gradle/versions").listFiles().map {
                it.nameWithoutExtension to it.absolutePath
            }.forEach { (name, path) ->
                create(name) { from(files(path)) }
            }
        }
    }
}

fun includeRoot(name: String, path: String) {
    include(":$name")
    project(":$name").projectDir = File(path)
}

fun includeSubs(base: String, path: String = base, vararg subs: String) {
    subs.forEach {
        include(":$base-$it")
        project(":$base-$it").projectDir = File("$path/$it")
    }
}

val tmp = 4

rootProject.name = "asoft"

includeBuild("../math/spatial/generator")

// dependencies
includeSubs("functions", "../functions", "core")
includeSubs("expect", "../expect", "core")
includeSubs("math", "../math", "core")
includeSubs("math-spatial", "../math/spatial", "core")
includeSubs("math-point", "../math/point", "core")
includeSubs("math-vector", "../math/vector", "core")

// submodules
includeSubs("sdc", ".", "core")
includeSubs("sdc-ui", "ui", "core")
includeSubs("sdc", "apps", "desktop", "web")