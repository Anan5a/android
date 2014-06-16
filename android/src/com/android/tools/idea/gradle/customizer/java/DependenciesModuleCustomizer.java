/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.tools.idea.gradle.customizer.java;

import com.android.tools.idea.gradle.IdeaJavaProject;
import com.android.tools.idea.gradle.customizer.AbstractDependenciesModuleCustomizer;
import com.android.tools.idea.gradle.messages.Message;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleOrderEntry;
import org.gradle.tooling.model.idea.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.android.tools.idea.gradle.messages.CommonMessageGroupNames.FAILED_TO_SET_UP_DEPENDENCIES;
import static com.intellij.openapi.util.io.FileUtil.getNameWithoutExtension;
import static com.intellij.openapi.util.io.FileUtil.sanitizeFileName;
import static java.util.Collections.singletonList;

public class DependenciesModuleCustomizer extends AbstractDependenciesModuleCustomizer<IdeaJavaProject> {
  @NotNull @NonNls private static final String UNRESOLVED_DEPENDENCY_PREFIX = "unresolved dependency - ";

  private static final Logger LOG = Logger.getInstance(AbstractDependenciesModuleCustomizer.class);

  private static final DependencyScope DEFAULT_DEPENDENCY_SCOPE = DependencyScope.COMPILE;

  @Override
  protected void setUpDependencies(@NotNull ModifiableRootModel model,
                                   @NotNull IdeaJavaProject javaProject,
                                   @NotNull List<Message> errorsFound) {
    List<String> unresolved = Lists.newArrayList();
    List<? extends IdeaDependency> dependencies = javaProject.getDependencies();
    for (IdeaDependency dependency : dependencies) {
      if (dependency instanceof IdeaModuleDependency) {
        updateDependency(model, (IdeaModuleDependency)dependency, errorsFound);
        continue;
      }
      if (dependency instanceof IdeaSingleEntryLibraryDependency) {
        IdeaSingleEntryLibraryDependency libDependency = (IdeaSingleEntryLibraryDependency)dependency;
        if (isResolved(libDependency)) {
          updateDependency(model, (IdeaSingleEntryLibraryDependency)dependency, errorsFound);
          continue;
        }
        String name = getUnresolvedDependencyName(libDependency);
        if (name != null) {
          unresolved.add(name);
        }
      }
    }
    reportUnresolvedDependencies(unresolved, model.getProject());
  }

  private static boolean isResolved(@NotNull IdeaSingleEntryLibraryDependency dependency) {
    String libraryName = getFileName(dependency);
    return libraryName != null && !libraryName.startsWith(UNRESOLVED_DEPENDENCY_PREFIX);
  }

  @Nullable
  private static String getUnresolvedDependencyName(@NotNull IdeaSingleEntryLibraryDependency dependency) {
    String libraryName = getFileName(dependency);
    if (libraryName == null) {
      return null;
    }
    // Gradle uses names like 'unresolved dependency - commons-collections commons-collections 3.2' for unresolved dependencies.
    // We report the unresolved dependency as 'commons-collections:commons-collections:3.2'
    return libraryName.substring(UNRESOLVED_DEPENDENCY_PREFIX.length()).replace(' ', ':');
  }

  @Nullable
  private static String getFileName(@NotNull IdeaSingleEntryLibraryDependency dependency) {
    File binaryPath = dependency.getFile();
    return binaryPath != null ? binaryPath.getName() : null;
  }

  private static void updateDependency(@NotNull ModifiableRootModel model,
                                       @NotNull IdeaModuleDependency dependency,
                                       @NotNull List<Message> errorsFound) {
    IdeaModule dependencyModule = dependency.getDependencyModule();
    if (dependencyModule == null || Strings.isNullOrEmpty(dependencyModule.getName())) {
      String msg = "Found a module dependency without name: " + dependency;
      LOG.info(msg);
      errorsFound.add(new Message(FAILED_TO_SET_UP_DEPENDENCIES, Message.Type.ERROR, msg));
      return;
    }
    String moduleName = dependencyModule.getName();
    ModuleManager moduleManager = ModuleManager.getInstance(model.getProject());
    Module found = null;
    for (Module module : moduleManager.getModules()) {
      if (moduleName.equals(module.getName())) {
        found = module;
      }
    }
    if (found != null) {
      ModuleOrderEntry orderEntry = model.addModuleOrderEntry(found);
      orderEntry.setExported(true);
      return;
    }
    String msg = String.format("Unable fo find module '%1$s'.", moduleName);
    LOG.info(msg);
    errorsFound.add(new Message(FAILED_TO_SET_UP_DEPENDENCIES, Message.Type.ERROR, msg));
  }

  private void updateDependency(@NotNull ModifiableRootModel model,
                                @NotNull IdeaSingleEntryLibraryDependency dependency,
                                @NotNull List<Message> errorsFound) {
    DependencyScope scope = parseScope(dependency.getScope());
    File binaryPath = dependency.getFile();
    if (binaryPath == null) {
      String msg = "Found a library dependency without a 'binary' path: " + dependency;
      LOG.info(msg);
      errorsFound.add(new Message(FAILED_TO_SET_UP_DEPENDENCIES, Message.Type.ERROR, msg));
      return;
    }
    String path = binaryPath.getPath();

    // Gradle API doesn't provide library name at the moment.
    String name = binaryPath.isFile() ? getNameWithoutExtension(binaryPath) : sanitizeFileName(path);
    setUpLibraryDependency(model, name, scope, singletonList(path), getPath(dependency.getSource()), getPath(dependency.getJavadoc()));
  }

  @NotNull
  private static List<String> getPath(@Nullable File file) {
    return file == null ? Collections.<String>emptyList() : singletonList(file.getPath());
  }

  @NotNull
  private static DependencyScope parseScope(@Nullable IdeaDependencyScope scope) {
    if (scope == null) {
      return DEFAULT_DEPENDENCY_SCOPE;
    }
    String description = scope.getScope();
    if (description == null) {
      return DEFAULT_DEPENDENCY_SCOPE;
    }
    for (DependencyScope dependencyScope : DependencyScope.values()) {
      if (description.equalsIgnoreCase(dependencyScope.toString())) {
        return dependencyScope;
      }
    }
    return DEFAULT_DEPENDENCY_SCOPE;
  }
}
