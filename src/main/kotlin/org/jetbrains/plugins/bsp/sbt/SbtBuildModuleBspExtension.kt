package org.jetbrains.plugins.bsp.sbt

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.SbtBuildTarget
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

public interface SbtBuildModuleBspExtension {
    public fun enrichBspSbtModule(
        sbtBuildTarget: SbtBuildTarget,
        sbtBuildTargetId: BuildTargetIdentifier,
        project: Project
    )
}
private val ep =
    ExtensionPointName.create<SbtBuildModuleBspExtension>(
        "org.jetbrains.bsp.sbtBuildModuleBspExtension",
    )

internal fun sbtBuildModuleBspExtension(): SbtBuildModuleBspExtension? =
    ep.extensionList.firstOrNull()

internal fun sbtBuildModuleBspExtensionExists(): Boolean =
    ep.extensionList.isNotEmpty()
