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
 *
 * THIS FILE WAS GENERATED BY codergen. EDIT WITH CARE.
 */
package com.android.tools.idea.editors.gfxtrace.service;

import com.android.tools.rpclib.binary.BinaryID;
import com.android.tools.idea.editors.gfxtrace.service.path.Path;
import com.android.tools.rpclib.any.Box;
import com.android.tools.idea.editors.gfxtrace.service.path.CapturePath;
import com.android.tools.idea.editors.gfxtrace.service.path.DevicePath;
import com.android.tools.idea.editors.gfxtrace.service.path.ImageInfoPath;
import com.android.tools.idea.editors.gfxtrace.service.path.AtomPath;
import com.android.tools.idea.editors.gfxtrace.service.path.TimingInfoPath;
import com.google.common.util.concurrent.ListenableFuture;

public abstract class ServiceClient {
  //<<<Start:Java.ClientBody:1>>>
  public abstract ListenableFuture<Path> follow(Path p);
  public abstract ListenableFuture<Box> get(Path p);
  public abstract ListenableFuture<CapturePath[]> getCaptures();
  public abstract ListenableFuture<DevicePath[]> getDevices();
  public abstract ListenableFuture<ImageInfoPath> getFramebufferColor(DevicePath device, AtomPath after, RenderSettings settings);
  public abstract ListenableFuture<ImageInfoPath> getFramebufferDepth(DevicePath device, AtomPath after);
  public abstract ListenableFuture<Schema> getSchema();
  public abstract ListenableFuture<TimingInfoPath> getTimingInfo(DevicePath device, CapturePath capture, TimingFlags flags);
  public abstract ListenableFuture<CapturePath> importCapture(String name, byte[] Data);
  public abstract ListenableFuture<Void> prerenderFramebuffers(DevicePath device, CapturePath capture, BinaryID api, int width, int height, long[] atomIndicies);
  public abstract ListenableFuture<Path> set(Path p, Box v);
  //<<<End:Java.ClientBody:1>>>
}
