package org.jetbrains.plugins.bsp.magicmetamodel.impl.workspacemodel.impl.updaters

import com.intellij.java.workspace.entities.JavaResourceRootPropertiesEntity
import com.intellij.java.workspace.entities.javaResourceRoots
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.SourceRootEntity
import com.intellij.platform.workspace.jps.entities.modifyContentRootEntity
import com.intellij.platform.workspace.jps.entities.modifySourceRootEntity
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.impl.url.toVirtualFileUrl
import org.jetbrains.plugins.bsp.magicmetamodel.impl.workspacemodel.ContentRoot
import org.jetbrains.plugins.bsp.magicmetamodel.impl.workspacemodel.ResourceRoot

internal class JavaResourceEntityUpdater(
  private val workspaceModelEntityUpdaterConfig: WorkspaceModelEntityUpdaterConfig,
) : WorkspaceModelEntityWithParentModuleUpdater<ResourceRoot, JavaResourceRootPropertiesEntity> {
  private val contentRootEntityUpdater = ContentRootEntityUpdater(workspaceModelEntityUpdaterConfig)

  override fun addEntity(
    entityToAdd: ResourceRoot,
    parentModuleEntity: ModuleEntity,
  ): JavaResourceRootPropertiesEntity {
    val contentRootEntity = addContentRootEntity(entityToAdd, parentModuleEntity)

    val sourceRoot = addSourceRootEntity(
      workspaceModelEntityUpdaterConfig.workspaceEntityStorageBuilder,
      contentRootEntity,
      entityToAdd,
      parentModuleEntity,
    )
    return addJavaResourceRootEntity(workspaceModelEntityUpdaterConfig.workspaceEntityStorageBuilder, sourceRoot)
  }

  private fun addContentRootEntity(
    entityToAdd: ResourceRoot,
    parentModuleEntity: ModuleEntity,
  ): ContentRootEntity {
    val contentRoot = ContentRoot(
      path = entityToAdd.resourcePath,
    )

    return contentRootEntityUpdater.addEntity(contentRoot, parentModuleEntity)
  }

  private fun addSourceRootEntity(
    builder: MutableEntityStorage,
    contentRootEntity: ContentRootEntity,
    entityToAdd: ResourceRoot,
    parentModuleEntity: ModuleEntity,
  ): SourceRootEntity {
    val entity = SourceRootEntity(
      url = entityToAdd.resourcePath.toVirtualFileUrl(workspaceModelEntityUpdaterConfig.virtualFileUrlManager),
      rootTypeId = entityToAdd.rootType,
      entitySource = parentModuleEntity.entitySource,
    )

    val updatedContentRootEntity = builder.modifyContentRootEntity(contentRootEntity) {
      this.sourceRoots += entity
    }

    return updatedContentRootEntity.sourceRoots.last()
  }

  private fun addJavaResourceRootEntity(
    builder: MutableEntityStorage,
    sourceRoot: SourceRootEntity,
  ): JavaResourceRootPropertiesEntity {
    val entity = JavaResourceRootPropertiesEntity(
      generated = DEFAULT_GENERATED,
      relativeOutputPath = DEFAULT_RELATIVE_OUTPUT_PATH,
      entitySource = sourceRoot.entitySource,
    )

    val updatedSourceRoot = builder.modifySourceRootEntity(sourceRoot) {
      this.javaResourceRoots += entity
    }

    return updatedSourceRoot.javaResourceRoots.last()
  }

  private companion object {
    private const val DEFAULT_GENERATED = false
    private const val DEFAULT_RELATIVE_OUTPUT_PATH = ""
  }
}
