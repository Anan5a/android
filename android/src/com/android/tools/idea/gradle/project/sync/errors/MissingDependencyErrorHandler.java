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
package com.android.tools.idea.gradle.project.sync.errors;

import com.android.tools.idea.gradle.service.notification.hyperlink.NotificationHyperlink;
import com.android.tools.idea.gradle.service.notification.hyperlink.OpenFileHyperlink;
import com.android.tools.idea.gradle.service.notification.hyperlink.SearchInBuildFilesHyperlink;
import com.android.tools.idea.gradle.service.notification.hyperlink.ToggleOfflineModeHyperlink;
import com.google.common.base.Splitter;
import com.intellij.openapi.externalSystem.service.notification.NotificationData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;

public class MissingDependencyErrorHandler extends SyncErrorHandler {
  private static final Pattern MISSING_MATCHING_DEPENDENCY_PATTERN = Pattern.compile("Could not find any version that matches (.*)\\.");
  private static final Pattern MISSING_DEPENDENCY_PATTERN = Pattern.compile("Could not find (.*)\\.");

  private static String dependency;
  private static String openFileLine;

  @Override
  @Nullable
  protected String findErrorMessage(@NotNull Throwable rootCause, @NotNull NotificationData notification, @NotNull Project project) {
    String text = rootCause.getMessage();
    List<String> message = Splitter.on('\n').omitEmptyStrings().trimResults().splitToList(text);
    String newMsg = null;
    for (String line : message) {
      // This happens when Gradle cannot find the Android Gradle plug-in in Maven Central or jcenter.
      if (line == null) {
        continue;
      }
      Matcher matcher = MISSING_MATCHING_DEPENDENCY_PATTERN.matcher(line);
      if (matcher.matches()) {
        dependency = matcher.group(1);
        newMsg = line;
      }
    }

    String firstLine = message.get(0);
    Matcher matcher = MISSING_DEPENDENCY_PATTERN.matcher(firstLine);
    if (matcher.matches() && message.size() > 1 && message.get(1).startsWith("Required by:")) {
      dependency = matcher.group(1);
      if (isNotEmpty(dependency)) {
        newMsg = text;
        openFileLine = message.get(message.size() - 1);
      }
    }
    if (isNotEmpty(newMsg)) {
      updateUsageTracker();
      return newMsg;
    }
    return null;
  }

  @Override
  @NotNull
  protected List<NotificationHyperlink> getQuickFixHyperlinks(@NotNull NotificationData notification,
                                                              @NotNull Project project,
                                                              @NotNull String text) {
    List<NotificationHyperlink> hyperlinks = new ArrayList<>();

    if (isNotEmpty(openFileLine)) {
      Pair<String, Integer> errorLocation = getErrorLocation(openFileLine);
      if (errorLocation != null) {
        // We have a location in file, show the "Open File" hyperlink.
        String filePath = errorLocation.getFirst();
        int line = errorLocation.getSecond();
        hyperlinks.add(new OpenFileHyperlink(filePath, line - 1));
      }
    }

    ToggleOfflineModeHyperlink disableOfflineMode = ToggleOfflineModeHyperlink.disableOfflineMode(project);
    if (disableOfflineMode != null) {
      hyperlinks.add(0, disableOfflineMode);
    }
    hyperlinks.add(new SearchInBuildFilesHyperlink(dependency));
    return hyperlinks;
  }
}