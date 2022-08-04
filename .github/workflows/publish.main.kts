#!/usr/bin/env kotlin

@file:DependsOn("it.krzeminski:github-actions-kotlin-dsl:0.23.0")
@file:Suppress("Since15")

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
    val name: String,
    val path: String,
    val subs: List<String>
)

val projects = listOf(
    RootProject("functions", "functions", listOf("core")),
    RootProject("live", "live", listOf("core", "coroutines", "react", "test")),
    RootProject("viewmodel", "viewmodel", listOf("core"))
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
    id = "${rp.name}-builder", runsOn = MacOSLatest
) {
    setupAndCheckout(rp)
    rp.subs.forEachIndexed { index, it ->

        // build with gradle subprojects so that when building with composite builds kotlinNpmInstall task is skipped
        if (index == 0) uses(
            name = "building ${rp.name}-$it [INCLUDE_BUILD=false]",
            env = linkedMapOf("INCLUDE_BUILD" to "false"),
            action = GradleBuildActionV2(arguments = ":${rp.name}-$it:build", buildRootDirectory = "./${rp.path}")
        )

        uses(
            name = "building ${rp.name}-$it [INCLUDE_BUILD=true]",
            env = linkedMapOf("INCLUDE_BUILD" to "true"),
            action = GradleBuildActionV2(arguments = ":${rp.name}-$it:build", buildRootDirectory = "./${rp.path}")
        )
    }
}

fun WorkflowBuilder.publishProject(rp: RootProject, after: Job) = job(
    id = "${rp.name}-publisher", runsOn = MacOSLatest, needs = listOf(after)
) {
    setupAndCheckout(rp)
    rp.subs.forEachIndexed { index, it ->
        // build with gradle subprojects so that when building with composite builds kotlinNpmInstall task is skipped
        if (index == 0) uses(
            name = "building ${rp.name}-$it [INCLUDE_BUILD=false]",
            env = linkedMapOf("INCLUDE_BUILD" to "false"),
            action = GradleBuildActionV2(arguments = ":${rp.name}-$it:build", buildRootDirectory = "./${rp.path}")
        )
        val argument = ":${rp.name}-$it:publishToSonatype closeAndReleaseStagingRepository"
        uses(
            name = "publishing ${rp.name}-$it [INCLUDE_BUILD=true]",
            env = linkedMapOf("INCLUDE_BUILD" to "true"),
            action = GradleBuildActionV2(arguments = argument, buildRootDirectory = "./${rp.path}")
        )
    }
}

val workflow = workflow(
    name = "Build, Cache then Publish",
    on = listOf(Push(branches = listOf("main"))),
    sourceFile = __FILE__.toPath(),
    env = linkedMapOf(
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