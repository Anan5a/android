/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tools.idea.gradle.structure.model.android

import com.android.tools.idea.gradle.structure.model.PsProject
import com.android.tools.idea.gradle.structure.model.meta.*
import com.android.tools.idea.testing.AndroidGradleTestCase
import com.android.tools.idea.testing.TestProjectPaths
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import java.io.File

class PsBuildTypeTest : AndroidGradleTestCase() {

  private fun <T> ResolvedValue<T>.asTestValue(): T? = (this as? ResolvedValue.Set<T>)?.resolved
  private fun <T> ParsedValue<T>.asTestValue(): T? = (this as? ParsedValue.Set.Parsed<T>)?.value
  private fun <T : Any> T.asParsed(): ParsedValue<T> = ParsedValue.Set.Parsed(value = this)

  fun testProperties() {
    loadProject(TestProjectPaths.PSD_SAMPLE)

    val resolvedProject = myFixture.project
    val project = PsProject(resolvedProject)

    val appModule = project.findModuleByName("app") as PsAndroidModule
    assertThat(appModule, notNullValue())

    val buildType = appModule.findBuildType("release")
    assertThat(buildType, notNullValue()); buildType!!

    val applicationIdSuffix = PsBuildType.BuildTypeDescriptors.applicationIdSuffix.getValue(buildType)
    val debuggable = PsBuildType.BuildTypeDescriptors.debuggable.getValue(buildType)
    // TODO(b/70501607): Decide on val embedMicroApp = PsBuildType.BuildTypeDescriptors.embedMicroApp.getValue(buildType)
    val jniDebuggable = PsBuildType.BuildTypeDescriptors.jniDebuggable.getValue(buildType)
    val minifyEnabled = PsBuildType.BuildTypeDescriptors.minifyEnabled.getValue(buildType)
    val multiDexEnabled = PsBuildType.BuildTypeDescriptors.multiDexEnabled.getValue(buildType)
    // TODO(b/70501607): Decide on val pseudoLocalesEnabled = PsBuildType.BuildTypeDescriptors.pseudoLocalesEnabled.getValue(buildType)
    val renderscriptDebuggable = PsBuildType.BuildTypeDescriptors.renderscriptDebuggable.getValue(buildType)
    val renderscriptOptimLevel = PsBuildType.BuildTypeDescriptors.renderscriptOptimLevel.getValue(buildType)
    // TODO(b/70501607): Decide on val testCoverageEnabled = PsBuildType.BuildTypeDescriptors.testCoverageEnabled.getValue(buildType)
    val versionNameSuffix = PsBuildType.BuildTypeDescriptors.versionNameSuffix.getValue(buildType)
    val zipAlignEnabled = PsBuildType.BuildTypeDescriptors.zipAlignEnabled.getValue(buildType)
    val proGuardFiles = PsBuildType.BuildTypeDescriptors.proGuardFiles.getEditableValues(buildType).map { it.getValue() }

    assertThat(applicationIdSuffix.resolved.asTestValue(), equalTo("suffix"))
    assertThat(applicationIdSuffix.parsedValue.asTestValue(), equalTo("suffix"))

    assertThat(debuggable.resolved.asTestValue(), equalTo(false))
    assertThat(debuggable.parsedValue.asTestValue(), equalTo(false))

    assertThat(jniDebuggable.resolved.asTestValue(), equalTo(false))
    assertThat(jniDebuggable.parsedValue.asTestValue(), equalTo(false))

    assertThat(minifyEnabled.resolved.asTestValue(), equalTo(false))
    assertThat(minifyEnabled.parsedValue.asTestValue(), equalTo(false))

    assertThat(multiDexEnabled.resolved.asTestValue(), nullValue())
    assertThat(multiDexEnabled.parsedValue.asTestValue(), nullValue())

    assertThat(renderscriptDebuggable.resolved.asTestValue(), equalTo(false))
    assertThat(renderscriptDebuggable.parsedValue.asTestValue(), nullValue())

    assertThat(renderscriptOptimLevel.resolved.asTestValue(), equalTo(2))
    assertThat(renderscriptOptimLevel.parsedValue.asTestValue(), equalTo(2))

    assertThat(versionNameSuffix.resolved.asTestValue(), equalTo("vsuffix"))
    assertThat(versionNameSuffix.parsedValue.asTestValue(), equalTo("vsuffix"))

    assertThat(zipAlignEnabled.resolved.asTestValue(), equalTo(true))
    assertThat(zipAlignEnabled.parsedValue.asTestValue(), nullValue())

    assertThat(proGuardFiles.size, equalTo(3))
    assertThat(proGuardFiles[0].resolved.asTestValue(), nullValue())
    // TODO(b/72052622): assertThat(proGuardFiles[0].parsedValue, instanceOf(ParsedValue.Set.Parsed::class.java))
    // TODO(b/72052622): assertThat(
    //  (proGuardFiles[0].parsedValue as ParsedValue.Set.Parsed<File>).dslText?.mode,
    //  equalTo(DslMode.OTHER_UNPARSED_DSL_TEXT)
    //)
    // TODO(b/72052622): assertThat(
    //  (proGuardFiles[0].parsedValue as ParsedValue.Set.Parsed<File>).dslText?.text,
    //  equalTo("getDefaultProguardFile('proguard-android.txt')")
    //)

    // TODO(b/72814329): Resolved values are not yet supported on list properties.
    assertThat(proGuardFiles[1].resolved.asTestValue(), nullValue())
    assertThat(proGuardFiles[1].parsedValue.asTestValue(), equalTo(File("proguard-rules.txt")))

    // TODO(b/72814329): Resolved values are not yet supported on list properties.
    assertThat(proGuardFiles[2].resolved.asTestValue(), nullValue())
    assertThat(proGuardFiles[2].parsedValue.asTestValue(), equalTo(File("proguard-rules2.txt")))
  }

