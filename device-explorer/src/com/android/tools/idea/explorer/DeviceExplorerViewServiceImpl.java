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
package com.android.tools.idea.explorer;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.sdklib.internal.avd.AvdInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class DeviceExplorerViewServiceImpl implements DeviceExplorerViewService{
  private final @NotNull Project myProject;

  DeviceExplorerViewServiceImpl(@NotNull Project project) {
    myProject = project;
  }

  @Override
  public void openAndShowDevice(@NotNull AvdInfo avdInfo) {
    if (!showToolWindowImpl()) {
      return;
    }

    DeviceExplorerController controller = DeviceExplorerController.getProjectController(myProject);
    assert controller != null;
    assert AndroidDebugBridge.getBridge() != null;

    String avdName = avdInfo.getName();
    Optional<IDevice> optionalIDevice = Arrays.stream(AndroidDebugBridge.getBridge().getDevices()).filter(
      device -> avdName.equals(device.getAvdName())).findAny();
    if (!optionalIDevice.isPresent()) {
      controller.reportErrorFindingDevice("Unable to find AVD " + avdName + " by name. Please retry.");
      return;
    }

    controller.selectActiveDevice(optionalIDevice.get().getSerialNumber());
  }

  @Override
  public void openAndShowDevice(@NotNull String serialNumber) {
    if (!showToolWindowImpl()) {
      return;
    }

    DeviceExplorerController controller = DeviceExplorerController.getProjectController(myProject);
    assert controller != null;

    controller.selectActiveDevice(serialNumber);
  }

  @Override
  public void showToolWindow() {
    showToolWindowImpl();
  }

  private boolean showToolWindowImpl() {
    ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(DeviceExplorerToolWindowFactory.TOOL_WINDOW_ID);

    if (toolWindow != null) {
      toolWindow.show();
      return true;
    }

    return false;
  }
}
