/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.gradle.project.sync

import com.android.builder.model.ProjectSyncIssues
import com.android.builder.model.v2.models.Versions
import com.android.ide.common.repository.GradleCoordinate
import com.android.ide.common.repository.GradleVersion
import com.android.ide.gradle.model.ArtifactIdentifier
import com.android.ide.gradle.model.ArtifactIdentifierImpl
import com.android.ide.gradle.model.LegacyApplicationIdModel
import com.android.ide.gradle.model.LegacyV1AgpVersionModel
import com.android.ide.gradle.model.artifacts.AdditionalClassifierArtifactsModel
import com.android.tools.idea.gradle.model.IdeAndroidProjectType
import com.android.tools.idea.gradle.model.IdeArtifactLibrary
import com.android.tools.idea.gradle.model.IdeSyncIssue
import com.android.tools.idea.gradle.model.IdeBaseArtifactCore
import com.android.tools.idea.gradle.model.IdeLibrary
import com.android.tools.idea.gradle.model.IdeUnresolvedDependency
import com.android.tools.idea.gradle.model.LibraryReference
import com.android.tools.idea.gradle.model.impl.IdeAndroidProjectImpl
import com.android.tools.idea.gradle.model.impl.IdeSyncIssueImpl
import com.android.tools.idea.gradle.model.impl.IdeUnresolvedLibraryTableImpl
import com.android.tools.idea.gradle.model.impl.IdeVariantCoreImpl
import com.android.tools.idea.gradle.model.ndk.v1.IdeNativeAndroidProject
import com.android.tools.idea.gradle.model.ndk.v1.IdeNativeVariantAbi
import com.android.tools.idea.gradle.model.ndk.v2.IdeNativeModule
import com.android.tools.idea.gradle.project.sync.Modules.createUniqueModuleId
import org.gradle.tooling.BuildController
import org.gradle.tooling.model.BuildModel
import org.gradle.tooling.model.Model
import org.gradle.tooling.model.gradle.BasicGradleProject
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.kotlin.idea.gradleTooling.KotlinGradleModel
import org.jetbrains.kotlin.idea.gradleTooling.model.kapt.KaptGradleModel
import org.jetbrains.kotlin.idea.gradleTooling.model.kapt.KaptSourceSetModel
import org.jetbrains.plugins.gradle.model.ProjectImportModelProvider
import java.io.File
import com.android.builder.model.ProjectSyncIssues as ProjectSyncIssuesV1
import com.android.builder.model.v2.models.ProjectSyncIssues as ProjectSyncIssuesV2

typealias IdeVariantFetcher = (
  controller: BuildController,
  androidProjectPathResolver: AndroidProjectPathResolver,
  module: AndroidModule,
  configuration: ModuleConfiguration
) -> IdeVariantWithPostProcessor?

sealed interface GradleModelCollection {
  fun deliverModels(consumer: ProjectImportModelProvider.BuildModelConsumer)
}

class GradleProject(
  private val buildModel: BuildModel,
  private val ideLibraryTable: IdeUnresolvedLibraryTableImpl
) : GradleModelCollection {

  override fun deliverModels(consumer: ProjectImportModelProvider.BuildModelConsumer) {
    with(ModelConsumer(consumer)) {
      ideLibraryTable.deliver()
    }
  }

  private inner class ModelConsumer(val buildModelConsumer: ProjectImportModelProvider.BuildModelConsumer) {
    inline fun <reified T : Any> T.deliver() {
      buildModelConsumer.consume(buildModel, this, T::class.java)
    }
  }
}

sealed class GradleModule(val gradleProject: BasicGradleProject) {
  val findModelRoot: Model get() = gradleProject
  val id = createUniqueModuleId(gradleProject)
  val moduleId: ModuleId = gradleProject.toModuleId()
  var projectSyncIssues: List<IdeSyncIssue> = emptyList(); private set
  fun setSyncIssues(issues: List<IdeSyncIssue>) {
    projectSyncIssues = issues
  }

  /**
   * Prepares the final collection of models for delivery to the IDE.
   */
  abstract fun prepare(indexedModels: IndexedModels): DeliverableGradleModule
  abstract fun getFetchSyncIssuesAction(): ActionToRun<Unit>?
}

/**
 * The final collection of models representing [gradleProject] prepared for consumption by the IDE.
 */
sealed class DeliverableGradleModule(
  val gradleProject: BasicGradleProject,
  val projectSyncIssues: List<IdeSyncIssue>
) : GradleModelCollection {
  protected inner class ModelConsumer(val buildModelConsumer: ProjectImportModelProvider.BuildModelConsumer) {
    inline fun <reified T : Any> T.deliver() {
      buildModelConsumer.consumeProjectModel(gradleProject, this, T::class.java)
    }
  }
}

