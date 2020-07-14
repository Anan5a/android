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
package com.android.tools.idea.adb.wireless;

import com.intellij.ui.components.JBLabel;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class PinCodePanel {
  @NotNull private JBLabel myFirstLineLabel;
  @NotNull private JBLabel mySecondLineLabel;
  @NotNull private JPanel myRootComponent;
  @NotNull private JPanel myPinCodeContainer;

  public PinCodePanel() {
    myPinCodeContainer.setBackground(UIColors.PAIRING_CONTENT_BACKGROUND);
    myRootComponent.setBackground(UIColors.PAIRING_CONTENT_BACKGROUND);
    myFirstLineLabel.setForeground(UIColors.PAIRING_HINT_LABEL);
    mySecondLineLabel.setForeground(UIColors.PAIRING_HINT_LABEL);
  }

  @NotNull
  public JComponent getComponent() {
    return myRootComponent;
  }

  public void setInnerComponent(@NotNull JComponent component) {
    myPinCodeContainer.removeAll();
    myPinCodeContainer.add(component, BorderLayout.CENTER);
  }
}
