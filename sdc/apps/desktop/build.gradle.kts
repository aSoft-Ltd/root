plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("sdc.MainKt")
}

kotlin {
    target {
        application()
    }

    sourceSets {
        val main by getting {
            dependencies {
                implementation(projects.sdcUiCore)
            }
        }
    }
}