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
package com.android.tools.idea.devicemanager.physicaltab;

import com.android.tools.idea.devicemanager.Device;
import icons.StudioIcons;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PhysicalDevice extends Device implements Comparable<@NotNull PhysicalDevice> {
  private static final @NotNull Comparator<@NotNull PhysicalDevice> COMPARATOR =
    Comparator.<PhysicalDevice, Boolean>comparing(Device::isOnline, Comparator.reverseOrder())
      .thenComparing(PhysicalDevice::getLastOnlineTime, Comparator.nullsLast(Comparator.reverseOrder()));

  private final @NotNull String mySerialNumber;
  private final @Nullable Instant myLastOnlineTime;
  private final @NotNull String myApi;
  private final @NotNull Collection<@NotNull ConnectionType> myConnectionTypes;

  public static final class Builder extends Device.Builder {
    private @Nullable String mySerialNumber;
    private @Nullable Instant myLastOnlineTime;
    private @Nullable String myApi;
    private final @NotNull Collection<@NotNull ConnectionType> myConnectionTypes = EnumSet.noneOf(ConnectionType.class);

    public @NotNull Builder setSerialNumber(@NotNull String serialNumber) {
      mySerialNumber = serialNumber;
      return this;
    }

    @NotNull Builder setLastOnlineTime(@Nullable Instant lastOnlineTime) {
      myLastOnlineTime = lastOnlineTime;
      return this;
    }

    public @NotNull Builder setName(@NotNull String name) {
      myName = name;
      return this;
    }

    public @NotNull Builder setTarget(@NotNull String target) {
      myTarget = target;
      return this;
    }

    public @NotNull Builder setApi(@NotNull String api) {
      myApi = api;
      return this;
    }

    public @NotNull Builder addConnectionType(@NotNull ConnectionType connectionType) {
      myConnectionTypes.add(connectionType);
      return this;
    }

    @NotNull Builder addAllConnectionTypes(@NotNull Collection<@NotNull ConnectionType> connectionTypes) {
      myConnectionTypes.addAll(connectionTypes);
      return this;
    }

    @Override
    public @NotNull PhysicalDevice build() {
      return new PhysicalDevice(this);
    }
  }

  public enum ConnectionType {
    USB,
    WI_FI;

    @Override
    public final @NotNull String toString() {
      return this.equals(USB) ? super.toString() : "Wi-Fi";
    }
  }

  private PhysicalDevice(@NotNull Builder builder) {
    super(builder);

    assert builder.mySerialNumber != null;
    mySerialNumber = builder.mySerialNumber;

    myLastOnlineTime = builder.myLastOnlineTime;

    assert builder.myApi != null;
    myApi = builder.myApi;

    myConnectionTypes = builder.myConnectionTypes;
  }

  @NotNull String getSerialNumber() {
    return mySerialNumber;
  }

  @Nullable Instant getLastOnlineTime() {
    return myLastOnlineTime;
  }

  @Override
  public @NotNull Icon getIcon() {
    return StudioIcons.DeviceExplorer.PHYSICAL_DEVICE_PHONE;
  }

  @Override
  public boolean isOnline() {
    return !myConnectionTypes.isEmpty();
  }

  @NotNull String getApi() {
    return myApi;
  }

  @NotNull Collection<@NotNull ConnectionType> getConnectionTypes() {
    return myConnectionTypes;
  }

  @Override
  public int hashCode() {
    int hashCode = mySerialNumber.hashCode();

    hashCode = 31 * hashCode + Objects.hashCode(myLastOnlineTime);
    hashCode = 31 * hashCode + myName.hashCode();
    hashCode = 31 * hashCode + myTarget.hashCode();
    hashCode = 31 * hashCode + myApi.hashCode();
    hashCode = 31 * hashCode + myConnectionTypes.hashCode();

    return hashCode;
  }

  @Override
  public boolean equals(@Nullable Object object) {
    if (!(object instanceof PhysicalDevice)) {
      return false;
    }

    PhysicalDevice device = (PhysicalDevice)object;

    return mySerialNumber.equals(device.mySerialNumber) &&
           Objects.equals(myLastOnlineTime, device.myLastOnlineTime) &&
           myName.equals(device.myName) &&
           myTarget.equals(device.myTarget) &&
           myApi.equals(device.myApi) &&
           myConnectionTypes.equals(device.myConnectionTypes);
  }

  @Override
  public int compareTo(@NotNull PhysicalDevice device) {
    return COMPARATOR.compare(this, device);
  }
}
