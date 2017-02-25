/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.idea.explorer.adbimpl;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.SyncService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestShellCommands {
  @NotNull private static final Logger LOGGER = Logger.getInstance(TestShellCommands.class);

  @NotNull private final Map<String, TestShellCommandResult> myCommands = new HashMap<>();
  @NotNull private String myDescription = "[MockDevice]";

  public void setDescription(@NotNull String description) {
    myDescription = description;
  }

  public void add(@NotNull String command, @NotNull String lines) {
    myCommands.put(command, new TestShellCommandResult(lines));
  }

  public void addError(@NotNull String command, @NotNull Exception error) {
    myCommands.put(command, new TestShellCommandResult(error));
  }

  public TestShellCommandResult get(@NotNull String command) {
    return myCommands.get(command);
  }

  public IDevice createMockDevice() throws Exception {
    SyncService syncService = mock(SyncService.class);

    IDevice device = mock(IDevice.class);

    doReturn(IDevice.DeviceState.ONLINE).when(device).getState();
    doReturn(myDescription).when(device).getName();
    doReturn("1234").when(device).getSerialNumber();
    doReturn(syncService).when(device).getSyncService();

    doAnswer(invocation -> {
      String command = invocation.getArgument(0);
      IShellOutputReceiver receiver = invocation.getArgument(1);
      executeShellCommand(command, receiver);
      return null;
    }).when(device).executeShellCommand(any(), any());

    return device;
  }

  private void executeShellCommand(String command, IShellOutputReceiver receiver) throws Exception {
    TestShellCommandResult commandResult = this.get(command);
    if (commandResult == null) {
      UnsupportedOperationException error = new UnsupportedOperationException(
        String.format("Command \"%s\" not found in mock device \"%s\". Test case is not correctly setup.", command, myDescription));
      LOGGER.error(error);
      throw error;
    }

    LOGGER.debug(String.format("Test command found: %s", command));
    if (commandResult.getError() != null) {
      throw commandResult.getError();
    }

    if (commandResult.getOutput() == null) {
      UnsupportedOperationException error = new UnsupportedOperationException(
        String.format("Command \"%s\" has no result in mock device \"%s\". Test case is not setup correctly", command, myDescription));
      LOGGER.error(error);
      throw error;
    }

    byte[] bytes = commandResult.getOutput().getBytes(Charset.forName("UTF-8"));
    int chunkSize = 100;
    for (int i = 0; i < bytes.length; i += chunkSize) {
      int count = Math.min(chunkSize, bytes.length - i);
      receiver.addOutput(bytes, i, count);
    }
    receiver.flush();
  }
}
