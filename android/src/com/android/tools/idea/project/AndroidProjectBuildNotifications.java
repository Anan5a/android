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
package com.android.tools.idea.project;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import static com.intellij.ui.AppUIUtil.invokeLaterIfProjectAlive;

/**
 * Notifies subscribers about build events on an Android project.
 */
public class AndroidProjectBuildNotifications {
  private static final Topic<AndroidProjectBuildListener> PROJECT_BUILD_LISTENER_TOPIC =
    new Topic<AndroidProjectBuildListener>("Android Project Build", AndroidProjectBuildListener.class);

  @NotNull private final Project myProject;
  @NotNull private final MessageBus myMessageBus;

  public static void subscribe(@NotNull Project project, @NotNull AndroidProjectBuildListener listener) {
    MessageBusConnection connection = project.getMessageBus().connect(project);
    connection.subscribe(PROJECT_BUILD_LISTENER_TOPIC, listener);
  }

  @NotNull
  public static AndroidProjectBuildNotifications getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, AndroidProjectBuildNotifications.class);
  }

  public AndroidProjectBuildNotifications(@NotNull Project project, @NotNull MessageBus messageBus) {
    myProject = project;
    myMessageBus = messageBus;
  }

  public void notifyBuildComplete(@NotNull final BuildContext context) {
    invokeLaterIfProjectAlive(myProject, new Runnable() {
      @Override
      public void run() {
        myMessageBus.syncPublisher(PROJECT_BUILD_LISTENER_TOPIC).buildComplete(context);
      }
    });
  }

  public interface AndroidProjectBuildListener {
    void buildComplete(@NotNull BuildContext context);
  }

  public interface BuildContext {
  }
}
