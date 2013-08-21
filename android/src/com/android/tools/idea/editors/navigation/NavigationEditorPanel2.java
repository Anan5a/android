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
package com.android.tools.idea.editors.navigation;

import com.android.navigation.*;
import com.android.tools.idea.rendering.RenderedView;
import com.android.tools.idea.rendering.ShadowPainter;
import com.intellij.ide.dnd.DnDEvent;
import com.intellij.ide.dnd.DnDManager;
import com.intellij.ide.dnd.DnDTarget;
import com.intellij.ide.dnd.TransferableWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiQualifiedNamedElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.Gray;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;

import static com.android.tools.idea.editors.navigation.Utilities.*;

public class NavigationEditorPanel2 extends JComponent {
  private static final Dimension GAP = new Dimension(150, 50);
  private static final Color BACKGROUND_COLOR = Gray.get(192);
  private static final Color SNAP_GRID_LINE_COLOR_MINOR = Gray.get(180);
  private static final Color SNAP_GRID_LINE_COLOR_MIDDLE = Gray.get(170);
  private static final Color SNAP_GRID_LINE_COLOR_MAJOR = Gray.get(160);

  // Snap grid
  private static final int MINOR_SNAP = 4;
  private static final int MIDDLE_COUNT = 5;
  private static final int MAJOR_COUNT = 10;
  private static final Dimension MINOR_SNAP_GRID = new Dimension(MINOR_SNAP, MINOR_SNAP);
  private static final Dimension MIDDLE_SNAP_GRID = scale(MINOR_SNAP_GRID, MIDDLE_COUNT);
  private static final Dimension MAJOR_SNAP_GRID = scale(MINOR_SNAP_GRID, MAJOR_COUNT);

  private static final float SCALE = 1 / 3f;
  private static final Dimension ORIGINAL_SIZE = new Dimension(480, 800);
  private static final Dimension PREVIEW_SIZE = scale(ORIGINAL_SIZE, SCALE);
  private static final Point MIDDLE_OF_PREVIEW = point(scale(PREVIEW_SIZE, 0.5f));
  private static final int LINE_WIDTH = 3;
  private static final Point MULTIPLE_DROP_STRIDE = point(MAJOR_SNAP_GRID);
  private static final String ID_PREFIX = "@+id/";
  private static final Color TRANSITION_LINE_COLOR = new Color(80, 80, 255);

  private final NavigationModel myNavigationModel;
  private final Project myProject;
  private boolean myStateCacheIsValid;
  private boolean myTransitionEditorCacheIsValid;
  private VirtualFileSystem myFileSystem;
  private String myPath;
  @NotNull private Selections.Selection mySelection = Selections.NULL;
  private final Assoc<State, AndroidRootComponent> myStateComponentAssociation =
    new Assoc<State, AndroidRootComponent>();
  private final Assoc<Transition, Component> myTransitionEditorAssociation =
    new Assoc<Transition, Component>();
  private Map<Locator, RenderedView> myLocationToRenderedView = null;
  private Image myBackgroundImage;

  private Assoc<State, AndroidRootComponent> getStateComponentAssociation() {
    if (!myStateCacheIsValid) {
      syncStateCache(myStateComponentAssociation);
      myStateCacheIsValid = true;
    }
    return myStateComponentAssociation;
  }

  private Assoc<Transition, Component> getTransitionEditorAssociation() {
    if (!myTransitionEditorCacheIsValid) {
      syncTransitionCache(myTransitionEditorAssociation);
      myTransitionEditorCacheIsValid = true;
    }
    return myTransitionEditorAssociation;
  }

  static class Assoc<K, V> {
    public final Map<K, V> keyToValue = new HashMap<K, V>();
    public final Map<V, K> valueToKey = new HashMap<V, K>();

    public void add(K key, V value) {
      keyToValue.put(key, value);
      valueToKey.put(value, key);
    }

    public void remove(K key, V value) {
      keyToValue.remove(key);
      valueToKey.remove(value);
    }

    public void clear() {
      keyToValue.clear();
      valueToKey.clear();
    }
  }

