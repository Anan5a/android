/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.tools.idea.uibuilder.motion;

import com.android.tools.idea.uibuilder.handlers.motion.editor.adapters.MTag;
import com.android.tools.idea.uibuilder.handlers.motion.editor.timeline.TimeLinePanel;
import com.android.tools.idea.uibuilder.handlers.motion.editor.timeline.TimeLineRow;
import com.android.tools.idea.uibuilder.handlers.motion.editor.timeline.TimeLineRowData;
import com.android.tools.idea.uibuilder.handlers.motion.editor.timeline.TimeLineTopLeft;
import com.android.tools.idea.uibuilder.handlers.motion.editor.ui.MeModel;
import com.android.tools.idea.uibuilder.handlers.motion.editor.ui.MotionEditor;
import com.android.tools.idea.uibuilder.motion.adapters.BaseMotionEditorTest;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class METimelineRenderingTest extends BaseMotionEditorTest {


  public void testMotionEditorPanel() {
    MotionEditor motionEditor  = new MotionEditor();
    assertTrue(motionEditor!=null);

    MeModel model = getModel();
    MTag[] trans = model.motionScene.getChildTags("Transition");
    motionEditor.setMTag(model);
    motionEditor.selectTag(trans[0]);
    int size = 1000;
    motionEditor.setBounds(0,0,size,size);
    BufferedImage bufferedImage = new BufferedImage(size,size,BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = bufferedImage.createGraphics();
    long time = System.nanoTime();
    motionEditor.paint(g2d);
    time = System.nanoTime()-time;
    assertTrue(time < 1E9);
  }
  class DummyPanel extends  JPanel {
    DummyPanel(){
      super(new BorderLayout());
    }
    public  void sendMouse(MouseEvent e) {
      super.processMouseEvent(e);
    }
    public void down(int x, int y){
      MouseEvent event = new MouseEvent(this, MouseEvent.MOUSE_PRESSED,System.currentTimeMillis(),x,y,0,1,false);
    }
    public void up(int x, int y){
      MouseEvent event = new MouseEvent(this, MouseEvent.MOUSE_RELEASED,System.currentTimeMillis(),x,y,0,1,false);
    }
    public void click(int x, int y){
      MouseEvent event = new MouseEvent(this, MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),x,y,0,1,false);
    }
  }

  public void testTimeLinePanel() {
    DummyPanel dummyPanel = new DummyPanel();
    TimeLinePanel timeLinePanel = new TimeLinePanel( );

    assertTrue(timeLinePanel!=null);

    MeModel model = getModel();
    MTag[] trans = model.motionScene.getChildTags("Transition");
    dummyPanel.add(timeLinePanel);
    int size = 1000;
    TimeLineTopLeft topLeft = (TimeLineTopLeft) find(timeLinePanel, e->{return e instanceof TimeLineTopLeft;});
    TimeLinePanel.METimeLine tl = (TimeLinePanel.METimeLine) find(timeLinePanel, e->{return e instanceof TimeLinePanel.METimeLine;});

    dummyPanel.setBounds(0,0,size,size);

    for (int i = 0; i < trans.length; i++) {
      MTag tran = trans[i];
      size/=2;
      dummyPanel.setBounds(0,0,size,size);

      dummyPanel.revalidate();

      dummyPanel.doLayout();
      dummyPanel.validate();
      BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = bufferedImage.createGraphics();
      long time = System.nanoTime();
      dummyPanel.paint(g2d);

      time = System.nanoTime() - time;
      timeLinePanel.setMTag(trans[i], model);
      for (int j = 0; j < dummyPanel.getHeight(); j+=10) {
        for (int k = 0; k < dummyPanel.getWidth(); k += 10) {
          dummyPanel.down(k, j);
          dummyPanel.up(k, j);
          dummyPanel.click(k, j);
        }
      }

      TimeLineRowData rowData = tl.getSelectedValue();
      TimeLineRow row  = tl.getTimeLineRow(0);
      assertNotNull(rowData);
      assertNotNull(row);
      row.toggleGraph();
      row.paint(g2d);

      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.PLAY);
      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.PAUSE);
      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.START);
      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.LOOP);
      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.LOOP);

      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.END);
      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.PLAY);
      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.SPEED);
      topLeft.notifyTimeLineListeners(TimeLineTopLeft.TimelineCommands.END);

      assertTrue(time < 1E9);

    }
  }

  static interface Test {
    boolean test(Component c);
  }
  static Component find(Component component, Test test) {
    if (test.test(component)){
      return component;
    }
    if (component instanceof Container) {
      Container container = (Container) component;
      int n = container.getComponentCount();
      for (int i = 0; i < n; i++) {
        Component found = find(container.getComponent(i),test);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
