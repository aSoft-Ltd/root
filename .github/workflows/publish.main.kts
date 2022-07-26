#!/usr/bin/env kotlin

@file:DependsOn("it.krzeminski:github-actions-kotlin-dsl:0.22.0")
@file:Suppress("Since15")

import it.krzeminski.githubactions.actions.actions.CheckoutV3
import it.krzeminski.githubactions.actions.actions.SetupJavaV3
import it.krzeminski.githubactions.actions.actions.SetupJavaV3.Distribution.Corretto
import it.krzeminski.githubactions.actions.gradle.GradleBuildActionV2
import it.krzeminski.githubactions.domain.RunnerType.MacOSLatest
import it.krzeminski.githubactions.domain.RunnerType.UbuntuLatest
import it.krzeminski.githubactions.domain.triggers.Push
import it.krzeminski.githubactions.dsl.StringCustomValue
import it.krzeminski.githubactions.dsl.WorkflowBuilder
import it.krzeminski.githubactions.dsl.workflow
import it.krzeminski.githubactions.yaml.toYaml

fun WorkflowBuilder.submoduleJob(
    prefix: String,
    project: String,
    task: String
) = job(id = "$prefix-$project", runsOn = MacOSLatest) {
    uses(CheckoutV3(submodules = true))
    uses(SetupJavaV3("18", Corretto))
    run(
        command = "chmod +x ./gradlew",
        _customArguments = mapOf("working-directory" to StringCustomValue(project))
    )
    uses(GradleBuildActionV2(arguments = task, buildRootDirectory = "./$project"))
}

fun WorkflowBuilder.buildJob(project: String) = submoduleJob("build", project, "build")

fun WorkflowBuilder.publishJob(project: String) = submoduleJob("publish", project, "publishToSonatype closeAndReleaseStagingRepository")

val workflow = workflow(
    name = "Build And Publish",
    on = listOf(Push(branches = listOf("main"))),
    sourceFile = __FILE__.toPath(),
) {
    val projects = listOf("live", "viewmodel")
    val buildJobs = projects.map { project -> buildJob(project) }
    job(id = "rendezvous", runsOn = UbuntuLatest, needs = buildJobs) {
        run("""echo "all builds completed. Beginning deployment"""")
    }
    projects.map { project -> publishJob(project) }
}

println(workflow.toYaml(addConsistencyCheck = false))