/**
 * The container class of modules we couldn't fetch using parallel Gradle TAPI API.
 * For now this list has :
 *  - All the non-Android modules
 *  - The android modules using an older AGP version than the minimum supported for V2 sync
 */
sealed class BasicIncompleteGradleModule(
  val gradleProject: BasicGradleProject,
  val buildName: String
) {
  val buildId: BuildId get() = BuildId(gradleProject.projectIdentifier.buildIdentifier.rootDir)
  val projectPath: String get() = gradleProject.path

}

/**
 * The container class of Android modules.
 */
sealed class BasicIncompleteAndroidModule(gradleProject: BasicGradleProject, buildName: String)
  :  BasicIncompleteGradleModule(gradleProject, buildName) {
   abstract val agpVersion: String?
  }


/**
 * The container class of Android modules that can be fetched using V2 builder models.
 */
class BasicV2AndroidModuleGradleProject(gradleProject: BasicGradleProject, buildName: String, val versions: Versions) :
  BasicIncompleteAndroidModule(gradleProject, buildName)
{
  override val agpVersion: String
    get() = versions.agp
}

/**
*  The container class of Android modules that can be fetched using V1 builder models.
 *  legacyV1AgpVersion: The model that contains the agp version used by the AndroidProject. This can be null if the AndroidProject is using
 *  an AGP version lower than the minimum supported version by Android Studio
*/
class BasicV1AndroidModuleGradleProject(
  gradleProject: BasicGradleProject,
  buildName: String,
  val legacyV1AgpVersion: LegacyV1AgpVersionModel?
) :  BasicIncompleteAndroidModule(gradleProject, buildName) {
  override val agpVersion: String?
    get() = legacyV1AgpVersion?.agp
}

/**
 * The container class of non-Android modules.
 */
class BasicNonAndroidIncompleteGradleModule(gradleProject: BasicGradleProject, buildName: String) :
  BasicIncompleteGradleModule(gradleProject, buildName)

/**
 * The container class for Java module, containing its Android models handled by the Android plugin.
 */
class JavaModule(
  gradleProject: BasicGradleProject,
  private val kotlinGradleModel: KotlinGradleModel?,
  private val kaptGradleModel: KaptGradleModel?
) : GradleModule(gradleProject) {
  override fun getFetchSyncIssuesAction(): ActionToRun<Unit>?= null

  override fun prepare(indexedModels: IndexedModels): DeliverableGradleModule {
    return DeliverableJavaModule(gradleProject, projectSyncIssues, kotlinGradleModel, kaptGradleModel)
  }
}

class DeliverableJavaModule(
  gradleProject: BasicGradleProject,
  projectSyncIssues: List<IdeSyncIssue>,
  private val kotlinGradleModel: KotlinGradleModel?,
  private val kaptGradleModel: KaptGradleModel?
): DeliverableGradleModule(gradleProject, projectSyncIssues) {
  override fun deliverModels(consumer: ProjectImportModelProvider.BuildModelConsumer) {
    with(ModelConsumer(consumer)) {
      kotlinGradleModel?.deliver()
      kaptGradleModel?.deliver()
    }
  }
}

/**
 * The container class for Android module, containing its Android model, Variant models, and dependency modules.
 */
