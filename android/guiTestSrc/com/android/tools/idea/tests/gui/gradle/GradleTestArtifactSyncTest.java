/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.tools.idea.tests.gui.gradle;

import com.android.tools.idea.gradle.project.GradleExperimentalSettings;
import com.android.tools.idea.tests.gui.framework.BelongsToTestGroups;
import com.android.tools.idea.tests.gui.framework.GuiTestCase;
import com.android.tools.idea.tests.gui.framework.IdeGuiTest;
import com.android.tools.idea.tests.gui.framework.IdeGuiTestSetup;
import com.android.tools.idea.tests.gui.framework.fixture.EditorFixture;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.tabs.impl.TabLabel;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;

import static com.android.tools.idea.tests.gui.framework.TestGroup.PROJECT_SUPPORT;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@BelongsToTestGroups({PROJECT_SUPPORT})
@IdeGuiTestSetup(skipSourceGenerationOnSync = true)
public class GradleTestArtifactSyncTest extends GuiTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    GradleExperimentalSettings.getInstance().LOAD_ALL_TEST_ARTIFACTS = true;
  }

  @Test @IdeGuiTest
  public void testLoadBothTestArtifacts() throws IOException {
    myProjectFrame = importProjectAndWaitForProjectSyncToFinish("LoadMultiTestArtifacts");
    EditorFixture editor = myProjectFrame.getEditor();

    Module appModule = myProjectFrame.getModule("app");
    // Following source roots will to be loaded:
    // main test freeTest debugTest androidTest freeAndroidTest res
    assertEquals(7, ModuleRootManager.getInstance(appModule).getSourceRoots().length);

    // Refer to the test source file for the reason of unresolved references
    editor.open("app/src/androidTest/java/com/example/ApplicationTest.java");
    editor.requireCodeAnalysisHighlightCount(HighlightSeverity.ERROR, 3);
    editor.moveTo(editor.findOffset("Test^Util util"));
    assertGotoFile(editor, "androidTest/java/com/example/TestUtil.java");

    editor.open("app/src/test/java/com/example/UnitTest.java");
    editor.requireCodeAnalysisHighlightCount(HighlightSeverity.ERROR, 2);
    editor.moveTo(editor.findOffset("Test^Util util"));
    assertGotoFile(editor, "test/java/com/example/TestUtil.java");
  }

  private static Color BLUE = new JBColor(new Color(0xdcf0ff), new Color(0x3C476B));
  private static Color GREEN = new JBColor(new Color(231, 250, 219), new Color(0x425444));

  @Test @IdeGuiTest
  public void testTestFileBackground() throws Exception {
    myProjectFrame = importSimpleApplication();
    EditorFixture editor = myProjectFrame.getEditor();

    editor.open("app/src/test/java/google/simpleapplication/UnitTest.java");
    TabLabel tabLabel = myRobot.finder().findByType(TabLabel.class);
    assertEquals(GREEN, tabLabel.getInfo().getTabColor());

    editor.close();
    editor.open("app/src/androidTest/java/google/simpleapplication/ApplicationTest.java");
    tabLabel = myRobot.finder().findByType(TabLabel.class);
    assertEquals(BLUE, tabLabel.getInfo().getTabColor());
  }

  private static void assertGotoFile(@NotNull EditorFixture editor, @NotNull String pathSuffix) {
    editor.invokeAction(EditorFixture.EditorAction.GOTO_DECLARATION);
    assertNotNull(editor.getCurrentFile());
    assertTrue(editor.getCurrentFile().getPath().endsWith(pathSuffix));
  }
}
