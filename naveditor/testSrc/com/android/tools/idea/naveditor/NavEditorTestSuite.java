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
package com.android.tools.idea.naveditor;

import com.android.testutils.JarTestSuiteRunner;
import com.android.tools.tests.IdeaTestSuiteBase;
import com.android.tools.tests.LeakCheckerRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(JarTestSuiteRunner.class)
@JarTestSuiteRunner.ExcludeClasses({NavEditorTestSuite.class})
public class NavEditorTestSuite extends IdeaTestSuiteBase {
  @ClassRule public static LeakCheckerRule checker = new LeakCheckerRule();

  static {
    symlinkToIdeaHome(
      "prebuilts/studio/layoutlib",
      "tools/adt/idea/android/testData",
      "tools/adt/idea/designer/testData",
      "tools/adt/idea/naveditor/testData",
      "tools/idea/java");

    setUpOfflineRepo("tools/adt/idea/android/test_deps.zip", "prebuilts/tools/common/m2/repository");
  }
}