@VisibleForTesting
sealed class AndroidModule constructor(
  val agpVersion: GradleVersion?,
  val buildName: String,
  val buildNameMap: Map<String, BuildId>,
  val buildIdMap: Map<BuildId, String>,
  gradleProject: BasicGradleProject,
  val androidProject: IdeAndroidProjectImpl,
  /** All configured variant names if supported by the AGP version. */
  val allVariantNames: Set<String>?,
  val defaultVariantName: String?,
  val variantFetcher: IdeVariantFetcher,
  /** Old V1 model. It's only set if [nativeModule] is not set. */
  val androidVariantResolver: AndroidVariantResolver,
  private val nativeAndroidProject: IdeNativeAndroidProject?,
  /** New V2 model. It's only set if [nativeAndroidProject] is not set. */
  private val nativeModule: IdeNativeModule?,
  val legacyApplicationIdModel: LegacyApplicationIdModel?,
) : GradleModule(gradleProject) {
  val projectType: IdeAndroidProjectType get() = androidProject.projectType

  fun getVariantAbiNames(variantName: String): Collection<String>? {
    return nativeModule?.variants?.firstOrNull { it.name == variantName }?.abis?.map { it.name }
           ?: nativeAndroidProject?.variantInfos?.get(variantName)?.abiNames
  }

  enum class NativeModelVersion { None, V1, V2 }

  val nativeModelVersion: NativeModelVersion = when {
    nativeModule != null -> NativeModelVersion.V2
    nativeAndroidProject != null -> NativeModelVersion.V1
    else -> NativeModelVersion.None
  }

  var syncedVariant: IdeVariantWithPostProcessor? = null
  var syncedNativeVariantAbiName: String? = null
  var syncedNativeVariant: IdeNativeVariantAbi? = null
  var allVariants: List<IdeVariantWithPostProcessor>? = null

  var additionalClassifierArtifacts: AdditionalClassifierArtifactsModel? = null
  var kotlinGradleModel: KotlinGradleModel? = null
  var kaptGradleModel: KaptGradleModel? = null

  var unresolvedDependencies: List<IdeUnresolvedDependency> = emptyList()

  /** Returns the list of all libraries this currently selected variant depends on (and temporarily maybe some
   * libraries other variants depend on).
   **/
  fun getLibraryDependencies(libraryResolver: (LibraryReference) -> IdeLibrary): Collection<ArtifactIdentifier> {
    return collectIdentifiers(listOfNotNull(syncedVariant?.variant), libraryResolver)
  }

  class V1(
    agpVersion: GradleVersion?,
    buildName: String,
    buildNameMap: Map<String, BuildId>,
    buildIdMap: Map<BuildId, String>,
    gradleProject: BasicGradleProject,
    androidProject: IdeAndroidProjectImpl,
    /** All configured variant names if supported by the AGP version. */
    allVariantNames: Set<String>?,
    defaultVariantName: String?,
    variantFetcher: IdeVariantFetcher,
    /** Old V1 native model. It's only set if [nativeModule] is not set. */
    nativeAndroidProject: IdeNativeAndroidProject?,
    /** New V2 native model. It's only set if [nativeAndroidProject] is not set. */
    nativeModule: IdeNativeModule?,
    legacyApplicationIdModel: LegacyApplicationIdModel?,
  ) : AndroidModule(
    agpVersion = agpVersion,
    buildName = buildName,
    buildNameMap = buildNameMap,
    buildIdMap = buildIdMap,
    gradleProject = gradleProject,
    androidProject = androidProject,
    /** All configured variant names if supported by the AGP version. */
    allVariantNames = allVariantNames,
    defaultVariantName = defaultVariantName,
    variantFetcher = variantFetcher,
    androidVariantResolver = AndroidVariantResolver.NONE,
    /** Old V1 model. It's only set if [nativeModule] is not set. */
    nativeAndroidProject = nativeAndroidProject,
    /** New V2 model. It's only set if [nativeAndroidProject] is not set. */
    nativeModule = nativeModule,
    legacyApplicationIdModel = legacyApplicationIdModel,
  ) {
    override fun getFetchSyncIssuesAction(): ActionToRun<Unit> {
      return ActionToRun(
        fun(controller: BuildController) {
          val syncIssues =
            controller.findModel(this.findModelRoot, ProjectSyncIssuesV1::class.java)?.syncIssues?.toSyncIssueData()

          if (syncIssues != null) {
            // These would have been attached above if there is no separate sync issue model.
            val legacyApplicationIdModelProblems = this.legacyApplicationIdModel.getProblemsAsSyncIssues()
            this.setSyncIssues(syncIssues + legacyApplicationIdModelProblems)
          }
        },
        fetchesV1Models = true
      )
    }
  }

  class V2(
    agpVersion: GradleVersion?,
    buildName: String,
    buildNameMap: Map<String, BuildId>,
    buildIdMap: Map<BuildId, String>,
    gradleProject: BasicGradleProject,
    androidProject: IdeAndroidProjectImpl,
    allVariantNames: Set<String>,
    defaultVariantName: String?,
    variantFetcher: IdeVariantFetcher,
    androidVariantResolver: AndroidVariantResolver,
    nativeModule: IdeNativeModule?,
    legacyApplicationIdModel: LegacyApplicationIdModel?,
  ) : AndroidModule(
    agpVersion = agpVersion,
    buildName = buildName,
    buildNameMap = buildNameMap,
    buildIdMap = buildIdMap,
    gradleProject = gradleProject,
    androidProject = androidProject,
    allVariantNames = allVariantNames,
    defaultVariantName = defaultVariantName,
    variantFetcher = variantFetcher,
    androidVariantResolver = androidVariantResolver,
    /** Old V1 model. Not used with V2. */
    nativeAndroidProject = null,
    nativeModule = nativeModule,
    legacyApplicationIdModel = legacyApplicationIdModel,
  ) {
    override fun getFetchSyncIssuesAction(): ActionToRun<Unit> {
      return ActionToRun(
        fun(controller: BuildController) {
          val syncIssues =
            controller.findModel(this.findModelRoot, ProjectSyncIssuesV2::class.java)?.syncIssues?.toV2SyncIssueData() ?: listOf()
          val legacyApplicationIdModelProblems = this.legacyApplicationIdModel.getProblemsAsSyncIssues()
          // For V2: we do not populate SyncIssues with Unresolved dependencies because we pass them through builder models.
          val v2UnresolvedDependenciesIssues = this.unresolvedDependencies.map {
            IdeSyncIssueImpl(
              message = "Unresolved dependencies",
              data = it.name,
              multiLineMessage = it.cause?.lines(),
              severity = IdeSyncIssue.SEVERITY_ERROR,
              type = IdeSyncIssue.TYPE_UNRESOLVED_DEPENDENCY
            )
          }
          this.setSyncIssues(syncIssues + v2UnresolvedDependenciesIssues + legacyApplicationIdModelProblems)
        },
        fetchesV2Models = true,
        fetchesKotlinModels = false
      )

    }
  }

  override fun prepare(indexedModels: IndexedModels): DeliverableGradleModule {
    fun IdeAndroidProjectImpl.populateBaseFeature(): IdeAndroidProjectImpl {
      return if (this.projectType != IdeAndroidProjectType.PROJECT_TYPE_DYNAMIC_FEATURE) this
      else copy(baseFeature = indexedModels.dynamicFeatureToBaseFeatureMap[moduleId]?.gradlePath)
    }

    // For now, use one model cache per module. It is does deliver the smallest memory footprint, but this is what we get after
    // models are deserialized from the DataNode cache anyway. This will be replaced with a model cache per sync when shared libraries
    // are moved out of `IdeAndroidProject` and delivered to the IDE separately.
    val selectedVariantName =
      syncedVariant?.name
        ?: allVariants?.map { it.name }?.getDefaultOrFirstItem("debug")
        ?: throw AndroidSyncException(
          "No variants found for '${gradleProject.path}'. Check build files to ensure at least one variant exists.")
    return DeliverableAndroidModule(
      gradleProject = gradleProject,
      projectSyncIssues = projectSyncIssues,
      selectedVariantName = selectedVariantName,
      selectedAbiName = syncedNativeVariantAbiName,
      androidProject = androidProject.patchForKapt(kaptGradleModel).populateBaseFeature(),
      fetchedVariants = (syncedVariant?.let { listOf(it) } ?: allVariants.orEmpty()).map { it.postProcess() }.patchForKapt(kaptGradleModel),
      nativeModule = nativeModule,
      nativeAndroidProject = nativeAndroidProject,
      syncedNativeVariant = syncedNativeVariant,
      kotlinGradleModel = kotlinGradleModel,
      kaptGradleModel = kaptGradleModel,
      additionalClassifierArtifacts = additionalClassifierArtifacts
    )
  }
}

