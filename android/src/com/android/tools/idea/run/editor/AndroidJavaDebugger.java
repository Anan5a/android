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
package com.android.tools.idea.run.editor;

import com.android.ddmlib.Client;
import com.android.tools.idea.run.tasks.ConnectJavaDebuggerTask;
import com.android.tools.idea.run.tasks.DebugConnectorTask;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.Content;
import com.intellij.util.NotNullFunction;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class AndroidJavaDebugger implements AndroidDebugger<AndroidDebuggerState> {

  public static final String ID = "Java";
  private static final String RUN_CONFIGURATION_NAME_PATTERN = "Android Debugger (%s)";

  @NotNull
  @Override
  public String getId() {
    return ID;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return getId();
  }

  @NotNull
  @Override
  public AndroidDebuggerState createState() {
    return new AndroidDebuggerState();
  }

  @NotNull
  @Override
  public AndroidDebuggerConfigurable<AndroidDebuggerState> createConfigurable(@NotNull Project project) {
    return new AndroidDebuggerConfigurable<AndroidDebuggerState>();
  }

  @NotNull
  @Override
  public DebugConnectorTask getConnectDebuggerTask(@NotNull ExecutionEnvironment env,
                                                   @NotNull Set<String> applicationIds,
                                                   @NotNull AndroidFacet facet,
                                                   @NotNull AndroidDebuggerState state,
                                                   @NotNull String runConfigTypeId) {
    return new ConnectJavaDebuggerTask(env.getProject(), applicationIds);
  }

  @Override
  public boolean supportsProject(@NotNull Project project) {
    return true;
  }

  @Override
  public void attachToClient(@NotNull Project project, @NotNull Client client) {
    String debugPort = Integer.toString(client.getDebuggerListenPort());
    String runConfigName = String.format(RUN_CONFIGURATION_NAME_PATTERN, debugPort);

    // Try to find existing debug session
    if (findExistingDebugSession(project, debugPort, runConfigName)) {
      return;
    }

    // Create run configuration
    RemoteConfigurationType remoteConfigurationType = RemoteConfigurationType.getInstance();
    ConfigurationFactory factory = remoteConfigurationType.getFactory();
    RunnerAndConfigurationSettings runSettings = RunManager.getInstance(project).createRunConfiguration(runConfigName, factory);

    RemoteConfiguration configuration = (RemoteConfiguration)runSettings.getConfiguration();
    configuration.HOST = "localhost";
    configuration.PORT = debugPort;
    configuration.USE_SOCKET_TRANSPORT = true;
    configuration.SERVER_MODE = false;

    ProgramRunnerUtil.executeConfiguration(project, runSettings, DefaultDebugExecutor.getDebugExecutorInstance());
  }

  private static boolean findExistingDebugSession(@NotNull Project project, @NotNull final String debugPort, @NotNull final String runConfigName) {
    Collection<RunContentDescriptor> descriptors = null;
    Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
    Project targetProject = null;

    // Scan through open project to find if this port has been opened in any session.
    for (Project openProject : openProjects) {
      targetProject = openProject;

      // First check the titles of the run configurations.
      descriptors = ExecutionHelper.findRunningConsoleByTitle(targetProject, new NotNullFunction<String, Boolean>() {
        @NotNull
        @Override
        public Boolean fun(String title) {
          return runConfigName.equals(title);
        }
      });

      // If it can't find a matching title, check the debugger sessions.
      if (descriptors.isEmpty()) {
        for (DebuggerSession session : DebuggerManagerEx.getInstanceEx(targetProject).getSessions()) {
          if (debugPort.trim().equals(session.getProcess().getConnection().getAddress().trim()) && session.getXDebugSession() != null) {
            descriptors = Collections.singletonList(session.getXDebugSession().getRunContentDescriptor());
            break;
          }
        }
      }

      if (!descriptors.isEmpty()) {
        break;
      }
    }

    if (descriptors != null && !descriptors.isEmpty()) {
      final RunContentDescriptor descriptor = descriptors.iterator().next();
      final ProcessHandler processHandler = descriptor.getProcessHandler();
      final Content content = descriptor.getAttachedContent();

      if (processHandler != null && content != null) {
        final Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();

        if (processHandler.isProcessTerminated()) {
          ExecutionManager.getInstance(targetProject).getContentManager().removeRunContent(executor, descriptor);
        }
        else {
          if (targetProject != project) {
            // Bring window frame to front if the found tool window is not for the active project.
            JFrame targetFrame = WindowManager.getInstance().getFrame(targetProject);
            boolean alwaysOnTop = targetFrame.isAlwaysOnTop();

            targetFrame.setExtendedState(Frame.NORMAL);
            targetFrame.setAlwaysOnTop(true);
            targetFrame.toFront();
            targetFrame.requestFocus();
            targetFrame.setAlwaysOnTop(alwaysOnTop);
          }
          content.getManager().setSelectedContent(content);
          ToolWindow window = ToolWindowManager.getInstance(targetProject).getToolWindow(executor.getToolWindowId());
          window.activate(null, false, true);
          return true;
        }
      }
    }
    return false;
  }
}
