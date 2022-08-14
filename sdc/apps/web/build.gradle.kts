plugins {
    kotlin("js")
}

kotlin {
    js(IR) {
        browserApp()
    }

    sourceSets {
        val main by getting {
            dependencies {
                implementation(projects.sdcUiCore)
            }
        }
    }
}