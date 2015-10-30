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
package com.android.tools.idea.sdk.remote.internal.packages;

import com.android.SdkConstants;
import com.android.repository.Revision;
import com.android.sdklib.repository.descriptors.PkgDesc;
import com.android.sdklib.repository.local.LocalLLDBPkgInfo;
import com.android.sdklib.repository.local.LocalSdk;
import com.android.tools.idea.sdk.remote.RemotePkgInfo;
import com.android.tools.idea.sdk.remote.internal.sources.SdkSource;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import java.io.File;
import java.util.Map;

/**
 * Remote package representing the Android LLDB.
 */
public class RemoteLLDBPkgInfo extends RemotePkgInfo {

  public RemoteLLDBPkgInfo(SdkSource source, Node packageNode, String nsUri, Map<String, String> licenses) {
    super(source, packageNode, nsUri, licenses);
    mPkgDesc = PkgDesc.Builder.newLLDB(getRevision())
      .setListDisplay("LLDB")
      .setDescriptionShort("LLDB")
      .create();
  }

  @NotNull
  @Override
  public String installId() {
    return mPkgDesc.getInstallId();
  }

  @NotNull
  @Override
  public File getInstallFolder(@NotNull String osSdkRoot, @NotNull LocalSdk sdkManager) {
    String pathToLLDB = new File(osSdkRoot, SdkConstants.FD_LLDB).getPath();
    Revision rev = getRevision();
    return new File(pathToLLDB, new Revision(rev.getMajor(), rev.getMinor()).toString());
  }

  @Override
  public boolean hasCompatibleArchive() {
    Revision rev = getRevision();
    if (rev.getMajor() != LocalLLDBPkgInfo.PINNED_REVISION.getMajor() ||
        rev.getMinor() != LocalLLDBPkgInfo.PINNED_REVISION.getMinor()) {
      return false;
    }

    return super.hasCompatibleArchive();
  }
}
