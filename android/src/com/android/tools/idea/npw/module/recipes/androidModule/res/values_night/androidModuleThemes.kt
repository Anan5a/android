/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.tools.idea.npw.module.recipes.androidModule.res.values_night

import com.android.tools.idea.npw.module.recipes.androidModule.res.values.DARK_ACTION_BAR_APPCOMPAT
import com.android.tools.idea.npw.module.recipes.androidModule.res.values.DARK_ACTION_BAR_MATERIAL_COMPONENTS

fun androidModuleThemes(useAndroidX: Boolean, themeName: String = "Theme.App"): String {
  val parent = if (useAndroidX) DARK_ACTION_BAR_MATERIAL_COMPONENTS else DARK_ACTION_BAR_APPCOMPAT
  return """
<resources>
  <!-- Application theme for dark theme. -->
  <style name="$themeName" parent="$parent">
      <!-- Customize your theme here. -->
      <item name="colorPrimary">@color/purple200</item>
      <item name="colorPrimaryDark">@color/purple700</item>
      <item name="colorAccent">@color/teal200</item>
  </style>

</resources>
"""
}
