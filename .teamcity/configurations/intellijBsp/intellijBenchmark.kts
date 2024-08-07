package configurations.intellijBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class Benchmark (
    vcsRoot: GitVcsRoot,
) : BaseConfiguration.BaseBuildType(

    name = "[benchmark] 10 targets",
    vcsRoot = vcsRoot,
    artifactRules = "+:%system.teamcity.build.checkoutDir%/bazel-testlogs/** => testlogs.zip",
    steps = {
        val sysArgs = "-DDO_NOT_REPORT_ERRORS=true  -Dbsp.benchmark.project.path=/tmp/project_10 -Dbsp.benchmark.teamcity.url=https://bazel.teamcity.com"
        script {
            this.name = "install xvfb and generate project for benchmark"
            id = "install_xvfb_and_generate_project_for_benchmark"
            scriptContent = """
                        #!/bin/bash
                        set -euxo pipefail
                        
                        sudo apt-get update
                        sudo apt-get install -y xvfb
                        
                        git clone https://github.com/JetBrains/bazel-bsp.git /tmp/bazel-bsp
                        cd /tmp/bazel-bsp
                        bazel run //bspcli:generator -- /tmp/project_10 10 --targetssize 1
                    """.trimIndent()
        }
        bazel {
            name = "run benchmark"
            id = "run_benchmark"
            command = "test"
            targets = "//performance-testing"
            arguments = """"--jvmopt=-Dorg.gradle.jvmargs=-Xmx12g $sysArgs""""
            param("toolPath", "/usr/local/bin")
        }
    }
)


object GitHub : Benchmark(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object Space : Benchmark(
    vcsRoot = BaseConfiguration.SpaceVcs
)