  @Nullable
  static String getViewId(@Nullable RenderedView leaf) {
    if (leaf != null) {
      XmlTag tag = leaf.tag;
      if (tag != null) {
        String attributeValue = tag.getAttributeValue("android:id");
        if (attributeValue != null && attributeValue.startsWith(ID_PREFIX)) {
          return attributeValue.substring(ID_PREFIX.length());
        }
      }
    }
    return null;
  }

  private Map<Locator, RenderedView> getLocationToRenderedView() {
    if (myLocationToRenderedView == null) {
      myLocationToRenderedView = new HashMap<Locator, RenderedView>();
      for (final State state : myNavigationModel.getStates()) {
        new Object() {
          void walk(RenderedView parent) {
            for (RenderedView child : parent.getChildren()) {
              String id = getViewId(child);
              if (id != null) {
                Locator locator = new Locator(state);
                locator.setViewName(id);
                myLocationToRenderedView.put(locator, child);
              }
              walk(child);
            }
          }
        }.walk(getStateComponentAssociation().keyToValue.get(state).getRootView());
      }
    }
    return myLocationToRenderedView;
  }

  public static void paintLeaf(Graphics g,
                                @Nullable RenderedView leaf,
                                Color color,
                                AndroidRootComponent component) {
    if (leaf != null) {
      Color oldColor = g.getColor();
      g.setColor(color);
      Rectangle r = component.getBounds(leaf);
      g.drawRect(r.x, r.y, r.width, r.height);
      g.setColor(oldColor);
    }
  }

