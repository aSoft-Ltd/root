#!/usr/bin/env kotlin

@file:DependsOn("it.krzeminski:github-actions-kotlin-dsl:0.22.0")
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
import it.krzeminski.githubactions.dsl.StringCustomValue
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
    RootProject("live", "live", listOf("core", "coroutines", "react", "test")),
    RootProject("viewmodel", "viewmodel", listOf("core", "coroutines", "react", "test"))
)

fun JobBuilder.setupAndCheckout(rp: RootProject) {
    uses(CheckoutV3(submodules = true))
    uses(SetupJavaV3("18", Corretto))
    uses(name = "Cache gradle", action = GradleBuildActionV2(buildRootDirectory = "./${rp.path}"))
    run(
        name = "Make ./gradlew executable",
        command = "chmod +x ./gradlew",
        _customArguments = mapOf("working-directory" to StringCustomValue(rp.path))
    )
}

fun WorkflowBuilder.buildProject(rp: RootProject) = job(
    id = "build-${rp.name}", runsOn = MacOSLatest
) {
    setupAndCheckout(rp)
    rp.subs.forEach {
        run(
            name = "build ${rp.name}-$it",
            command = ":${rp.name}-$it:build",
            _customArguments = mapOf(
                "working-directory" to StringCustomValue("./${rp.path}")
            )
        )
    }
}

fun WorkflowBuilder.publishProject(rp: RootProject, after: Job) = job(
    id = "publish-${rp.name}", runsOn = MacOSLatest, needs = listOf(after)
) {
    setupAndCheckout(rp)
    rp.subs.forEach {
//        val argument = ":${rp.name}-$it:publishToSonatype closeAndReleaseStagingRepository"
//        uses(
//            name = "publish ${rp.name}-$it",
//            action = GradleBuildActionV2(arguments = argument, buildRootDirectory = "./${rp.path}")
//        )
        run(
            name = "publish ${rp.name}-$it",
            command = ":${rp.name}-$it:publishToSonatype",
            _customArguments = mapOf(
                "working-directory" to StringCustomValue("./${rp.path}")
            )
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
        "INCLUDE_BUILD" to "true"
    )
) {
//    val buildJobs = projects.map { buildProject(it) }
    val buildJobs = listOf<Job>()
    val rendezvous = job(id = "rendezvous", runsOn = UbuntuLatest, needs = buildJobs) {
        run("""echo "all builds completed. Beginning deployment"""")
    }
    projects.forEach { publishProject(it, rendezvous) }
}

println(workflow.toYaml(addConsistencyCheck = false))