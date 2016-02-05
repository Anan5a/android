/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tools.idea.gradle.structure.configurables.android.treeview;

import com.android.tools.idea.gradle.structure.model.PsdModel;
import com.google.common.collect.Lists;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractPsdNode<T extends PsdModel> extends SimpleNode {
  @NotNull private final List<T> myModels;

  private boolean myAutoExpandNode;

  public AbstractPsdNode(@NotNull T...models) {
    myModels = Lists.newArrayList(models);
  }

  public AbstractPsdNode(@NotNull List<T> models) {
    myModels = models;
  }

  @NotNull
  public List<T> getModels() {
    return myModels;
  }

  @Override
  public boolean isAutoExpandNode() {
    return myAutoExpandNode;
  }

  public void setAutoExpandNode(boolean autoExpandNode) {
    myAutoExpandNode = autoExpandNode;
  }

  public boolean matches(@NotNull PsdModel model) {
    int modelCount = myModels.size();
    if (modelCount == 1) {
      return myModels.get(0).equals(model);
    }
    return false;
  }
}
