/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.android;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("plugins/android-extensions/android-extensions-idea/testData/android/usageHighlighting")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class AndroidUsageHighlightingTestGenerated extends AbstractAndroidUsageHighlightingTest {
    public void testAllFilesPresentInUsageHighlighting() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/android-extensions/android-extensions-idea/testData/android/usageHighlighting"), Pattern.compile("^([^\\.]+)$"), TargetBackend.ANY, false);
    }

    @TestMetadata("simple")
    public void testSimple() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/usageHighlighting/simple/");
        doTest(fileName);
    }
}