  fun testProperties_defaultResolved() {
    loadProject(TestProjectPaths.PSD_SAMPLE)

    val resolvedProject = myFixture.project
    val project = PsProject(resolvedProject)

    val appModule = project.findModuleByName("app") as PsAndroidModule
    assertThat(appModule, notNullValue())

    val buildType = appModule.findBuildType("debug")
    assertThat(buildType, notNullValue()); buildType!!
    assertFalse(buildType.isDeclared)

    val applicationIdSuffix = PsBuildType.BuildTypeDescriptors.applicationIdSuffix.getValue(buildType)
    val debuggable = PsBuildType.BuildTypeDescriptors.debuggable.getValue(buildType)
    // TODO(b/70501607): Decide on val embedMicroApp = PsBuildType.BuildTypeDescriptors.embedMicroApp.getValue(buildType)
    val jniDebuggable = PsBuildType.BuildTypeDescriptors.jniDebuggable.getValue(buildType)
    val minifyEnabled = PsBuildType.BuildTypeDescriptors.minifyEnabled.getValue(buildType)
    val multiDexEnabled = PsBuildType.BuildTypeDescriptors.multiDexEnabled.getValue(buildType)
    // TODO(b/70501607): Decide on val pseudoLocalesEnabled = PsBuildType.BuildTypeDescriptors.pseudoLocalesEnabled.getValue(buildType)
    val renderscriptDebuggable = PsBuildType.BuildTypeDescriptors.renderscriptDebuggable.getValue(buildType)
    val renderscriptOptimLevel = PsBuildType.BuildTypeDescriptors.renderscriptOptimLevel.getValue(buildType)
    // TODO(b/70501607): Decide on val testCoverageEnabled = PsBuildType.BuildTypeDescriptors.testCoverageEnabled.getValue(buildType)
    val versionNameSuffix = PsBuildType.BuildTypeDescriptors.versionNameSuffix.getValue(buildType)
    val zipAlignEnabled = PsBuildType.BuildTypeDescriptors.zipAlignEnabled.getValue(buildType)

    assertThat(applicationIdSuffix.resolved.asTestValue(), nullValue())
    assertThat(applicationIdSuffix.parsedValue.asTestValue(), nullValue())

    assertThat(debuggable.resolved.asTestValue(), equalTo(true))
    assertThat(debuggable.parsedValue.asTestValue(), nullValue())

    assertThat(jniDebuggable.resolved.asTestValue(), equalTo(false))
    assertThat(jniDebuggable.parsedValue.asTestValue(), nullValue())

    assertThat(minifyEnabled.resolved.asTestValue(), equalTo(false))
    assertThat(minifyEnabled.parsedValue.asTestValue(), nullValue())

    assertThat(multiDexEnabled.resolved.asTestValue(), nullValue())
    assertThat(multiDexEnabled.parsedValue.asTestValue(), nullValue())

    assertThat(renderscriptDebuggable.resolved.asTestValue(), equalTo(false))
    assertThat(renderscriptDebuggable.parsedValue.asTestValue(), nullValue())

    assertThat(renderscriptOptimLevel.resolved.asTestValue(), equalTo(3))
    assertThat(renderscriptOptimLevel.parsedValue.asTestValue(), nullValue())

    assertThat(versionNameSuffix.resolved.asTestValue(), nullValue())
    assertThat(versionNameSuffix.parsedValue.asTestValue(), nullValue())

    assertThat(zipAlignEnabled.resolved.asTestValue(), equalTo(true))
    assertThat(zipAlignEnabled.parsedValue.asTestValue(), nullValue())
  }

