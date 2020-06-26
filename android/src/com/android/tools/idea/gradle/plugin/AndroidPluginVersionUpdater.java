/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tools.idea.gradle.plugin;

import static com.android.tools.idea.gradle.project.sync.hyperlink.SearchInBuildFilesHyperlink.searchInBuildFiles;
import static com.intellij.openapi.util.text.StringUtil.isEmpty;
import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;
import static com.intellij.util.ThreeState.NO;
import static com.intellij.util.ThreeState.UNSURE;
import static com.intellij.util.ThreeState.YES;

import com.android.ide.common.repository.GradleVersion;
import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencyModel;
import com.android.tools.idea.gradle.project.sync.setup.post.upgrade.AgpClasspathDependencyRefactoringProcessor;
import com.android.tools.idea.gradle.project.sync.setup.post.upgrade.AgpGradleVersionRefactoringProcessor;
import com.android.tools.idea.gradle.project.sync.setup.post.upgrade.GMavenRepositoryRefactoringProcessor;
import com.android.tools.idea.gradle.util.GradleVersions;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AndroidPluginVersionUpdater {
  @NotNull private final Project myProject;
  @NotNull private final TextSearch myTextSearch;

  @NotNull
  public static AndroidPluginVersionUpdater getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, AndroidPluginVersionUpdater.class);
  }

  public AndroidPluginVersionUpdater(@NotNull Project project) {
    this(project, new TextSearch(project));
  }

  @NonInjectable
  @VisibleForTesting
  AndroidPluginVersionUpdater(@NotNull Project project,
                              @NotNull TextSearch textSearch) {
    myProject = project;
    myTextSearch = textSearch;
  }

  /**
   * @param pluginVersion the Android Gradle plugin version to update to
   * @param gradleVersion the Gradle version to update to
   * @return whether or not the update of the Android Gradle plugin OR Gradle version was successful.
   */
  public boolean updatePluginVersion(@NotNull GradleVersion pluginVersion, @Nullable GradleVersion gradleVersion) {
    UpdateResult result = updatePluginVersion(pluginVersion, gradleVersion, null);
    return result.isPluginVersionUpdated() || result.isGradleVersionUpdated();
  }

  /**
   * Updates the plugin version and, optionally, the Gradle version used by the project.
   *
   * @param pluginVersion    the plugin version to update to.
   * @param gradleVersion    the version of Gradle to update to (optional.)
   * @param oldPluginVersion the version of plugin from which we update. Used because of b/130738995.
   * @return the result of the update operation.
   */
  public UpdateResult updatePluginVersion(
    @NotNull GradleVersion pluginVersion,
    @Nullable GradleVersion gradleVersion,
    @Nullable GradleVersion oldPluginVersion
  ) {
    UpdateResult result = new UpdateResult();

    if (oldPluginVersion == null) {
      // if we don't know the version we're upgrading from, assume an early one.
      // FIXME(xof): we should always know what we're upgrading from.  Find callers and fix them.
      oldPluginVersion = new GradleVersion(1, 0, 0);
    }
    GradleVersion projectGradleVersion;
    if (gradleVersion == null) {
      // if we're not requesting an upgrade to the gradle version, use the project's current gradle version ...
      projectGradleVersion = GradleVersions.getInstance().getGradleVersionOrDefault(myProject, new GradleVersion(1, 0, 0));
    }
    else {
      // ... but if we are, use the to-be-upgraded version.
      projectGradleVersion = gradleVersion;
    }

    AgpClasspathDependencyRefactoringProcessor rp1 = new AgpClasspathDependencyRefactoringProcessor(myProject, oldPluginVersion, pluginVersion);
    GMavenRepositoryRefactoringProcessor rp2 = new GMavenRepositoryRefactoringProcessor(myProject, oldPluginVersion, pluginVersion, projectGradleVersion);
    try {
      rp1.run();
      if (!oldPluginVersion.isAtLeast(3, 0, 0)) {
        rp2.run();
      }
      if (rp1.getFoundUsages()) {
        result.pluginVersionUpdated();
      }
    } catch (Throwable e) {
      result.setPluginVersionUpdateError(e);
    }

    if (result.isPluginVersionUpdated() && gradleVersion != null) {
      AgpGradleVersionRefactoringProcessor rp3 = new AgpGradleVersionRefactoringProcessor(myProject, oldPluginVersion, pluginVersion, gradleVersion);
      try {
        rp3.run();
        result.gradleVersionUpdated();
      }
      catch (Throwable e) {
        result.setGradleVersionUpdateError(e);
      }
    }

    Throwable pluginVersionUpdateError = result.getPluginVersionUpdateError();
    Throwable gradleVersionUpdateError = result.getGradleVersionUpdateError();

    if (pluginVersionUpdateError != null) {
      String msg = String.format("Failed to update Android plugin to version '%1$s'", pluginVersion);
      logUpdateError(msg, pluginVersionUpdateError);
    }
    if (gradleVersionUpdateError != null) {
      String msg = String.format("Failed to update Gradle to version '%1$s'", gradleVersion);
      logUpdateError(msg, gradleVersionUpdateError);
    }

    if (result.getPluginVersionUpdateError() != null) {
      myTextSearch.execute();
    }
    return result;
  }

  private static void logUpdateError(@NotNull String msg, @NotNull Throwable error) {
    String cause = error.getMessage();
    if (isNotEmpty(cause)) {
      msg += ": " + cause;
    }
    Logger.getInstance(AndroidPluginVersionUpdater.class).warn(msg);
  }

  @NotNull
  public static ThreeState isUpdatablePluginVersion(@NotNull GradleVersion pluginVersion, @NotNull ArtifactDependencyModel model) {
    String artifactId = model.name().forceString();
    String groupId = model.group().toString();
    if (!AndroidPluginInfo.isAndroidPlugin(artifactId, groupId)) {
      return UNSURE;
    }

    String versionValue = model.version().toString();
    return (isEmpty(versionValue) || pluginVersion.compareTo(versionValue) != 0) ? YES : NO;
  }

  public static class UpdateResult {
    @Nullable private Throwable myPluginVersionUpdateError;
    @Nullable private Throwable myGradleVersionUpdateError;

    private boolean myPluginVersionUpdated;
    private boolean myGradleVersionUpdated;

    @VisibleForTesting
    public UpdateResult() {
    }

    @Nullable
    public Throwable getPluginVersionUpdateError() {
      return myPluginVersionUpdateError;
    }

    void setPluginVersionUpdateError(@NotNull Throwable error) {
      myPluginVersionUpdateError = error;
    }

    @Nullable
    public Throwable getGradleVersionUpdateError() {
      return myGradleVersionUpdateError;
    }

    void setGradleVersionUpdateError(@NotNull Throwable error) {
      myGradleVersionUpdateError = error;
    }

    public boolean isPluginVersionUpdated() {
      return myPluginVersionUpdated;
    }

    void pluginVersionUpdated() {
      myPluginVersionUpdated = true;
    }

    public boolean isGradleVersionUpdated() {
      return myGradleVersionUpdated;
    }

    void gradleVersionUpdated() {
      myGradleVersionUpdated = true;
    }

    public boolean versionUpdateSuccess() {
      return (myPluginVersionUpdated || myGradleVersionUpdated) && myPluginVersionUpdateError == null && myGradleVersionUpdateError == null;
    }
  }

  @VisibleForTesting
  static class TextSearch {
    @NotNull private final Project myProject;

    TextSearch(@NotNull Project project) {
      myProject = project;
    }

    void execute() {
      String msg = "Failed to update the version of the Android Gradle plugin.\n\n" +
                   "Please click 'OK' to perform a textual search and then update the build files manually.";
      ApplicationManager.getApplication().invokeLater(() -> {
        Messages.showErrorDialog(myProject, msg, "Unexpected Error");
        String textToFind = AndroidPluginInfo.GROUP_ID + ":" + AndroidPluginInfo.ARTIFACT_ID;
        searchInBuildFiles(textToFind, myProject);
      });
    }
  }
}
