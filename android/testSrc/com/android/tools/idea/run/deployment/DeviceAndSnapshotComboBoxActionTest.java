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
package com.android.tools.idea.run.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.android.tools.idea.run.AndroidDevice;
import com.android.tools.idea.run.AndroidRunConfiguration;
import com.android.tools.idea.run.AndroidRunConfigurationBase;
import com.android.tools.idea.run.editor.DeployTargetContext;
import com.android.tools.idea.testartifacts.instrumented.AndroidTestRunConfiguration;
import com.android.tools.idea.testing.AndroidProjectRule;
import com.google.common.collect.Sets;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.ide.util.ProjectPropertiesComponentImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import icons.StudioIcons;
import java.awt.Component;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.swing.JComponent;
import org.jetbrains.android.actions.RunAndroidAvdManagerAction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public final class DeviceAndSnapshotComboBoxActionTest {
  @Rule
  public final AndroidProjectRule myRule = AndroidProjectRule.inMemory();

  private AsyncDevicesGetter myDevicesGetter;

  private Clock myClock;

  private PropertiesComponent myProperties;

  private ExecutionTargetService myExecutionTargetService;

  private RunManager myRunManager;

  private Project myProject;
  private Presentation myPresentation;
  private AnActionEvent myEvent;

  private DataContext myContext;

  @Before
  public void mockDevicesGetter() {
    myDevicesGetter = Mockito.mock(AsyncDevicesGetter.class);
  }

  @Before
  public void mockClock() {
    myClock = Mockito.mock(Clock.class);
    Mockito.when(myClock.instant()).thenReturn(Instant.parse("2018-11-28T01:15:27.000Z"));
  }

  @Before
  public void mockProperties() {
    myProperties = Mockito.mock(PropertiesComponent.class);
  }

  @Before
  public void mockExecutionTargetService() {
    myExecutionTargetService = Mockito.mock(ExecutionTargetService.class);
  }

  @Before
  public void mockRunManager() {
    myRunManager = Mockito.mock(RunManager.class);
  }

  @Before
  public void mockEvent() {
    myProject = myRule.getProject();
    myPresentation = new Presentation();

    myEvent = Mockito.mock(AnActionEvent.class);

    Mockito.when(myEvent.getPresentation()).thenReturn(myPresentation);
    Mockito.when(myEvent.getProject()).thenReturn(myProject);
    Mockito.when(myEvent.getPlace()).thenReturn(ActionPlaces.MAIN_TOOLBAR);
  }

  @Before
  public void mockContext() {
    myContext = Mockito.mock(DataContext.class);
    Mockito.when(myContext.getData(CommonDataKeys.PROJECT)).thenReturn(myRule.getProject());
  }

  @Test
  public void getSelectedDeviceDevicesIsEmpty() {
    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setClock(myClock)
      .build();

    assertNull(action.getSelectedDevice(myProject));
  }

  @Test
  public void getSelectedDeviceSelectedDeviceIsntPresent() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);

    assertEquals(builder.build(), action.getSelectedDevice(myProject));
  }

  @Test
  public void getSelectedDeviceSelectedDeviceIsConnected() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setConnectionTime(Instant.parse("2018-11-28T01:15:27.000Z"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    action.setSelectedDevice(myProject, builder.build());

    assertEquals(builder.build(), action.getSelectedDevice(myProject));
  }

  @Test
  public void getSelectedDeviceSelectionTimeIsBeforeConnectionTime() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    action.setSelectedDevice(myProject, builder.build());

    Device physicalDevice = new PhysicalDevice.Builder()
      .setName("LGE Nexus 5X")
      .setKey(new Key("00fff9d2279fa601"))
      .setConnectionTime(Instant.parse("2018-11-28T01:15:28.000Z"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Arrays.asList(builder.build(), physicalDevice));

    action.update(myEvent);

    assertEquals(physicalDevice, action.getSelectedDevice(myProject));
  }

  @Test
  public void getSelectedDeviceSelectionTimeIsNull() {
    // Arrange
    Device pixel3ApiQ = new VirtualDevice.Builder()
      .setName("Pixel 3 API Q")
      .setKey(new Key("Pixel_3_API_Q"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Device pixel2ApiQ = new VirtualDevice.Builder()
      .setName("Pixel 2 API Q")
      .setKey(new Key("Pixel_2_API_Q"))
      .setConnectionTime(Instant.parse("2019-04-04T22:54:09.086Z"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Arrays.asList(pixel3ApiQ, pixel2ApiQ));

    Mockito.when(myProperties.getValue(DeviceAndSnapshotComboBoxAction.SELECTED_DEVICE)).thenReturn("Pixel_3_API_Q");

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);

    // Act
    Object actualDevice = action.getSelectedDevice(myProject);

    // Assert
    assertEquals(pixel2ApiQ, actualDevice);
  }

  @Test
  public void getSelectedDeviceConnectedDeviceIsntPresent() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device1 = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device1));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    action.setSelectedDevice(myProject, builder.build());

    Device device2 = new VirtualDevice.Builder()
      .setName("Pixel XL API 28")
      .setKey(new Key("Pixel_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Arrays.asList(builder.build(), device2));

    action.update(myEvent);

    assertEquals(builder.build(), action.getSelectedDevice(myProject));
  }

  @Test
  public void createCustomComponent() {
    // Arrange
    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setClock(myClock)
      .build();

    myPresentation.setIcon(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE);

    // noinspection DialogTitleCapitalization
    myPresentation.setText(
      "Pixel 2 API 29 (Failed to parse properties from /usr/local/google/home/juancnuno/.android/avd/Pixel_2_API_29.avd/config.ini)",
      false);

    // Act
    Component component = action.createCustomComponent(myPresentation, i -> i);

    // Assert
    assertEquals(253, component.getPreferredSize().width);
  }

  @Test
  public void createPopupActionGroupActionsIsEmpty() {
    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    Object actualChildren = Arrays.asList(action.createPopupActionGroup(Mockito.mock(JComponent.class), myContext).getChildren(null));

    Object expectedChildren = Arrays.asList(
      action.getMultipleDevicesAction(),
      action.getModifyDeviceSetAction(),
      ActionManager.getInstance().getAction(RunAndroidAvdManagerAction.ID));

    assertEquals(expectedChildren, actualChildren);
  }

  @Test
  public void createPopupActionGroupDeviceIsVirtual() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    Object actualChildren = Arrays.asList(action.createPopupActionGroup(Mockito.mock(JComponent.class), myContext).getChildren(null));

    Object expectedChildren = Arrays.asList(
      new Heading("Available devices"),
      SelectDeviceAction.newSelectDeviceAction(action, myProject, builder.build()),
      Separator.getInstance(),
      action.getMultipleDevicesAction(),
      action.getModifyDeviceSetAction(),
      ActionManager.getInstance().getAction(RunAndroidAvdManagerAction.ID));

    assertEquals(expectedChildren, actualChildren);
  }

  @Test
  public void createPopupActionGroupDeviceIsPhysical() {
    Device.Builder builder = new PhysicalDevice.Builder()
      .setName("LGE Nexus 5X")
      .setKey(new Key("00fff9d2279fa601"))
      .setConnectionTime(Instant.parse("2018-11-28T01:15:27.000Z"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    Object actualChildren = Arrays.asList(action.createPopupActionGroup(Mockito.mock(JComponent.class), myContext).getChildren(null));

    Object expectedChildren = Arrays.asList(
      new Heading("Running devices"),
      SelectDeviceAction.newSelectDeviceAction(action, myProject, builder.build()),
      Separator.getInstance(),
      action.getMultipleDevicesAction(),
      action.getModifyDeviceSetAction(),
      ActionManager.getInstance().getAction(RunAndroidAvdManagerAction.ID));

    assertEquals(expectedChildren, actualChildren);
  }

  @Test
  public void createPopupActionGroupDevicesAreVirtualAndPhysical() {
    Device.Builder virtualDeviceBuilder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device.Builder physicalDeviceBuilder = new PhysicalDevice.Builder()
      .setName("LGE Nexus 5X")
      .setKey(new Key("00fff9d2279fa601"))
      .setConnectionTime(Instant.parse("2018-11-28T01:15:27.000Z"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device1 = virtualDeviceBuilder.build();
    Device device2 = physicalDeviceBuilder.build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Arrays.asList(device1, device2));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    Object actualChildren = Arrays.asList(action.createPopupActionGroup(Mockito.mock(JComponent.class), myContext).getChildren(null));

    Object expectedChildren = Arrays.asList(
      new Heading("Running devices"),
      SelectDeviceAction.newSelectDeviceAction(action, myProject, physicalDeviceBuilder.build()),
      Separator.getInstance(),
      new Heading("Available devices"),
      SelectDeviceAction.newSelectDeviceAction(action, myProject, virtualDeviceBuilder.build()),
      Separator.getInstance(),
      action.getMultipleDevicesAction(),
      action.getModifyDeviceSetAction(),
      ActionManager.getInstance().getAction(RunAndroidAvdManagerAction.ID));

    assertEquals(expectedChildren, actualChildren);
  }

  @Test
  public void createPopupActionGroupDeviceSnapshotIsDefault() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .setSnapshot(Snapshot.quickboot(FileSystems.getDefault()));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    Object actualChildren = Arrays.asList(action.createPopupActionGroup(Mockito.mock(JComponent.class), myContext).getChildren(null));

    Object expectedChildren = Arrays.asList(
      new Heading("Available devices"),
      SelectDeviceAction.newSelectDeviceAction(action, myProject, builder.build()),
      Separator.getInstance(),
      action.getMultipleDevicesAction(),
      action.getModifyDeviceSetAction(),
      ActionManager.getInstance().getAction(RunAndroidAvdManagerAction.ID));

    assertEquals(expectedChildren, actualChildren);
  }

  @Test
  public void createPopupActionGroupSnapshotIsntDefault() {
    FileSystem fileSystem = FileSystems.getDefault();

    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .setSnapshot(new Snapshot(fileSystem.getPath("snap_2018-08-07_16-27-58"), fileSystem));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);
    Object actualChildren = Arrays.asList(action.createPopupActionGroup(Mockito.mock(JComponent.class), myContext).getChildren(null));

    Object expectedChildren = Arrays.asList(
      new Heading("Available devices"),
      SelectDeviceAction.newSelectDeviceAction(action, myProject, builder.build()),
      Separator.getInstance(),
      action.getMultipleDevicesAction(),
      action.getModifyDeviceSetAction(),
      ActionManager.getInstance().getAction(RunAndroidAvdManagerAction.ID));

    assertEquals(expectedChildren, actualChildren);
  }

  @Test
  public void updatePresentationSettingsIsNull() {
    // Act
    DeviceAndSnapshotComboBoxAction.updatePresentation(myPresentation, null);

    // Assert
    assertEquals("Add a run/debug configuration", myPresentation.getDescription());
    assertFalse(myPresentation.isEnabled());
  }

  @Test
  public void updatePresentationConfigurationIsntAndroidRunConfigurationNorAndroidTestRunConfiguration() {
    // Arrange
    RunConfiguration configuration = Mockito.mock(RunConfiguration.class);
    Mockito.when(configuration.getName()).thenReturn("ExampleUnitTest");

    RunnerAndConfigurationSettings settings = Mockito.mock(RunnerAndConfigurationSettings.class);
    Mockito.when(settings.getConfiguration()).thenReturn(configuration);

    // Act
    DeviceAndSnapshotComboBoxAction.updatePresentation(myPresentation, settings);

    // Assert
    assertEquals("Not applicable for the \"ExampleUnitTest\" configuration", myPresentation.getDescription());
    assertFalse(myPresentation.isEnabled());
  }

  @Test
  public void updatePresentationConfigurationIsntAndroidRunConfigurationNorAndroidTestRunConfigurationButDeploysToLocalDevice() {
    // Arrange
    RunConfigurationBase<?> configuration = Mockito.mock(RunConfigurationBase.class);
    Mockito.when(configuration.getUserData(DeviceAndSnapshotComboBoxAction.DEPLOYS_TO_LOCAL_DEVICE)).thenReturn(true);

    RunnerAndConfigurationSettings settings = Mockito.mock(RunnerAndConfigurationSettings.class);
    Mockito.when(settings.getConfiguration()).thenReturn(configuration);

    // Act
    DeviceAndSnapshotComboBoxAction.updatePresentation(myPresentation, settings);

    // Assert
    assertNull(myPresentation.getDescription());
    assertTrue(myPresentation.isEnabled());
  }

  @Test
  public void updatePresentationFirebaseTestLabDeviceMatrixTargetIsntSelected() {
    // Arrange
    DeployTargetContext context = Mockito.mock(DeployTargetContext.class);
    Mockito.when(context.getCurrentDeployTargetProvider()).thenReturn(new DeviceAndSnapshotComboBoxTargetProvider());

    AndroidRunConfigurationBase configuration = Mockito.mock(AndroidTestRunConfiguration.class);
    Mockito.when(configuration.getDeployTargetContext()).thenReturn(context);

    RunnerAndConfigurationSettings settings = Mockito.mock(RunnerAndConfigurationSettings.class);
    Mockito.when(settings.getConfiguration()).thenReturn(configuration);

    // Act
    DeviceAndSnapshotComboBoxAction.updatePresentation(myPresentation, settings);

    // Assert
    assertNull(myPresentation.getDescription());
    assertTrue(myPresentation.isEnabled());
  }

  @Test
  public void updatePresentation() {
    // Arrange
    RunConfiguration configuration = Mockito.mock(AndroidRunConfiguration.class);

    RunnerAndConfigurationSettings settings = Mockito.mock(RunnerAndConfigurationSettings.class);
    Mockito.when(settings.getConfiguration()).thenReturn(configuration);

    // Act
    DeviceAndSnapshotComboBoxAction.updatePresentation(myPresentation, settings);

    // Assert
    assertNull(myPresentation.getDescription());
    assertTrue(myPresentation.isEnabled());
  }

  @Test
  public void getTextDeviceHasSnapshot() {
    // Arrange
    Snapshot snapshot = new Snapshot(FileSystems.getDefault().getPath("snap_2019-09-27_15-48-09"), "Snapshot");

    Device device = new VirtualDevice.Builder()
      .setName("Pixel 3 API 29")
      .setKey(new Key("Pixel_3_API_29", snapshot))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .setSnapshot(snapshot)
      .build();

    // Act
    Object text = DeviceAndSnapshotComboBoxAction.getText(device, Collections.singletonList(device), true);

    // Assert
    assertEquals("Pixel 3 API 29 - Snapshot", text);
  }

  @Test
  public void getTextDeviceHasValidityReason() {
    // Arrange
    Device device = new VirtualDevice.Builder()
      .setName("Pixel 3 API 29")
      .setValidityReason("Missing system image")
      .setKey(new Key("Pixel_3_API_29"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    // Act
    Object text = DeviceAndSnapshotComboBoxAction.getText(device, Collections.singletonList(device), true);

    // Assert
    assertEquals("Pixel 3 API 29 (Missing system image)", text);
  }

  @Test
  public void updateEventPlaceEqualsMainMenu() {
    // Arrange
    AnAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    Mockito.when(myEvent.getPlace()).thenReturn(ActionPlaces.MAIN_MENU);

    // Act
    action.update(myEvent);

    // Assert
    assertTrue(myPresentation.isVisible());
    assertNull(myPresentation.getIcon());
    assertEquals("Select Device...", myPresentation.getText());
  }

  @Test
  public void updateEventPlaceEqualsActionSearch() {
    // Arrange
    AnAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    Mockito.when(myEvent.getPlace()).thenReturn(ActionPlaces.ACTION_SEARCH);

    // Act
    action.update(myEvent);

    // Assert
    assertNull(myPresentation.getIcon());
    assertEquals("Select Device...", myPresentation.getText());
  }

  @Test
  public void updateInToolbarForMultipleDevicesSelectedKeysIsEmptyDevicesIsEmpty() {
    // Arrange
    PropertiesComponent properties = new ProjectPropertiesComponentImpl();

    Set<Key> keys = Sets.newHashSet(new Key("Pixel_2_API_29"));

    SelectedDevicesService service = Mockito.mock(SelectedDevicesService.class);
    Mockito.when(service.getSelectedDeviceKeys()).thenReturn(keys);

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> properties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setSelectedDevicesServiceGetInstance(project -> service)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setMultipleDevicesSelected(myProject, true);

    // Act
    action.update(myEvent);

    // Assert
    Mockito.verify(service).setSelectedDeviceKeys(Collections.emptySet());

    assertFalse(properties.isValueSet(DeviceAndSnapshotComboBoxAction.MULTIPLE_DEVICES_SELECTED));
    assertFalse(properties.isValueSet(DeviceAndSnapshotComboBoxAction.SELECTED_DEVICE));
    assertFalse(properties.isValueSet(DeviceAndSnapshotComboBoxAction.SELECTION_TIME));

    assertNull(myPresentation.getIcon());
    assertEquals("No Devices", myPresentation.getText());
  }

  @Test
  public void updateInToolbarForMultipleDevicesSelectedKeysIsEmpty() {
    // Arrange
    Device device = new VirtualDevice.Builder()
      .setName("Pixel 3 API 29")
      .setKey(new Key("Pixel_3_API_29"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    PropertiesComponent properties = new ProjectPropertiesComponentImpl();

    Set<Key> keys = Sets.newHashSet(new Key("Pixel_2_API_29"));

    SelectedDevicesService service = Mockito.mock(SelectedDevicesService.class);
    Mockito.when(service.getSelectedDeviceKeys()).thenReturn(keys);

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> properties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setSelectedDevicesServiceGetInstance(project -> service)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setMultipleDevicesSelected(myProject, true);

    // Act
    action.update(myEvent);

    // Assert
    Mockito.verify(service).setSelectedDeviceKeys(Collections.emptySet());

    assertFalse(properties.isValueSet(DeviceAndSnapshotComboBoxAction.MULTIPLE_DEVICES_SELECTED));
    assertEquals("Pixel_3_API_29", properties.getValue(DeviceAndSnapshotComboBoxAction.SELECTED_DEVICE));
    assertEquals("2018-11-28T01:15:27Z", properties.getValue(DeviceAndSnapshotComboBoxAction.SELECTION_TIME));

    assertEquals(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE, myPresentation.getIcon());
    assertEquals("Pixel 3 API 29", myPresentation.getText());
  }

  @Test
  public void updateInToolbarForMultipleDevices() {
    // Arrange
    Key key = new Key("Pixel_2_API_29");

    Device device = new VirtualDevice.Builder()
      .setName("Pixel 2 API 29")
      .setKey(key)
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    PropertiesComponent properties = new ProjectPropertiesComponentImpl();

    SelectedDevicesService service = Mockito.mock(SelectedDevicesService.class);
    Mockito.when(service.getSelectedDeviceKeys()).thenReturn(Collections.singleton(key));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> properties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setSelectedDevicesServiceGetInstance(project -> service)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setMultipleDevicesSelected(myProject, true);

    // Act
    action.update(myEvent);

    // Assert
    Mockito.verify(service, Mockito.never()).setSelectedDeviceKeys(ArgumentMatchers.any());

    assertTrue(properties.getBoolean(DeviceAndSnapshotComboBoxAction.MULTIPLE_DEVICES_SELECTED));
    assertFalse(properties.isValueSet(DeviceAndSnapshotComboBoxAction.SELECTED_DEVICE));
    assertFalse(properties.isValueSet(DeviceAndSnapshotComboBoxAction.SELECTION_TIME));

    assertNull(myPresentation.getIcon());
    assertEquals("Multiple Devices", myPresentation.getText());
  }

  @Test
  public void updateDevicesIsEmpty() {
    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);

    assertTrue(myPresentation.isVisible());
    assertNull(action.getSelectedDevice(myProject));
    assertNull(myPresentation.getIcon());
    assertEquals("No Devices", myPresentation.getText());
  }

  @Test
  public void updateDoesntClearSelectedDeviceWhenDevicesIsEmpty() {
    // Arrange
    PropertiesComponent properties = new ProjectPropertiesComponentImpl();

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> properties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    Device pixel2XlApiQ = new VirtualDevice.Builder()
      .setName("Pixel 2 XL API Q")
      .setKey(new Key("Pixel_2_XL_API_Q"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Device pixel3XlApiQ = new VirtualDevice.Builder()
      .setName("Pixel 3 XL API Q")
      .setKey(new Key("Pixel_3_XL_API_Q"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    // Act
    action.setSelectedDevice(myProject, pixel3XlApiQ);
    action.update(myEvent);

    Mockito.when(myDevicesGetter.get()).thenReturn(Arrays.asList(pixel2XlApiQ, pixel3XlApiQ));
    action.update(myEvent);

    // Assert
    assertEquals(pixel3XlApiQ, action.getSelectedDevice(myProject));
  }

  @Test
  public void updateSelectedDeviceIsNull() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.update(myEvent);

    assertTrue(myPresentation.isVisible());
    assertEquals(builder.build(), action.getSelectedDevice(myProject));
    assertEquals(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE, myPresentation.getIcon());
    assertEquals(TestDevices.PIXEL_2_XL_API_28, myPresentation.getText());
  }

  @Test
  public void updateDevicesContainSelectedDevice() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setSelectedDevice(myProject, builder.build());
    action.update(myEvent);

    assertTrue(myPresentation.isVisible());
    assertEquals(builder.build(), action.getSelectedDevice(myProject));
    assertEquals(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE, myPresentation.getIcon());
    assertEquals(TestDevices.PIXEL_2_XL_API_28, myPresentation.getText());
  }

  @Test
  public void updateDevicesDontContainSelectedDevice() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device1 = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device1));

    Device device2 = new VirtualDevice.Builder()
      .setName("Pixel XL API 28")
      .setKey(new Key("Pixel_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setSelectedDevice(myProject, device2);
    action.update(myEvent);

    assertTrue(myPresentation.isVisible());
    assertEquals(builder.build(), action.getSelectedDevice(myProject));
    assertEquals(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE, myPresentation.getIcon());
    assertEquals(TestDevices.PIXEL_2_XL_API_28, myPresentation.getText());
  }

  @Test
  public void updateTwoDevicesHaveSameName() {
    // Arrange
    Device lgeNexus5x1 = new PhysicalDevice.Builder()
      .setName("LGE Nexus 5X")
      .setKey(new Key("00fff9d2279fa601"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Device lgeNexus5x2 = new PhysicalDevice.Builder()
      .setName("LGE Nexus 5X")
      .setKey(new Key("00fff9d2279fa602"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Arrays.asList(lgeNexus5x1, lgeNexus5x2));

    AnAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    // Act
    action.update(myEvent);

    // Assert
    assertEquals("LGE Nexus 5X [00fff9d2279fa601]", myPresentation.getText());
  }

  @Test
  public void updateSetTextDoesntMangleDeviceName() {
    // Arrange
    Device apiQ64Google = new VirtualDevice.Builder()
      .setName("apiQ_64_Google")
      .setKey(new Key("apiQ_64_Google"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(apiQ64Google));

    AnAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    // Act
    action.update(myEvent);

    // Assert
    assertEquals("apiQ_64_Google", myPresentation.getText());
  }

  @Test
  public void updateSelectedSnapshotIsNull() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .setSnapshot(Snapshot.quickboot(FileSystems.getDefault()));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setSelectedDevice(myProject, builder.build());
    action.update(myEvent);

    assertTrue(myPresentation.isVisible());
    assertEquals(builder.build(), action.getSelectedDevice(myProject));
    assertEquals(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE, myPresentation.getIcon());
    assertEquals(TestDevices.PIXEL_2_XL_API_28, myPresentation.getText());
  }

  @Test
  public void updateSnapshotsContainSelectedSnapshot() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .setSnapshot(Snapshot.quickboot(FileSystems.getDefault()));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setSelectedDevice(myProject, builder.build());
    action.update(myEvent);

    assertTrue(myPresentation.isVisible());
    assertEquals(builder.build(), action.getSelectedDevice(myProject));
    assertEquals(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE, myPresentation.getIcon());
    assertEquals(TestDevices.PIXEL_2_XL_API_28, myPresentation.getText());
  }

  @Test
  public void updateSelectedDeviceHasSnapshot() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .setSnapshot(Snapshot.quickboot(FileSystems.getDefault()));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setSelectedDevice(myProject, builder.build());
    action.update(myEvent);

    assertTrue(myPresentation.isVisible());
    assertEquals(builder.build(), action.getSelectedDevice(myProject));
    assertEquals(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE, myPresentation.getIcon());
    assertEquals(TestDevices.PIXEL_2_XL_API_28, myPresentation.getText());
  }

  @Test
  public void updateSelectedDeviceDoesntHaveSnapshot() {
    Device.Builder builder = new VirtualDevice.Builder()
      .setName(TestDevices.PIXEL_2_XL_API_28)
      .setKey(new Key("Pixel_2_XL_API_28"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class));

    Device device = builder.build();
    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device));

    DeviceAndSnapshotComboBoxAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setSelectDeviceSnapshotComboBoxSnapshotsEnabled(() -> true)
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    action.setSelectedDevice(myProject, builder.build());
    action.update(myEvent);

    assertTrue(myPresentation.isVisible());
    assertEquals(builder.build(), action.getSelectedDevice(myProject));
    assertEquals(StudioIcons.DeviceExplorer.VIRTUAL_DEVICE_PHONE, myPresentation.getIcon());
    assertEquals(TestDevices.PIXEL_2_XL_API_28, myPresentation.getText());
  }

  @Test
  public void updateDevicesGetterReturnsDifferentLists() {
    // Arrange
    Device device = new PhysicalDevice.Builder()
      .setName("Unknown Device")
      .setKey(new Key("cd020375-1ce4-45dc-a5be-b45e5765c6f2"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get())
      .thenReturn(Collections.emptyList())
      .thenReturn(Collections.singletonList(device));

    AnAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    // Act
    action.update(myEvent);
  }

  @Test
  public void updateConnectedDeviceActiveTargetIsSet() {
    // Arrange
    Device device = new VirtualDevice.Builder()
      .setName("Pixel 3 API 29")
      .setKey(new Key("Pixel_3_API_29"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Device connectedDevice = new VirtualDevice.Builder()
      .setName("Pixel 3 API 29")
      .setKey(new Key("Pixel_3_API_29"))
      .setConnectionTime(Instant.parse("2020-03-13T23:13:20.913Z"))
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Mockito.when(myDevicesGetter.get()).thenReturn(Collections.singletonList(device), Collections.singletonList(connectedDevice));

    AnAction action = new DeviceAndSnapshotComboBoxAction.Builder()
      .setDevicesGetterGetter(project -> myDevicesGetter)
      .setGetProperties(project -> myProperties)
      .setClock(myClock)
      .setExecutionTargetServiceGetInstance(project -> myExecutionTargetService)
      .setGetRunManager(project -> myRunManager)
      .build();

    // Act
    action.update(myEvent);
    action.update(myEvent);

    // Assert
    Mockito.verify(myExecutionTargetService).setActiveTarget(new DeviceAndSnapshotComboBoxExecutionTarget(device));
    Mockito.verify(myExecutionTargetService).setActiveTarget(new DeviceAndSnapshotComboBoxExecutionTarget(connectedDevice));
  }
}