  fun testSetProperties() {
    loadProject(TestProjectPaths.PSD_SAMPLE)

    val resolvedProject = myFixture.project
    var project = PsProject(resolvedProject)

    var appModule = project.findModuleByName("app") as PsAndroidModule
    assertThat(appModule, notNullValue())

    val buildType = appModule.findBuildType("release")
    assertThat(buildType, notNullValue()); buildType!!

    buildType.applicationIdSuffix = "new_suffix".asParsed()
    buildType.debuggable = true.asParsed()
    buildType.jniDebuggable = true.asParsed()
    buildType.minifyEnabled = true.asParsed()
    buildType.multiDexEnabled = true.asParsed()
    buildType.multiDexEnabled = false.asParsed()
    buildType.renderscriptDebuggable = true.asParsed()
    buildType.renderscriptOptimLevel = 3.asParsed()
    buildType.versionNameSuffix = "new_vsuffix".asParsed()
    buildType.zipAlignEnabled = false.asParsed()
    val editableProGuardFiles = PsBuildType.BuildTypeDescriptors.proGuardFiles.getEditableValues(buildType)
    editableProGuardFiles[1].setValue(File("a.txt").asParsed())
    editableProGuardFiles[2].setValue(File("b.txt").asParsed())

    fun verifyValues(buildType: PsBuildType, afterSync: Boolean = false) {
      val applicationIdSuffix = PsBuildType.BuildTypeDescriptors.applicationIdSuffix.getValue(buildType)
      val debuggable = PsBuildType.BuildTypeDescriptors.debuggable.getValue(buildType)
      // TODO(b/70501607): Decide on val embedMicroApp = PsBuildType.BuildTypeDescriptors.embedMicroApp.getValue(buildType)
      val jniDebuggable = PsBuildType.BuildTypeDescriptors.jniDebuggable.getValue(buildType)
      val minifyEnabled = PsBuildType.BuildTypeDescriptors.minifyEnabled.getValue(buildType)
      val multiDexEnabled = PsBuildType.BuildTypeDescriptors.multiDexEnabled.getValue(buildType)
      // TODO(b/70501607): Decide on val pseudoLocalesEnabled = PsBuildType.BuildTypeDescriptors.pseudoLocalesEnabled.getValue(buildType)
      val renderscriptDebuggable = PsBuildType.BuildTypeDescriptors.renderscriptDebuggable.getValue(buildType)
      val renderscriptOptimLevel = PsBuildType.BuildTypeDescriptors.renderscriptOptimLevel.getValue(buildType)
      // TODO(b/70501607): Decide on val testCoverageEnabled = PsBuildType.BuildTypeDescriptors.testCoverageEnabled.getValue(buildType)
      val versionNameSuffix = PsBuildType.BuildTypeDescriptors.versionNameSuffix.getValue(buildType)
      val zipAlignEnabled = PsBuildType.BuildTypeDescriptors.zipAlignEnabled.getValue(buildType)
      val proGuardFiles = PsBuildType.BuildTypeDescriptors.proGuardFiles.getEditableValues(buildType).map { it.getValue() }

      assertThat(applicationIdSuffix.parsedValue.asTestValue(), equalTo("new_suffix"))
      assertThat(debuggable.parsedValue.asTestValue(), equalTo(true))
      assertThat(jniDebuggable.parsedValue.asTestValue(), equalTo(true))
      assertThat(minifyEnabled.parsedValue.asTestValue(), equalTo(true))
      assertThat(multiDexEnabled.parsedValue.asTestValue(), equalTo(false))
      assertThat(renderscriptDebuggable.parsedValue.asTestValue(), equalTo(true))
      assertThat(renderscriptOptimLevel.parsedValue.asTestValue(), equalTo(3))
      assertThat(versionNameSuffix.parsedValue.asTestValue(), equalTo("new_vsuffix"))
      assertThat(zipAlignEnabled.parsedValue.asTestValue(), equalTo(false))

      // TODO(b/72814329): Resolved values are not yet supported on list properties.
      assertThat(proGuardFiles[1].resolved.asTestValue(), nullValue())
      assertThat(proGuardFiles[1].parsedValue.asTestValue(), equalTo(File("a.txt")))
      // TODO(b/72814329): Resolved values are not yet supported on list properties.
      assertThat(proGuardFiles[2].resolved.asTestValue(), nullValue())
      assertThat(proGuardFiles[2].parsedValue.asTestValue(), equalTo(File("b.txt")))

      if (afterSync) {
        assertThat(applicationIdSuffix.parsedValue.asTestValue(), equalTo(applicationIdSuffix.resolved.asTestValue()))
        assertThat(debuggable.parsedValue.asTestValue(), equalTo(debuggable.resolved.asTestValue()))
        assertThat(jniDebuggable.parsedValue.asTestValue(), equalTo(jniDebuggable.resolved.asTestValue()))
        assertThat(minifyEnabled.parsedValue.asTestValue(), equalTo(minifyEnabled.resolved.asTestValue()))
        assertThat(multiDexEnabled.parsedValue.asTestValue(), equalTo(multiDexEnabled.resolved.asTestValue()))
        assertThat(renderscriptDebuggable.parsedValue.asTestValue(), equalTo(renderscriptDebuggable.resolved.asTestValue()))
        assertThat(renderscriptOptimLevel.parsedValue.asTestValue(), equalTo(renderscriptOptimLevel.resolved.asTestValue()))
        assertThat(versionNameSuffix.parsedValue.asTestValue(), equalTo(versionNameSuffix.resolved.asTestValue()))
        assertThat(zipAlignEnabled.parsedValue.asTestValue(), equalTo(zipAlignEnabled.resolved.asTestValue()))

        // TODO(b/72814329): assertThat(proGuardFiles[1].parsedValue.asTestValue(), equalTo(proGuardFiles[1].resolved.asTestValue()))
        // TODO(b/72814329): assertThat(proGuardFiles[2].parsedValue.asTestValue(), equalTo(proGuardFiles[2].resolved.asTestValue()))
       }
    }

    verifyValues(buildType)

    appModule.applyChanges()
    requestSyncAndWait()
    project = PsProject(resolvedProject)
    appModule = project.findModuleByName("app") as PsAndroidModule
    // Verify nothing bad happened to the values after the re-parsing.
    verifyValues(appModule.findBuildType("release")!!, afterSync = true)
  }