class DeliverableAndroidModule(
  gradleProject: BasicGradleProject,
  projectSyncIssues: List<IdeSyncIssue>,
  val selectedVariantName: String,
  val selectedAbiName: String?,
  val androidProject: IdeAndroidProjectImpl,
  val fetchedVariants: List<IdeVariantCoreImpl>,
  val nativeModule: IdeNativeModule?,
  val nativeAndroidProject: IdeNativeAndroidProject?,
  val syncedNativeVariant: IdeNativeVariantAbi?,
  val kotlinGradleModel: KotlinGradleModel?,
  val kaptGradleModel: KaptGradleModel?,
  val additionalClassifierArtifacts: AdditionalClassifierArtifactsModel?
): DeliverableGradleModule(gradleProject, projectSyncIssues) {
  override fun deliverModels(consumer: ProjectImportModelProvider.BuildModelConsumer) {

    val ideAndroidModels = IdeAndroidModels(
      androidProject,
      fetchedVariants,
      selectedVariantName,
      selectedAbiName,
      projectSyncIssues,
      nativeModule,
      nativeAndroidProject,
      syncedNativeVariant,
      kaptGradleModel
    )
    with(ModelConsumer(consumer)) {
      ideAndroidModels.deliver()
      kotlinGradleModel?.deliver()
      additionalClassifierArtifacts?.deliver()
    }
  }
}

private fun IdeAndroidProjectImpl.patchForKapt(kaptModel: KaptGradleModel?) = copy(isKaptEnabled = kaptModel?.isEnabled ?: false)

