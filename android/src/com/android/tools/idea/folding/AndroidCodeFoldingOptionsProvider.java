/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.tools.idea.folding;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import org.jetbrains.android.util.AndroidBundle;

public class AndroidCodeFoldingOptionsProvider extends BeanConfigurable<AndroidFoldingSettings> implements CodeFoldingOptionsProvider {
  public AndroidCodeFoldingOptionsProvider() {
    super(AndroidFoldingSettings.getInstance(), AndroidBundle.message("group.Internal.Android.text"));
    AndroidFoldingSettings settings = getInstance();
    checkBox(
      AndroidBundle.message("android.editor.settings.general.code.folding.string.references"),
      settings::isCollapseAndroidStrings,
      settings::setCollapseAndroidStrings
    );
  }
}