  @Ignore("b/72853928")
  fun /*test*/SetListReferences() {
    loadProject(TestProjectPaths.PSD_SAMPLE)

    val resolvedProject = myFixture.project
    var project = PsProject(resolvedProject)

    var appModule = project.findModuleByName("app") as PsAndroidModule
    assertThat(appModule, notNullValue())

    val buildType = appModule.findBuildType("release")
    assertThat(buildType, notNullValue()); buildType!!

    PsBuildType.BuildTypeDescriptors.proGuardFiles.setValue(
      buildType,
      ParsedValue.Set.Parsed(
        dslText = DslText(DslMode.REFERENCE, "varProGuardFiles"),
        value = null
      )
    )

    fun verifyValues(buildType: PsBuildType, afterSync: Boolean = false) {
      val proGuardFilesValue = PsBuildType.BuildTypeDescriptors.proGuardFiles.getValue(buildType)
      val parsedProGuardFilesValue = proGuardFilesValue.parsedValue as? ParsedValue.Set.Parsed
      val proGuardFiles = PsBuildType.BuildTypeDescriptors.proGuardFiles.getEditableValues(buildType).map { it.getValue() }

      assertThat(parsedProGuardFilesValue?.dslText?.mode, equalTo(DslMode.REFERENCE))
      assertThat(parsedProGuardFilesValue?.dslText?.text, equalTo("varProGuardFiles"))

      assertThat(proGuardFiles.size, equalTo(2))
      // TODO(b/72814329): Resolved values are not yet supported on list properties.
      assertThat(proGuardFiles[0].resolved.asTestValue(), nullValue())
      assertThat(proGuardFiles[0].parsedValue.asTestValue(), equalTo(File("proguard-rules.txt")))

      // TODO(b/72814329): Resolved values are not yet supported on list properties.
      assertThat(proGuardFiles[1].resolved.asTestValue(), nullValue())
      assertThat(proGuardFiles[1].parsedValue.asTestValue(), equalTo(File("proguard-rules2.txt")))

      if (afterSync) {
        // TODO(b/72814329): assertThat(proGuardFiles[0].parsedValue.asTestValue(), equalTo(proGuardFiles[0].resolved.asTestValue()))
        // TODO(b/72814329): assertThat(proGuardFiles[1].parsedValue.asTestValue(), equalTo(proGuardFiles[1].resolved.asTestValue()))
      }
    }

    verifyValues(buildType)

    appModule.applyChanges()
    requestSyncAndWait()
    project = PsProject(resolvedProject)
    appModule = project.findModuleByName("app") as PsAndroidModule
    // Verify nothing bad happened to the values after the re-parsing.
    verifyValues(appModule.findBuildType("release")!!, afterSync = true)
  }
}
