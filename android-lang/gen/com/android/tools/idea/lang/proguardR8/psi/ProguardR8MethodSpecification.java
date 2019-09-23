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

// ATTENTION: This file has been automatically generated from proguardR8.bnf. Do not edit it manually.

package com.android.tools.idea.lang.proguardR8.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ProguardR8MethodSpecification extends PsiElement {

  @Nullable
  ProguardR8AnnotationName getAnnotationName();

  @Nullable
  ProguardR8AnyFieldOrMethod getAnyFieldOrMethod();

  @Nullable
  ProguardR8ClassName getClassName();

  @Nullable
  ProguardR8Method getMethod();

  @NotNull
  List<ProguardR8MethodModifier> getMethodModifierList();

  @Nullable
  ProguardR8Parameters getParameters();

}
