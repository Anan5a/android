/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.tools.idea.model;

import com.android.SdkConstants;
import com.android.resources.ScreenSize;
import com.android.sdklib.IAndroidTarget;
import com.google.common.base.Charsets;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidRootUtil;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.SdkConstants.*;
import static com.android.xml.AndroidManifest.*;

class PrimaryManifestInfo extends ManifestInfo {
  private static final Logger LOG = Logger.getInstance(PrimaryManifestInfo.class);

  private final Module myModule;
  private String myPackage;
  private String myManifestTheme;
  private Map<String, ActivityAttributes> myActivityAttributesMap;
  private ManifestFile myManifestFile;
  private long myLastChecked;
  private String myMinSdkName;
  private int myMinSdk;
  private int myTargetSdk;
  private String myApplicationIcon;
  private String myApplicationLabel;
  private boolean myApplicationSupportsRtl;
  private Manifest myManifest;

  PrimaryManifestInfo(Module module) {
    myModule = module;
  }

  @Override
  public void clear() {
    myLastChecked = 0;
  }

  @Nullable
  @Override
  public String getPackage() {
    sync();
    return myPackage;
  }

  @NotNull
  @Override
  public Map<String, ActivityAttributes> getActivityAttributesMap() {
    sync();
    if (myActivityAttributesMap == null) {
      return Collections.emptyMap();
    }
    return myActivityAttributesMap;
  }

  @Nullable
  @Override
  public ActivityAttributes getActivityAttributes(@NotNull String activity) {
    int index = activity.indexOf('.');
    if (index <= 0 && myPackage != null && !myPackage.isEmpty()) {
      activity = myPackage + (index == -1 ? "." : "") + activity;
    }
    return getActivityAttributesMap().get(activity);
  }

  @Nullable
  @Override
  public String getManifestTheme() {
    sync();
    return myManifestTheme;
  }

  @NotNull
  @Override
  public String getDefaultTheme(@Nullable IAndroidTarget renderingTarget, @Nullable ScreenSize screenSize) {
    sync();

    if (myManifestTheme != null) {
      return myManifestTheme;
    }

    // From manifest theme documentation:
    // "If that attribute is also not set, the default system theme is used."

    int renderingTargetSdk = myTargetSdk;
    if (renderingTarget != null) {
      renderingTargetSdk = renderingTarget.getVersion().getApiLevel();
    }

    int apiLevel = Math.min(myTargetSdk, renderingTargetSdk);
    // For now this theme works only on XLARGE screens. When it works for all sizes,
    // add that new apiLevel to this check.
    if (apiLevel >= 11 && screenSize == ScreenSize.XLARGE || apiLevel >= 14) {
      return ANDROID_STYLE_RESOURCE_PREFIX + "Theme.Holo"; //$NON-NLS-1$
    }
    else {
      return ANDROID_STYLE_RESOURCE_PREFIX + "Theme"; //$NON-NLS-1$
    }
  }

  @Nullable
  @Override
  public String getApplicationIcon() {
    sync();
    return myApplicationIcon;
  }

  @Nullable
  @Override
  public String getApplicationLabel() {
    sync();
    return myApplicationLabel;
  }

  @Override
  public boolean isRtlSupported() {
    sync();
    return myApplicationSupportsRtl;
  }

  @Override
  public int getTargetSdkVersion() {
    sync();
    return myTargetSdk;
  }

  @Override
  public int getMinSdkVersion() {
    sync();
    return myMinSdk;
  }

  @Override
  @NotNull
  public String getMinSdkName() {
    sync();
    if (myMinSdkName == null || myMinSdkName.isEmpty()) {
      myMinSdkName = "1"; //$NON-NLS-1$
    }

    return myMinSdkName;
  }

  @Override
  @Nullable
  public String getMinSdkCodeName() {
    String minSdkName = getMinSdkName();
    if (!Character.isDigit(minSdkName.charAt(0))) {
      return minSdkName;
    }

    return null;
  }

  @NotNull
  @Override
  protected List<Manifest> getManifests() {
    sync();
    return Collections.singletonList(myManifest);
  }