private fun List<IdeVariantCoreImpl>.patchForKapt(kaptModel: KaptGradleModel?): List<IdeVariantCoreImpl> {
  if (kaptModel == null) return this
  val sourceSets = kaptModel.sourceSets.associateBy { it.sourceSetName }
  return map { variant ->

    fun <T> T.maybePatch(suffix: String, code: T.(KaptSourceSetModel) -> T): T {
      val kaptSourceSet = sourceSets[variant.name + suffix] ?: return this
      return code(kaptSourceSet)
    }

    fun IdeBaseArtifactCore.generatedSourceFoldersPatchedForKapt(kaptSourceSet: KaptSourceSetModel): List<File> {
      return (generatedSourceFolders.toSet() + listOfNotNull(kaptSourceSet.generatedKotlinSourcesDirFile)).toList()
    }

    variant.copy(
      mainArtifact = variant.mainArtifact
        .maybePatch("") { copy(generatedSourceFolders = generatedSourceFoldersPatchedForKapt(it)) },
      androidTestArtifact = variant.androidTestArtifact
        ?.maybePatch("AndroidTest") { copy(generatedSourceFolders = generatedSourceFoldersPatchedForKapt(it)) },
      unitTestArtifact = variant.unitTestArtifact
        ?.maybePatch("UnitTest") { copy(generatedSourceFolders = generatedSourceFoldersPatchedForKapt(it)) },
      testFixturesArtifact = variant.testFixturesArtifact
        ?.maybePatch("TestFixtures") { copy(generatedSourceFolders = generatedSourceFoldersPatchedForKapt(it)) }
    )
  }
}

data class ModuleConfiguration(val id: String, val variant: String, val abi: String?)

class NativeVariantsAndroidModule private constructor(
  gradleProject: BasicGradleProject,
  private val nativeVariants: List<IdeNativeVariantAbi>? // Null means V2.
) : GradleModule(gradleProject) {
  companion object {
    fun createV2(gradleProject: BasicGradleProject): NativeVariantsAndroidModule = NativeVariantsAndroidModule(gradleProject, null)
    fun createV1(gradleProject: BasicGradleProject, nativeVariants: List<IdeNativeVariantAbi>): NativeVariantsAndroidModule =
      NativeVariantsAndroidModule(gradleProject, nativeVariants)
  }

  override fun getFetchSyncIssuesAction(): ActionToRun<Unit> {
    return ActionToRun(
      fun(controller: BuildController) {
        val syncIssues =
          controller.findModel(this.findModelRoot, ProjectSyncIssues::class.java)?.syncIssues?.toSyncIssueData()

        if (syncIssues != null) {
          this.setSyncIssues(syncIssues)
        }
      },
      fetchesV1Models = true
    )
  }

  override fun prepare(indexedModels: IndexedModels): DeliverableGradleModule {
    return DeliverableNativeVariantsAndroidModule(gradleProject, projectSyncIssues, nativeVariants)
  }
}

class DeliverableNativeVariantsAndroidModule(
  gradleProject: BasicGradleProject,
  projectSyncIssues: List<IdeSyncIssue>,
  private val nativeVariants: List<IdeNativeVariantAbi>? // Null means V2.
) : DeliverableGradleModule(gradleProject, projectSyncIssues) {
  override fun deliverModels(consumer: ProjectImportModelProvider.BuildModelConsumer) {
    with(ModelConsumer(consumer)) {
      IdeAndroidNativeVariantsModels(nativeVariants, projectSyncIssues).deliver()
    }
  }
}

fun Collection<String>.getDefaultOrFirstItem(defaultValue: String): String? =
  if (contains(defaultValue)) defaultValue else minByOrNull { it }

private fun collectIdentifiers(
  variants: Collection<IdeVariantCoreImpl>,
  libraryResolver: (LibraryReference) -> IdeLibrary
): List<ArtifactIdentifier> {
  return variants.asSequence()
    .flatMap {
      sequenceOf(
        it.mainArtifact.compileClasspathCore,
        it.androidTestArtifact?.compileClasspathCore,
        it.unitTestArtifact?.compileClasspathCore,
        it.testFixturesArtifact?.compileClasspathCore
      )
        .filterNotNull()
    }
    .flatMap { it.dependencies.asSequence() }
    .mapNotNull { (libraryResolver(it.target) as? IdeArtifactLibrary)?.artifactAddress }
    .mapNotNull { GradleCoordinate.parseCoordinateString(it) }
    .map { ArtifactIdentifierImpl(it.groupId, it.artifactId, it.version?.toString().orEmpty()) }
    .distinct()
    .toList()
}