  private void registerKeyBinding(int keyCode, String name, Action action) {
    InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), name);
    getActionMap().put(name, action);
  }

  public NavigationEditorPanel2(Project project, VirtualFile file, NavigationModel model) {
    myProject = project;
    myFileSystem = file.getFileSystem();
    myPath = file.getParent().getParent().getPath();
    myNavigationModel = model;

    setFocusable(true);
    setBackground(BACKGROUND_COLOR);

    // Mouse listener
    {
      MouseAdapter mouseListener = new MyMouseListener();
      addMouseListener(mouseListener);
      addMouseMotionListener(mouseListener);
    }

    // Focus listener
    {
      addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent focusEvent) {
          repaint();
        }

        @Override
        public void focusLost(FocusEvent focusEvent) {
          repaint();
        }
      });
    }

    // Drag and Drop listener
    {
      final DnDManager dndManager = DnDManager.getInstance();
      dndManager.registerTarget(new MyDnDTarget(), this);
    }

    // Key listeners
    {
      Action remove = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          mySelection.remove();
          setSelection(Selections.NULL);
        }
      };
      registerKeyBinding(KeyEvent.VK_DELETE, "delete", remove);
      registerKeyBinding(KeyEvent.VK_BACK_SPACE, "backspace", remove);
    }

    // Model listener
    {
      myNavigationModel.getListeners().add(new Listener<NavigationModel.Event>() {
        @Override
        public void notify(@NotNull NavigationModel.Event event) {
          if (event.operation != NavigationModel.Event.Operation.UPDATE) {
            if (event.operandType.isAssignableFrom(State.class)) {
              myStateCacheIsValid = false;
            }
            if (event.operandType.isAssignableFrom(Transition.class)) {
              myTransitionEditorCacheIsValid = false;
            }
          }
          repaint();
        }
      });
    }
  }

  private void setSelection(@NotNull Selections.Selection selection) {
    mySelection = selection;
    repaint();
  }

  private void moveSelection(Point location) {
    mySelection.moveTo(location);
    revalidate();
    repaint();
  }

  private void finaliseSelectionLocation(Point location) {
    mySelection = mySelection.finaliseSelectionLocation(location, getComponentAt(location), getStateComponentAssociation().valueToKey);
    revalidate();
    repaint();
  }

  /*
  private List<State> findDestinationsFor(State state, Set<State> exclude) {
    List<State> result = new ArrayList<State>();
    for (Transition transition : myNavigationModel) {
      State source = transition.getSource();
      if (source.equals(state)) {
        State destination = transition.getDestination();
        if (!exclude.contains(destination)) {
          result.add(destination);
        }
      }
    }
    return result;
  }
  */

  private void drawGrid(Graphics g, Color c, Dimension size) {
    g.setColor(c);
    for (int x = 0; x < getWidth(); x += size.width) {
      g.drawLine(x, 0, x, getHeight());
    }
    for (int y = 0; y < getHeight(); y += size.height) {
      g.drawLine(0, y, getWidth(), y);
    }
  }

  private void drawBackground(Graphics g, int width, int height) {
    g.setColor(BACKGROUND_COLOR);
    g.fillRect(0, 0, width, height);

    drawGrid(g, SNAP_GRID_LINE_COLOR_MINOR, MINOR_SNAP_GRID);
    drawGrid(g, SNAP_GRID_LINE_COLOR_MIDDLE, MIDDLE_SNAP_GRID);
    drawGrid(g, SNAP_GRID_LINE_COLOR_MAJOR, MAJOR_SNAP_GRID);
  }

  private Image getBackGroundImage() {
    if (myBackgroundImage == null ||
        myBackgroundImage.getWidth(null) != getWidth() ||
        myBackgroundImage.getHeight(null) != getHeight()) {
      myBackgroundImage = UIUtil.createImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
      drawBackground(myBackgroundImage.getGraphics(), getWidth(), getHeight());
    }
    return myBackgroundImage;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    // draw background
    g.drawImage(getBackGroundImage(), 0, 0, null);

    // draw component shadows
    for (Component c : getStateComponentAssociation().keyToValue.values()) {
      Rectangle r = c.getBounds();
      ShadowPainter.drawRectangleShadow(g, r.x, r.y, r.width, r.height);
    }

    // draw selection
    mySelection.paint(g, hasFocus());
  }

  public static Graphics2D createTransitionGraphics(Graphics g) {
    Graphics2D g2D = (Graphics2D)g.create();
    g2D.setColor(TRANSITION_LINE_COLOR);
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2D.setStroke(new BasicStroke(LINE_WIDTH));
    return g2D;
  }

  private void paintTransitions(Graphics2D g) {
    Map<State, AndroidRootComponent> stateToComponent = getStateComponentAssociation().keyToValue;
    Map<Transition, Component> transitionToEditor = getTransitionEditorAssociation().keyToValue;

    for (Transition transition : myNavigationModel.getTransitions()) {
      State source = transition.getSource().getState();
      State destination = transition.getDestination().getState();
      AndroidRootComponent sourceComponent = stateToComponent.get(source);
      AndroidRootComponent destinationComponent = stateToComponent.get(destination);

      Rectangle r1 = sourceComponent.getBounds(getSourceView(transition));
      Rectangle r2 = transitionToEditor.get(transition).getBounds();
      Rectangle r3 = destinationComponent.getBounds(getDestinationView(transition));

      g.drawRect(r1.x, r1.y, r1.width, r1.height);
      Point m1 = Utilities.centre(r1);
      Point m2 = Utilities.centre(r2);
      Point m3 = Utilities.centre(r3);

      Point A = new Point(m2.x, m1.y);
      Point B = new Point(m2.x, m3.y);

      Point p1 = Utilities.project(r1, A);
      Point p2 = Utilities.project(r2, A);

      Point p3 = Utilities.project(r2, B);
      Point p4 = Utilities.project(r3, B);

      g.drawLine(p1.x, p1.y, A.x, A.y);
      g.drawLine(A.x, A.y, p2.x, p2.y);
      g.drawLine(p3.x, p3.y, B.x, B.y);
      Utilities.drawArrow(g, B.x, B.y, p4.x, p4.y);

      Color oldColor = g.getColor();
      g.setColor(Color.CYAN);
      g.drawRect(r3.x, r3.y, r3.width, r3.height);
      g.setColor(oldColor);
    }
  }

  private RenderedView getSourceView(Transition t) {
    return getLocationToRenderedView().get(t.getSource());
  }

  private RenderedView getDestinationView(Transition t) {
    return getLocationToRenderedView().get(t.getDestination());
  }

  @Override
  protected void paintChildren(Graphics g) {
    super.paintChildren(g);
    paintTransitions(createTransitionGraphics(g));
    mySelection.paintOver(g);
  }

  @Override
  public void doLayout() {
    Map<State, AndroidRootComponent> stateToComponent = getStateComponentAssociation().keyToValue;
    Map<Transition, Component> transitionToEditor = getTransitionEditorAssociation().keyToValue;

    for (Transition transition : myNavigationModel.getTransitions()) {
      AndroidRootComponent source = stateToComponent.get(transition.getSource().getState());
      AndroidRootComponent dest = stateToComponent.get(transition.getDestination().getState());
      Point sl = Utilities.centre(source.getBounds(getSourceView(transition)));
      Point dl = Utilities.centre(dest.getBounds(getDestinationView(transition)));
      String gesture = transition.getType();
      if (gesture != null) {
        Component c = transitionToEditor.get(transition);
        c.setSize(c.getPreferredSize());
        int sx = (sl.x + dl.x - c.getWidth()) / 2;
        int sy = (sl.y + dl.y - c.getHeight()) / 2;
        c.setLocation(sx, sy);
      }
    }
  }

  /*
  private void addChildrenOld(Collection<State> states) {
    final Set<State> visited = new HashSet<State>();
    final Point location = new Point(GAP.width, GAP.height);
    final Point maxLocation = new Point(0, 0);
    final int gridWidth = PREVIEW_SIZE.width + GAP.width;
    final int gridHeight = PREVIEW_SIZE.height + GAP.height;
    getStateComponentAssociation().clear();
    for (State state : states) {
      if (visited.contains(state)) {
        continue;
      }
      new Object() {
        public void addChildrenFor(State source) {
          visited.add(source);
          add(createRootComponentFor(source, location));
          List<State> children = findDestinationsFor(source, visited);
          location.x += gridWidth;
          maxLocation.x = Math.max(maxLocation.x, location.x);
          if (children.isEmpty()) {
            location.y += gridHeight;
            maxLocation.y = Math.max(maxLocation.y, location.y);
          }
          else {
            for (State child : children) {
              addChildrenFor(child);
            }
          }
          location.x -= gridWidth;
        }
      }.addChildrenFor(state);
    }
    setPreferredSize(new Dimension(maxLocation.x, maxLocation.y));
  }
  */

  private <K, V extends Component> void removeLeftovers(Assoc<K, V> assoc, Collection<K> a) {
    for (Map.Entry<K, V> e : new ArrayList<Map.Entry<K, V>>(assoc.keyToValue.entrySet())) {
      K k = e.getKey();
      V v = e.getValue();
      if (!a.contains(k)) {
        assoc.remove(k, v);
        remove(v);
        repaint();
      }
    }
  }

  private JComboBox createEditorFor(final Transition transition) {
    String gesture = transition.getType();
    JComboBox c = new JComboBox(new Object[]{"", "click", "list", "menu", "contains", "update"});
    c.setSelectedItem(gesture);
    c.setForeground(getForeground());
    //c.setBorder(LABEL_BORDER);
    //c.setOpaque(true);
    c.setBackground(BACKGROUND_COLOR);
    c.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent itemEvent) {
        transition.setType((String)itemEvent.getItem());
        myNavigationModel.getListeners().notify(NavigationModel.Event.update(Transition.class));
      }
    });
    return c;
  }

  private void syncTransitionCache(Assoc<Transition, Component> assoc) {
    // add anything that is in the model but not in our cache
    for (Transition transition : myNavigationModel.getTransitions()) {
      if (!assoc.keyToValue.containsKey(transition)) {
        Component editor = createEditorFor(transition);
        add(editor);
        assoc.add(transition, editor);
      }
    }
    // remove anything that is in our cache but not in the model
    removeLeftovers(assoc, myNavigationModel.getTransitions());
  }

  private AndroidRootComponent createRootComponentFor(State state, Point point) {
    AndroidRootComponent result = new AndroidRootComponent();
    result.setScale(SCALE);
    VirtualFile file = myFileSystem.findFileByPath(myPath + "/layout/" +
                                                   state.getXmlResourceName() + ".xml");
    if (file != null) {
      result.render(myProject, file);
    }
    result.setLocation(point);
    result.setSize(PREVIEW_SIZE);
    return result;
  }

  private void setPreferredSize(Set<AndroidRootComponent> roots) {
    Dimension size = PREVIEW_SIZE;
    Dimension gridSize = new Dimension(size.width + GAP.width, size.height + GAP.height);
    Point maxLoc = new Point(0, 0);
    for (AndroidRootComponent c : roots) {
      maxLoc = Utilities.max(maxLoc, c.getLocation());
    }
    setPreferredSize(new Dimension(maxLoc.x + gridSize.width, maxLoc.y + gridSize.height));
  }

  private void syncStateCache(Assoc<State, AndroidRootComponent> assoc) {
    // add anything that is in the model but not in our cache
    for (State state : myNavigationModel.getStates()) {
      if (!assoc.keyToValue.containsKey(state)) {
        Point p = Utilities.toAWTPoint(state.getLocation());
        AndroidRootComponent root = createRootComponentFor(state, p);
        assoc.add(state, root);
        add(root);
      }
    }
    // remove anything that is in our cache but not in the model
    removeLeftovers(assoc, myNavigationModel.getStates());

    setPreferredSize(assoc.valueToKey.keySet());
  }

  private static int snap(int i, int d) {
    return ((int)Math.round((double)i / d)) * d;
  }

  public static Point snap(Point p) {
    return new Point(snap(p.x, MIDDLE_SNAP_GRID.width), snap(p.y, MIDDLE_SNAP_GRID.height));
  }

  private class MyMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      Point location = e.getPoint();
      boolean modified =
        (e.isShiftDown() || e.isControlDown() || e.isMetaDown()) && !e.isPopupTrigger();
      setSelection(Selections.create(location, modified, myNavigationModel, getComponentAt(location),
                                     getTransitionEditorAssociation().valueToKey.get(getComponentAt(location)),
                                     getStateComponentAssociation().valueToKey));
      requestFocus();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
      moveSelection(mouseEvent.getPoint());
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
      finaliseSelectionLocation(mouseEvent.getPoint());
    }
  }

  private class MyDnDTarget implements DnDTarget {
    private int applicableDropCount = 0;

    private void dropOrPrepareToDrop(DnDEvent anEvent, boolean execute) {
      Object attachedObject = anEvent.getAttachedObject();
      if (attachedObject instanceof TransferableWrapper) {
        TransferableWrapper wrapper = (TransferableWrapper)attachedObject;
        PsiElement[] psiElements = wrapper.getPsiElements();
        Point dropLocation =
          diff(anEvent.getPointOn(NavigationEditorPanel2.this), MIDDLE_OF_PREVIEW);

        if (psiElements != null) {
          for (PsiElement element : psiElements) {
            if (element instanceof PsiQualifiedNamedElement) {
              PsiQualifiedNamedElement namedElement = (PsiQualifiedNamedElement)element;
              String qualifiedName = namedElement.getQualifiedName();
              if (qualifiedName != null) {
                State state = new State(qualifiedName);
                state.setLocation(Utilities.toNavPoint(snap(dropLocation)));
                String name = namedElement.getName();
                if (name != null) {
                  state.setXmlResourceName(getXmlFileNameFromJavaFileName(name));
                }
                if (!getStateComponentAssociation().keyToValue.containsKey(state)) {
                  if (execute) {
                    myNavigationModel.addState(state);
                  }
                  else {
                    applicableDropCount++;
                  }
                }
                dropLocation = Utilities.add(dropLocation, MULTIPLE_DROP_STRIDE);
              }
            }
          }
        }
      }
      if (execute) {
        revalidate();
        repaint();
      }
    }

    @Override
    public boolean update(DnDEvent anEvent) {
      applicableDropCount = 0;
      dropOrPrepareToDrop(anEvent, false);
      anEvent.setDropPossible(applicableDropCount > 0);
      return false;
    }

    @Override
    public void drop(DnDEvent anEvent) {
      dropOrPrepareToDrop(anEvent, true);
    }


    @Override
    public void cleanUpOnLeave() {
    }

    @Override
    public void updateDraggedImage(Image image, Point dropPoint, Point imageOffset) {
    }

  }

  private static String getXmlFileNameFromJavaFileName(String name) {
    return Utilities.getXmlFileNameFromJavaFileName(name);
  }

}
