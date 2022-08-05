/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.android.tools.idea.device.monitor.processes

import com.android.annotations.concurrency.UiThread

/**
 * Events fired by an instance of [DeviceListService].
 */
@UiThread
interface DeviceListServiceListener {
  /**
   * The internal state of the [DeviceListService] has changed,
   * meaning all devices and file system are now invalid and should be
   * re-acquired.
   */
  fun serviceRestarted()

  /**
   * A [Device] has been added to the list of connected devices of the
   * [DeviceListService]
   */
  fun deviceAdded(device: Device)

  /**
   * A [Device] has been removed from the list of connected devices of the
   * [DeviceListService]
   */
  fun deviceRemoved(device: Device)

  /**
   * A [Device]  from the list of connected devices of the
   * [DeviceListService] has had a state change, for example it
   * has become online after being offline.
   */
  fun deviceUpdated(device: Device)

  /**
   * This list of processes of [device] has changed.
   */
  fun deviceProcessListUpdated(device: Device)
}
