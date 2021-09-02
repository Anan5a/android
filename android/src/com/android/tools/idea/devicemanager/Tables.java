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
package com.android.tools.idea.devicemanager;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import java.awt.Color;
import java.awt.Component;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jetbrains.annotations.NotNull;

public final class Tables {
  private Tables() {
  }

  public static @NotNull Color getBackground(@NotNull JTable table, boolean selected) {
    if (selected) {
      return table.getSelectionBackground();
    }

    return table.getBackground();
  }

  public static @NotNull Border getBorder(boolean selected, boolean focused) {
    return getBorder(selected, focused, UIManager::getBorder);
  }

  @VisibleForTesting
  static @NotNull Border getBorder(boolean selected, boolean focused, @NotNull Function<@NotNull Object, @NotNull Border> getBorder) {
    if (!focused) {
      return getBorder.apply("Table.cellNoFocusBorder");
    }

    if (selected) {
      return getBorder.apply("Table.focusSelectedCellHighlightBorder");
    }

    return getBorder.apply("Table.focusCellHighlightBorder");
  }

  public static @NotNull Color getForeground(@NotNull JTable table, boolean selected) {
    if (selected) {
      return table.getSelectionForeground();
    }

    return table.getForeground();
  }

  public static void sizeWidthToFit(@NotNull JTable table, int viewColumnIndex) {
    TableColumn column = table.getColumnModel().getColumn(viewColumnIndex);
    int width = getPreferredColumnWidth(table, viewColumnIndex);

    column.setMinWidth(width);
    column.setMaxWidth(width);
    column.setPreferredWidth(width);
  }

  private static int getPreferredColumnWidth(@NotNull JTable table, int viewColumnIndex) {
    OptionalInt width = IntStream.range(-1, table.getRowCount())
      .map(viewRowIndex -> getPreferredCellWidth(table, viewRowIndex, viewColumnIndex))
      .max();

    int minWidth = JBUIScale.scale(65);

    if (!width.isPresent()) {
      return minWidth;
    }

    return Math.max(width.getAsInt(), minWidth);
  }

  private static int getPreferredCellWidth(@NotNull JTable table, int viewRowIndex, int viewColumnIndex) {
    Component component;

    if (viewRowIndex == -1) {
      TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
      Object value = table.getColumnModel().getColumn(viewColumnIndex).getHeaderValue();

      component = renderer.getTableCellRendererComponent(table, value, false, false, -1, viewColumnIndex);
    }
    else {
      component = table.prepareRenderer(table.getCellRenderer(viewRowIndex, viewColumnIndex), viewRowIndex, viewColumnIndex);
    }

    return component.getPreferredSize().width + JBUI.scale(8);
  }
}
