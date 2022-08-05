/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.android.tools.idea.gradle.model.impl

import com.android.tools.idea.gradle.model.IdeModuleDependency
import com.android.tools.idea.gradle.model.IdeModuleLibrary
import org.jetbrains.annotations.TestOnly
import java.io.Serializable

data class IdeModuleDependencyImpl(
  override val target: IdeModuleLibrary,
) : IdeModuleDependency, Serializable {
  @get:TestOnly
  val displayName: String get() = moduleLibraryDisplayName(target.buildId, target.projectPath, target.variant, target.sourceSet)
}
