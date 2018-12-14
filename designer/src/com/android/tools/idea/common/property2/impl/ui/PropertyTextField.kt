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
package com.android.tools.idea.common.property2.impl.ui

import com.android.tools.adtui.stdui.CommonTextField
import com.android.tools.adtui.stdui.registerKeyAction
import com.android.tools.idea.common.property2.impl.model.TextFieldPropertyEditorModel
import com.android.tools.idea.common.property2.impl.support.TextEditorFocusListener
import com.intellij.ide.ui.laf.darcula.DarculaUIUtil
import com.intellij.ide.ui.laf.darcula.ui.DarculaTextBorder
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.KeyStroke

/**
 * A standard control for editing a text property.
 */
class PropertyTextField(editorModel: TextFieldPropertyEditorModel) : CommonTextField<TextFieldPropertyEditorModel>(editorModel) {
  init {
    registerKeyAction({ enter() }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter")
    registerKeyAction({ tab() }, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "tab")
    registerKeyAction({ backTab() }, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "backTab")
    registerKeyAction({ escape() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape")
    registerKeyAction({ editorModel.f1KeyPressed() }, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "help")
    registerKeyAction({ editorModel.shiftF1KeyPressed() }, KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_DOWN_MASK), "help2")
    addFocusListener(TextEditorFocusListener(this, this, editorModel))
    putClientProperty(DarculaUIUtil.COMPACT_PROPERTY, true)
    focusTraversalKeysEnabled = false // handle tab and shift-tab ourselves
  }

  override fun updateFromModel() {
    super.updateFromModel()
    isVisible = editorModel.visible
    if (editorModel.focusRequest && !isFocusOwner) {
      requestFocusInWindow()
    }
  }

  override fun getToolTipText(event: MouseEvent): String? {
    return PropertyTooltip.setToolTip(this, event, editorModel.property, forValue = true, text = text.orEmpty())
  }

  private fun enter() {
    enterInLookup()
    commit()
  }

  private fun tab() {
    if (commit()) {
      transferFocus()
    }
    // TODO: b/121043039 Add some kind of notification that the commit failed e.g shake the edit control
  }

  private fun backTab() {
    if (commit()) {
      transferFocusBackward()
    }
    // TODO: b/121043039 Add some kind of notification that the commit failed e.g shake the edit control
  }

  private fun commit(): Boolean {
    if (!editorModel.commit()) {
      return false
    }
    selectAll()
    return true
  }

  private fun escape() {
    if (escapeInLookup()) {
      return
    }
    editorModel.escape()
  }

  companion object {

    @JvmStatic
    fun addBorderAtTextFieldBorderSize(component: JComponent) {
      val insets = DarculaTextBorder().getBorderInsets(component)
      // The insets are already scaled: do not use JBUI.Borders.emptyBorder(...)
      component.border = BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right)
    }
  }
}
