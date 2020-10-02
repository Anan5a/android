/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
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
@TestMetadata("android-extensions-idea/testData/android/rename")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class AndroidRenameTestGenerated extends AbstractAndroidRenameTest {
    public void testAllFilesPresentInRename() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("android-extensions-idea/testData/android/rename"), Pattern.compile("^([^\\.]+)$"), TargetBackend.ANY, false);
    }

    @TestMetadata("commonElementId")
    public void testCommonElementId() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("android-extensions-idea/testData/android/rename/commonElementId/");
        doTest(fileName);
    }

    @TestMetadata("fqNameInAttr")
    public void testFqNameInAttr() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("android-extensions-idea/testData/android/rename/fqNameInAttr/");
        doTest(fileName);
    }

    @TestMetadata("fqNameInAttrFragment")
    public void testFqNameInAttrFragment() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("android-extensions-idea/testData/android/rename/fqNameInAttrFragment/");
        doTest(fileName);
    }

    @TestMetadata("multiFile")
    public void testMultiFile() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("android-extensions-idea/testData/android/rename/multiFile/");
        doTest(fileName);
    }

    @TestMetadata("multiFileFragment")
    public void testMultiFileFragment() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("android-extensions-idea/testData/android/rename/multiFileFragment/");
        doTest(fileName);
    }

    @TestMetadata("simple")
    public void testSimple() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("android-extensions-idea/testData/android/rename/simple/");
        doTest(fileName);
    }

    @TestMetadata("simpleFragment")
    public void testSimpleFragment() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("android-extensions-idea/testData/android/rename/simpleFragment/");
        doTest(fileName);
    }

    @TestMetadata("simpleView")
    public void testSimpleView() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("android-extensions-idea/testData/android/rename/simpleView/");
        doTest(fileName);
    }
}
