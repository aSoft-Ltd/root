#!/usr/bin/env kotlin

@file:DependsOn("it.krzeminski:github-actions-kotlin-dsl:0.23.0") @file:Suppress("Since15")

import it.krzeminski.githubactions.actions.actions.CheckoutV3
import it.krzeminski.githubactions.actions.actions.SetupJavaV3
import it.krzeminski.githubactions.actions.actions.SetupJavaV3.Distribution.Corretto
import it.krzeminski.githubactions.actions.gradle.GradleBuildActionV2
import it.krzeminski.githubactions.domain.Job
import it.krzeminski.githubactions.domain.RunnerType.MacOSLatest
import it.krzeminski.githubactions.domain.RunnerType.UbuntuLatest
import it.krzeminski.githubactions.domain.triggers.Push
import it.krzeminski.githubactions.dsl.JobBuilder
import it.krzeminski.githubactions.dsl.WorkflowBuilder
import it.krzeminski.githubactions.dsl.expressions.expr
import it.krzeminski.githubactions.dsl.workflow
import it.krzeminski.githubactions.yaml.toYaml

data class RootProject(
    val name: String, val path: String, val subs: List<String>
)

val projects = listOf(
    RootProject("functions", "functions", listOf("core")),
    RootProject("expect", "expect", listOf("core", "coroutines")),
    RootProject("koncurrent-primitives", "koncurrent", listOf("core", "coroutines", "mock")),
    RootProject("koncurrent-later", "koncurrent", listOf("core", "coroutines", "test")),
    RootProject("live", "live", listOf("core", "compose", "coroutines", "react", "test")),
    RootProject("viewmodel", "viewmodel", listOf("core")),
    RootProject("cache", "cache", listOf("api", "browser", "file", "mock", "react-native")),

    RootProject("formatter", "formatter", listOf("core")),
    RootProject("kash", "kash", listOf("currency", "money")),
    RootProject("identifier", "identifier", listOf("core", "generators")),

    RootProject("events", "events", listOf("core", "inmemory", "react")),
    RootProject("response", "response", listOf("core")),

    RootProject("geo", "geo", listOf("core", "languages", "countries")),
    RootProject("krono", "krono", listOf("api")),
    RootProject("presenters", "presenters", listOf("core", "actions", "mock", "krono", "geo")),

    RootProject("mailer", "mailer", listOf("api", "mock", "smtp")),

    RootProject("bitframe-actor", "bitframe", listOf("core", "app", "user", "space")),
    RootProject("bitframe-dao", "bitframe", listOf("core", "mock", "mongo", "file")),
    RootProject("bitframe", "bitframe", listOf("dao")),
    RootProject("bitframe-service-builder", "bitfrmae", listOf("core", "daod", "rest")),
    RootProject("bitframe-service-builder-api", "bitframe", listOf("core", "ktor", "mock")),
    RootProject("bitframe-service-builder-sdk-client", "bitframe", listOf("core", "react" /* "mock",*/)),
    RootProject("bitframe-service-builder-sdk-server", "bitframe", listOf("core")),
    RootProject("bitframe-api", "api", listOf("core" /* "ktor", "mock" */)),
    RootProject("bitframe-sdk-server", "bitframer", listOf("core", "ktor", "test")),

    // math libs
    RootProject("math", "math", listOf("core")),
    RootProject("math-spatial", "math", listOf("core")),
    RootProject("math-vector", "math", listOf("core")),
    RootProject("math-point", "math", listOf("core")),

    RootProject("kida", "kida", listOf("api", "ktor", "core", "fake"))
)

fun JobBuilder.setupAndCheckout(rp: RootProject) {
    uses(CheckoutV3(submodules = true))
    uses(SetupJavaV3("18", Corretto))
    run(
        name = "Make ./gradlew executable",
        command = "chmod +x ./gradlew",
        workingDirectory = rp.path,
    )
}

fun WorkflowBuilder.buildProject(rp: RootProject) = job(
    id = "${rp.name}-builder", runsOn = UbuntuLatest
) {
    setupAndCheckout(rp)
    rp.subs.forEach {
        val task = ":${rp.name}-$it:build"
        uses(
            name = "./gradlew $task", action = GradleBuildActionV2(arguments = task, buildRootDirectory = "./${rp.path}")
        )
    }
}

fun WorkflowBuilder.publishProject(rp: RootProject, after: Job) = job(
    id = "${rp.name}-publisher", runsOn = UbuntuLatest, needs = listOf(after)
) {
    setupAndCheckout(rp)
    rp.subs.forEachIndexed { index, it ->
        val argument = ":${rp.name}-$it:publishToSonatype closeAndReleaseStagingRepository"
        uses(
            name = "publishing ${rp.name}-$it", action = GradleBuildActionV2(arguments = argument, buildRootDirectory = "./${rp.path}")
        )
    }
}

val workflow = workflow(
    name = "Build, Cache then Publish", on = listOf(Push(branches = listOf("main"))), sourceFile = __FILE__.toPath(), env = linkedMapOf(
        "ASOFT_MAVEN_PGP_PRIVATE_KEY" to expr { secrets["ASOFT_MAVEN_PGP_PRIVATE_KEY"].toString() },
        "ASOFT_MAVEN_PGP_PASSWORD" to expr { secrets["ASOFT_MAVEN_PGP_PASSWORD"].toString() },
        "ASOFT_NEXUS_PASSWORD" to expr { secrets["ASOFT_NEXUS_PASSWORD"].toString() },
        "ASOFT_NEXUS_USERNAME" to expr { secrets["ASOFT_NEXUS_USERNAME"].toString() },
    )
) {
    val buildJobs = projects.map { buildProject(it) }
    val rendezvous = job(id = "rendezvous", runsOn = UbuntuLatest, needs = buildJobs) {
        run("""echo "all builds completed. Beginning deployment"""")
    }
    projects.forEach { publishProject(it, rendezvous) }
}

println(workflow.toYaml(addConsistencyCheck = false))