  /**
   * Ensure that the package, theme and activity maps are initialized and up to date
   * with respect to the manifest file
   */
  private void sync() {
    // Since each of the accessors call sync(), allow a bunch of immediate
    // accessors to all bypass the file stat() below
    long now = System.currentTimeMillis();
    if (now - myLastChecked < 50 && myManifestFile != null) {
      return;
    }
    myLastChecked = now;

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        syncWithReadPermission();
      }
    });
  }

  private void syncWithReadPermission() {
    if (myManifestFile == null) {
      myManifestFile = ManifestFile.create(myModule);
      if (myManifestFile == null) {
        return;
      }
    }

    // Check to see if our data is up to date
    boolean refresh = myManifestFile.refresh();
    if (!refresh) {
      // Already have up to date data
      return;
    }

    myActivityAttributesMap = new HashMap<String, ActivityAttributes>();
    myManifestTheme = null;
    myTargetSdk = 1; // Default when not specified
    myMinSdk = 1; // Default when not specified
    myMinSdkName = "1"; // Default when not specified
    myPackage = ""; //$NON-NLS-1$
    myApplicationIcon = null;
    myApplicationLabel = null;
    myApplicationSupportsRtl = false;

    try {
      XmlTag root = myManifestFile.getXmlFile().getRootTag();
      if (root == null) {
        return;
      }

      myPackage = root.getAttributeValue(ATTRIBUTE_PACKAGE);

      XmlTag[] applications = root.findSubTags(NODE_APPLICATION);
      if (applications.length > 0) {
        assert applications.length == 1;
        XmlTag application = applications[0];
        myApplicationIcon = application.getAttributeValue(ATTRIBUTE_ICON, ANDROID_URI);
        myApplicationLabel = application.getAttributeValue(ATTRIBUTE_LABEL, ANDROID_URI);
        myManifestTheme = application.getAttributeValue(ATTRIBUTE_THEME, ANDROID_URI);
        myApplicationSupportsRtl = VALUE_TRUE.equals(application.getAttributeValue(ATTRIBUTE_SUPPORTS_RTL, ANDROID_URI));

        XmlTag[] activities = application.findSubTags(NODE_ACTIVITY);
        for (XmlTag activity : activities) {
          ActivityAttributes attributes = new ActivityAttributes(activity, myPackage);
          myActivityAttributesMap.put(attributes.getName(), attributes);
        }
      }

      // Look up target SDK
      XmlTag[] usesSdks = root.findSubTags(NODE_USES_SDK);
      if (usesSdks.length > 0) {
        XmlTag usesSdk = usesSdks[0];
        myMinSdk = getApiVersion(usesSdk, ATTRIBUTE_MIN_SDK_VERSION, 1);
        myTargetSdk = getApiVersion(usesSdk, ATTRIBUTE_TARGET_SDK_VERSION, myMinSdk);
      }

      myManifest = AndroidUtils.loadDomElementWithReadPermission(myModule.getProject(), myManifestFile.getXmlFile(), Manifest.class);
    }
    catch (Exception e) {
      LOG.error("Could not read Manifest data", e);
    }
  }

  private int getApiVersion(XmlTag usesSdk, String attribute, int defaultApiLevel) {
    String valueString = usesSdk.getAttributeValue(attribute, ANDROID_URI);
    if (attribute.equals(ATTRIBUTE_MIN_SDK_VERSION)) {
      myMinSdkName = valueString;
    }

    if (valueString != null) {
      int apiLevel = -1;
      try {
        apiLevel = Integer.valueOf(valueString);
      }
      catch (NumberFormatException e) {
        // Handle codename
        AndroidFacet facet = AndroidFacet.getInstance(myModule);
        if (facet != null) {
          IAndroidTarget target = facet.getTargetFromHashString("android-" + valueString);
          if (target != null) {
            // codename future API level is current api + 1
            apiLevel = target.getVersion().getApiLevel() + 1;
          }
        }
      }

      return apiLevel;
    }

    return defaultApiLevel;
  }

  private static class ManifestFile {
    private final Module myModule;
    private VirtualFile myVFile;
    private XmlFile myXmlFile;
    private long myLastModified = 0;

    private ManifestFile(@NotNull Module module, @NotNull VirtualFile file) {
      myModule = module;
      myVFile = file;
    }

    @Nullable
    public static ManifestFile create(@NotNull Module module) {
      ApplicationManager.getApplication().assertReadAccessAllowed();

      AndroidFacet facet = AndroidFacet.getInstance(module);
      if (facet == null) {
        return null;
      }

      VirtualFile manifestFile = AndroidRootUtil.getPrimaryManifestFile(facet);
      if (manifestFile == null) {
        return null;
      }

      return new ManifestFile(module, manifestFile);
    }

    @Nullable
    private XmlFile parseManifest() {
      PsiFile psiFile = PsiManager.getInstance(myModule.getProject()).findFile(myVFile);
      return (psiFile instanceof XmlFile) ? (XmlFile)psiFile : null;
    }

    public boolean refresh() {
      long lastModified = getLastModified();
      if (myXmlFile == null || myLastModified < lastModified) {
        myXmlFile = parseManifest();
        if (myXmlFile == null) {
          return false;
        }
        myLastModified = lastModified;
        return true;
      } else {
        return false;
      }
    }

    private long getLastModified() {
      if (myXmlFile != null) {
        return myXmlFile.getModificationStamp();
      } else {
        return 0;
      }
    }

    public XmlFile getXmlFile() {
      return myXmlFile;
    }
  }
}
