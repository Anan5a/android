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
package com.android.tools.idea.uibuilder.api.actions

import com.android.tools.idea.common.model.NlComponent
import com.android.tools.idea.uibuilder.analytics.NlUsageTracker
import com.android.tools.idea.uibuilder.api.ViewEditor
import com.android.tools.idea.uibuilder.api.ViewHandler
import com.google.wireless.android.sdk.stats.LayoutEditorEvent
import com.intellij.ide.util.PropertiesComponent
import icons.StudioIcons

private const val PREFERENCE_KEY_PREFIX = "LayoutEditorPreference"
private const val AUTO_CONNECT_PREF_KEY = PREFERENCE_KEY_PREFIX + "AutoConnect"
private const val DEFAULT_AUTO_CONNECT_VALUE = false

private const val AUTO_CONNECTION_ON_TOOLTIP = "Turn On Autoconnect"
private const val AUTO_CONNECTION_OFF_TOOLTIP = "Turn Off Autoconnect"

class ToggleAutoConnectAction : ToggleViewAction(StudioIcons.LayoutEditor.Toolbar.AUTO_CORRECT_OFF,
                                                 StudioIcons.LayoutEditor.Toolbar.AUTO_CONNECT,
                                                 AUTO_CONNECTION_ON_TOOLTIP,
                                                 AUTO_CONNECTION_OFF_TOOLTIP) {

  override fun isSelected(editor: ViewEditor, handler: ViewHandler, parent: NlComponent, selectedChildren: List<NlComponent>) =
    PropertiesComponent.getInstance().getBoolean(AUTO_CONNECT_PREF_KEY, DEFAULT_AUTO_CONNECT_VALUE)

  override fun setSelected(editor: ViewEditor,
                           handler: ViewHandler,
                           parent: NlComponent,
                           selectedChildren: List<NlComponent>,
                           selected: Boolean) {
    NlUsageTracker.getInstance(editor.scene.designSurface)
      .logAction(if (selected)
                   LayoutEditorEvent.LayoutEditorEventType.TURN_ON_AUTOCONNECT
                 else
                   LayoutEditorEvent.LayoutEditorEventType.TURN_OFF_AUTOCONNECT)
    PropertiesComponent.getInstance().setValue(AUTO_CONNECT_PREF_KEY, selected, DEFAULT_AUTO_CONNECT_VALUE)
  }

  override fun affectsUndo() = false

  companion object {
    @JvmStatic
    fun isAutoconnectOn(): Boolean {
      return PropertiesComponent.getInstance().getBoolean(AUTO_CONNECT_PREF_KEY, DEFAULT_AUTO_CONNECT_VALUE)
    }
  }
}
