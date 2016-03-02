/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.tools.idea.run.tasks;

import com.android.ddmlib.IDevice;
import com.android.tools.idea.run.ConsolePrinter;
import com.android.tools.idea.run.activity.ActivityLocator;
import com.android.tools.idea.run.editor.AndroidDebugger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultActivityLaunchTask extends ActivityLaunchTask {
  @NotNull private final ActivityLocator myActivityLocator;

  public DefaultActivityLaunchTask(@NotNull String applicationId,
                                   @NotNull ActivityLocator activityLocator,
                                   boolean waitForDebugger,
                                   @Nullable AndroidDebugger androidDebugger,
                                   @NotNull String extraAmOptions) {
    super(applicationId, waitForDebugger, androidDebugger, extraAmOptions);
    myActivityLocator = activityLocator;
  }

  @Nullable
  @Override
  protected String getQualifiedActivityName(@NotNull IDevice device, @NotNull ConsolePrinter printer) {
    try {
      return myActivityLocator.getQualifiedActivityName(device);
    }
    catch (ActivityLocator.ActivityLocatorException e) {
      printer.stderr("Could not identify launch activity: " + e.getMessage());
      return null;
    }
  }
}
