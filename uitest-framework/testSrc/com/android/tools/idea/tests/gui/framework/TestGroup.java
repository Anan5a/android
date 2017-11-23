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
package com.android.tools.idea.tests.gui.framework;

public enum TestGroup {
  /** For measuring gradle sync performance **/
  SYNC_PERFORMANCE,
  /** For preparing the SYNC_PERFORMANCE test; {@see GradleSyncGuiPerfTestSetup} **/
  SYNC_PERFORMANCE_SETUP,
  PROJECT_SUPPORT,
  PROJECT_WIZARD,
  THEME,
  EDITING,
  QA,
  QA_UNRELIABLE(/* per-test forked execution */ true),
  SANITY,
  UNRELIABLE,
  /** Assigned implicitly where group is unspecified; not intended to be specified explicitly. */
  DEFAULT;

  // This is similar to JUnit's "perTest" forked execution mode, which means each test class within a suite uses a fresh JVM.
  private final boolean isForked;

  TestGroup() {
    this(false);
  }

  TestGroup(boolean fork) {
    isForked = fork;
  }

  public boolean isForked() {
    return isForked;
  }
}
