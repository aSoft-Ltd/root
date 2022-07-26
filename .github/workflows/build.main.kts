#!/usr/bin/env kotlin

@file:DependsOn("it.krzeminski:github-actions-kotlin-dsl:0.22.0")
@file:Suppress("Since15")

import it.krzeminski.githubactions.actions.actions.CheckoutV3
import it.krzeminski.githubactions.actions.actions.SetupJavaV3
import it.krzeminski.githubactions.actions.actions.SetupJavaV3.Distribution.Corretto
import it.krzeminski.githubactions.domain.RunnerType.MacOSLatest
import it.krzeminski.githubactions.domain.triggers.Push
import it.krzeminski.githubactions.dsl.StringCustomValue
import it.krzeminski.githubactions.dsl.WorkflowBuilder
import it.krzeminski.githubactions.dsl.workflow
import it.krzeminski.githubactions.yaml.toYaml

fun WorkflowBuilder.submodule(name: String) = job(id = name, runsOn = MacOSLatest) {
    uses(CheckoutV3(submodules = true))
    uses(SetupJavaV3("18", Corretto))
    run(
        command = "./gradlew build",
        _customArguments = mapOf("working-directory" to StringCustomValue(name))
    )
}

val workflow = workflow(
    name = "Build Libs",
    on = listOf(Push(branches = listOf("dev"))),
    sourceFile = __FILE__.toPath(),
) {
    submodule("live")
    submodule("viewmodel")
}

println(workflow.toYaml(addConsistencyCheck = false))