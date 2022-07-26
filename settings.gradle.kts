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

rootProject.name = "stdlib"

// includeSubs("live", "mvivm/live", "core", "react", "coroutines")// "compose")
// includeSubs("viewmodel", "mvivm/viewmodel", "core", "react", "coroutines")// "compose")
// includeSubs("viewmodel-test", "mvivm/viewmodel/test", "core", "expect")

includeRoot("formatter", "formatter")
includeSubs("identifier", "identifier", "core", "generators")

includeSubs("color", "aesthetics/color", "core", "css")// "compose")
includeSubs("theme", "aesthetics/theme", "core", "css", "react")//"compose",)
includeSubs("reakt", "reakt", "core", "web", "icons")

includeSubs("kash", "finance/kash", "core")
includeSubs("payments-requests", "finance/payments/requests", "core")