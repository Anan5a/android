/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.android.designer.model;

import com.intellij.designer.model.RadComponent;
import com.intellij.designer.propertyTable.Property;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: now dummy implementation for tests
 *
 * @author Alexander Lobas
 */
public class RadViewComponent extends RadComponent {
  public static final AnAction LinearLayout =
    new AnAction("Horizontal/Vertical", "LinearLayout", IconLoader.getIcon("/com/intellij/android/designer/icons/LinearLayout.png")) {
      @Override
      public void actionPerformed(AnActionEvent e) {
      }
    };

  private final List<RadComponent> myChildren = new ArrayList<RadComponent>();
  private Component myNativeComponent;
  private final Rectangle myBounds = new Rectangle();
  private XmlTag myTag;
  private List<Property> myProperties;

  public RadViewComponent(RadViewComponent parent) {
    setParent(parent);
    if (parent != null) {
      parent.getChildren().add(this);
    }
    setLayout(new RadViewLayout(this));
  }

  public XmlTag getTag() {
    return myTag;
  }

  public void setTag(XmlTag tag) {
    myTag = tag;
  }

  @Override
  public List<RadComponent> getChildren() {
    return myChildren;
  }

  @Override
  public Rectangle getBounds() {
    return myBounds;
  }

  @Override
  public Rectangle getBounds(Component relativeTo) {
    return SwingUtilities.convertRectangle(myNativeComponent, myBounds, relativeTo);
  }

  public void setBounds(int x, int y, int width, int height) {
    myBounds.setBounds(x, y, width, height);
  }

  public Component getNativeComponent() {
    return myNativeComponent;
  }

  public void setNativeComponent(Component nativeComponent) {
    myNativeComponent = nativeComponent;
  }

  @Override
  public Point convertPoint(Component component, int x, int y) {
    return SwingUtilities.convertPoint(component, x, y, myNativeComponent);
  }

  @Override
  public void addSelectionActions(DefaultActionGroup actionGroup, JComponent shortcuts, List<RadComponent> selection) {
    if (myTag != null && myTag.getName().equals("LinearLayout") && selection.size() == 1 && selection.get(0) == this) {
      AnAction action = new AnAction() {
        @Override
        public void actionPerformed(AnActionEvent e) {
          System.out.println("LinearLayout: " + e);
        }
      };
      action.copyFrom(LinearLayout);
      actionGroup.add(action);
    }
  }

  @Override
  public List<Property> getProperties() {
    if (myProperties == null && myTag != null) {
      myProperties = new ArrayList<Property>();
      for (XmlAttribute attribute : myTag.getAttributes()) {
        String name = attribute.getName();
        if (name.equals("xmlns:android")) {
          continue;
        }

        Property property = new AttributeProperty(null, new String(name).replace("android:", "").replace('_', ' '), name);
        property.setImportant(name.equals("android:text"));
        property.setExpert(name.equals("android:id"));
        property.setDeprecated(name.equals("android:background"));
        myProperties.add(property);
      }
    }
    return myProperties == null ? super.getProperties() : myProperties;
  }
}