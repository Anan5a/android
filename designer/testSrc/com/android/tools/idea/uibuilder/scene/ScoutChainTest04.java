/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.idea.uibuilder.scene;

import static com.android.AndroidXConstants.CONSTRAINT_LAYOUT;
import static com.android.SdkConstants.TEXT_VIEW;

import com.android.tools.idea.common.fixtures.ModelBuilder;
import com.android.tools.idea.common.model.NlComponent;
import com.android.tools.idea.uibuilder.scout.Scout;
import com.intellij.openapi.command.WriteCommandAction;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Check that views get id
 */
public class ScoutChainTest04 extends SceneTest {
  @Override
  @NotNull
  public ModelBuilder createModel() {
    return model("constraint.xml",
                 component(CONSTRAINT_LAYOUT.defaultName())
                   .id("@+id/content_main")
                   .withBounds(0, 0, 720, 1024)
                   .width("360dp")
                   .height("512dp")
                   .children(
                     component(TEXT_VIEW)
                       .id("@+id/a")
                       .withBounds(8, 177, 98, 34)
                       .width("wrap_content")
                       .height("wrap_content"),
                     component(TEXT_VIEW)
                       .id("@+id/b")
                       .withBounds(8, 389, 98, 34)
                       .width("wrap_content")
                       .height("wrap_content"),
                     component(TEXT_VIEW)
                       .id("@+id/c")
                       .withBounds(8, 601, 98, 34)
                       .width("wrap_content")
                       .height("wrap_content"),
                     component(TEXT_VIEW)
                       .id("@+id/d")
                       .withBounds(8, 812, 98, 34)
                       .width("wrap_content")
                       .height("wrap_content")
                   ));
  }

  public void testRTLScene() {
    myScreen.get("@+id/content_main")
      .expectXml("<android.support.constraint.ConstraintLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                 "  android:id=\"@+id/content_main\"\n" +
                 "  android:layout_width=\"360dp\"\n" +
                 "  android:layout_height=\"512dp\">\n" +
                 "\n" +
                 "  <TextView\n" +
                 "    android:id=\"@+id/a\"\n" +
                 "    android:layout_width=\"wrap_content\"\n" +
                 "    android:layout_height=\"wrap_content\"/>\n" +
                 "\n" +
                 "  <TextView\n" +
                 "    android:id=\"@+id/b\"\n" +
                 "    android:layout_width=\"wrap_content\"\n" +
                 "    android:layout_height=\"wrap_content\"/>\n" +
                 "\n" +
                 "  <TextView\n" +
                 "    android:id=\"@+id/c\"\n" +
                 "    android:layout_width=\"wrap_content\"\n" +
                 "    android:layout_height=\"wrap_content\"/>\n" +
                 "\n" +
                 "  <TextView\n" +
                 "    android:id=\"@+id/d\"\n" +
                 "    android:layout_width=\"wrap_content\"\n" +
                 "    android:layout_height=\"wrap_content\"/>\n" +
                 "\n" +
                 "</android.support.constraint.ConstraintLayout>");
    List<NlComponent> list = myModel.getComponents().get(0).getChildren();
    WriteCommandAction.runWriteCommandAction(myFacet.getModule().getProject(), () -> {
      Scout.inferConstraintsAndCommit(myModel.getComponents());
    });
    myScreen.get("@+id/content_main")
      .expectXml("<android.support.constraint.ConstraintLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                 "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
                 "    android:id=\"@+id/content_main\"\n" +
                 "    android:layout_width=\"360dp\"\n" +
                 "    android:layout_height=\"512dp\">\n" +
                 "\n" +
                 "    <TextView\n" +
                 "        android:id=\"@+id/a\"\n" +
                 "        android:layout_width=\"wrap_content\"\n" +
                 "        android:layout_height=\"wrap_content\"\n" +
                 "        android:layout_marginStart=\"4dp\"\n" +
                 "        android:layout_marginLeft=\"4dp\"\n" +
                 "        android:layout_marginTop=\"1dp\"\n" +
                 "        android:layout_marginBottom=\"1dp\"\n" +
                 "        app:layout_constraintBottom_toTopOf=\"@+id/b\"\n" +
                 "        app:layout_constraintStart_toStartOf=\"parent\"\n" +
                 "        app:layout_constraintTop_toTopOf=\"parent\" />\n" +
                 "\n" +
                 "    <TextView\n" +
                 "        android:id=\"@+id/b\"\n" +
                 "        android:layout_width=\"wrap_content\"\n" +
                 "        android:layout_height=\"wrap_content\"\n" +
                 "        android:layout_marginStart=\"4dp\"\n" +
                 "        android:layout_marginLeft=\"4dp\"\n" +
                 "        android:layout_marginBottom=\"1dp\"\n" +
                 "        app:layout_constraintBottom_toTopOf=\"@+id/c\"\n" +
                 "        app:layout_constraintStart_toStartOf=\"parent\"\n" +
                 "        app:layout_constraintTop_toBottomOf=\"@+id/a\" />\n" +
                 "\n" +
                 "    <TextView\n" +
                 "        android:id=\"@+id/c\"\n" +
                 "        android:layout_width=\"wrap_content\"\n" +
                 "        android:layout_height=\"wrap_content\"\n" +
                 "        android:layout_marginStart=\"4dp\"\n" +
                 "        android:layout_marginLeft=\"4dp\"\n" +
                 "        app:layout_constraintBottom_toTopOf=\"@+id/d\"\n" +
                 "        app:layout_constraintStart_toStartOf=\"parent\"\n" +
                 "        app:layout_constraintTop_toBottomOf=\"@+id/b\" />\n" +
                 "\n" +
                 "    <TextView\n" +
                 "        android:id=\"@+id/d\"\n" +
                 "        android:layout_width=\"wrap_content\"\n" +
                 "        android:layout_height=\"wrap_content\"\n" +
                 "        android:layout_marginStart=\"4dp\"\n" +
                 "        android:layout_marginLeft=\"4dp\"\n" +
                 "        android:layout_marginBottom=\"1dp\"\n" +
                 "        app:layout_constraintBottom_toBottomOf=\"parent\"\n" +
                 "        app:layout_constraintStart_toStartOf=\"parent\"\n" +
                 "        app:layout_constraintTop_toBottomOf=\"@+id/c\" />\n" +
                 "\n" +
                 "</android.support.constraint.ConstraintLayout>");
  }
}