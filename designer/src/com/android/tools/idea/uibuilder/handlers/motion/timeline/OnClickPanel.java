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
package com.android.tools.idea.uibuilder.handlers.motion.timeline;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import com.android.tools.idea.uibuilder.handlers.motion.AttrName;
import com.android.tools.idea.uibuilder.handlers.motion.Debug;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jetbrains.annotations.NotNull;

/**
 * Used for OnClickeControls
 */
public class OnClickPanel extends JPanel {
  OnClickTagPanel myClickUI;

  OnClickPanel(Dimension size) {
    super(new BorderLayout());
    setBackground(Chart.ourSecondaryPanelBackground);
    myClickUI = new OnClickTagPanel();
    JBScrollPane scrollPane = new JBScrollPane(myClickUI);
    scrollPane.setPreferredSize(size);
    add(scrollPane);
  }

  public void setOnClick(MotionSceneModel.OnClickTag tag) {
    myClickUI.setOnClickTag(tag);
  }

  static class OnClickTagPanel extends TagPanel {
    private static final boolean DEBUG = false;
    private Vector<String> colNames = new Vector<String>(Arrays.asList("Name", "Value"));
    private Vector<Vector<Object>> data = new Vector<>();
    private DefaultTableModel myTableModel = new OnClickeTableModel(data, colNames);
    MotionSceneModel.OnClickTag myOnClickeTag;
    private JBPopupMenu myPopupMenu = new JBPopupMenu("Add Attribute");
    private static final String ATTR_ATTRIBUTE_NAME = "AttributeName";

    public OnClickTagPanel() {

      myTable = new JBTable(myTableModel);
      myRemoveTagButton = EditorUtils.makeButton(TimeLineIcons.REMOVE_TAG);
      setup();
      myTable.setSelectionMode(SINGLE_SELECTION);
      myTable.setDefaultRenderer(EditorUtils.AttributesNamesHolder.class, new EditorUtils.AttributesNamesCellRenderer());
      myTable.setDefaultRenderer(String.class, new EditorUtils.AttributesValueCellRenderer());

      myTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
          Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          if (!isSelected) {
            c.setForeground(column > 0 ? EditorUtils.ourValueColor : EditorUtils.ourNameColor);
          }

          return c;
        }
      });

      myPopupMenu.add(new JMenuItem("test1"));
      myAddRemovePanel.myAddButton.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          myPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
      });

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weightx = 1;

      if (myTitle != null) {
        gbc.fill = GridBagConstraints.BOTH;
        add(myTitle, gbc);
      }

      gbc.gridwidth = 2;
      gbc.gridx = 0;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.anchor = GridBagConstraints.EAST;
      gbc.fill = GridBagConstraints.BOTH;
      add(myTable, gbc);

      gbc.gridy++;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.WEST;
      add(myAddRemovePanel, gbc);

      gbc.gridx = 1;
      gbc.weightx = 0;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.EAST;
      add(myRemoveTagButton, gbc);
    }

    @Override
    protected void deleteAttr(int selection) {
      AttrName attributeName = (AttrName)myTable.getValueAt(selection, 0);
      if (myOnClickeTag != null && myOnClickeTag.deleteAttribute(attributeName)) {
        myTableModel.removeRow(selection);
      }
    }

    @Override
    protected void deleteTag() {
      if (myOnClickeTag != null && myOnClickeTag.deleteTag()) {
        setVisible(false);
      }
    }

    public ActionListener myAddItemAction = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        AttrName attributeName = (AttrName)((JMenuItem)e.getSource()).getClientProperty(ATTR_ATTRIBUTE_NAME);
        String value = "";
        if (myOnClickeTag != null && myOnClickeTag.setValue(attributeName, value)) {
          myTableModel.addRow(new Object[]{attributeName, value});
        }
      }
    };

    private void setupPopup(MotionSceneModel.OnClickTag tag, Set<AttrName> strings) {
      myOnClickeTag = tag;
      myPopupMenu.removeAll();
      AttrName[] names = tag.getPossibleAttr();
      for (int i = 0; i < names.length; i++) {
        if (strings.contains(names[i])) {
          continue;
        }
        JMenuItem menuItem = new JMenuItem(names[i].getName());
        menuItem.putClientProperty(ATTR_ATTRIBUTE_NAME, names[i]);
        menuItem.addActionListener(myAddItemAction);
        myPopupMenu.add(menuItem);
      }
    }

    public void setOnClickTag(MotionSceneModel.OnClickTag tag) {
      if (DEBUG) {
        Debug.println( " setOnClickTag = " + tag);
      }

      setVisible((tag != null));
      if (tag == null) {
        setVisible(false);
        return;
      }
      HashMap<AttrName, Object> attr = tag.getAttributes();
      data.clear();
      for (AttrName s : attr.keySet()) {
        Vector<Object> v = new Vector<>(Arrays.asList(s, attr.get(s)));
        if (DEBUG) {
          Debug.println(s + " = " + attr.get(s));
        }
        data.add(v);
      }
      myTableModel.fireTableDataChanged();
      setupPopup(tag, attr.keySet());
      setVisible(true);
    }

    private class OnClickeTableModel extends DefaultTableModel {

      private OnClickeTableModel(@NotNull Vector data, @NotNull Vector columnNames) {
        super(data, columnNames);
      }

      @Override
      public void setValueAt(@NotNull Object value, int rowIndex, int columnIndex) {
        super.setValueAt(value, rowIndex, columnIndex);
        AttrName key = (AttrName)getValueAt(rowIndex, 0);
        myOnClickeTag.setValue(key, (String)value);
      }

      @Override
      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
      }
    }
  }
}