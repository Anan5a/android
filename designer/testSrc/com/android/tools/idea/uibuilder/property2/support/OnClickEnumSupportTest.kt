/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tools.idea.uibuilder.property2.support

import com.android.SdkConstants.FRAME_LAYOUT
import com.android.SdkConstants.TEXT_VIEW
import com.android.tools.idea.uibuilder.property2.testutils.SupportTestUtil
import com.android.tools.property.panel.api.HeaderEnumValue
import com.google.common.truth.Truth.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.android.AndroidTestCase

@Language("JAVA")
private const val MAIN_ACTIVITY = """
  package p1.p2;

  import android.app.Activity;
  import android.os.Bundle;
  import android.view.View;

  public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
    }

    public void help(View view) {
    }
  }
"""

@Language("JAVA")
private const val OTHER_ACTIVITY = """
  package p1.p2;

  import android.app.Activity;
  import android.os.Bundle;
  import android.view.View;

  public class OtherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
    }

    public void startProcessing(View view) {
    }
  }
"""

@Language("JAVA")
private const val THIRD_ACTIVITY = """
  package p1.p2;

  import android.app.Activity;
  import android.os.Bundle;
  import android.view.View;

  public class ThirdActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
    }
  }
"""


class OnClickEnumSupportTest: AndroidTestCase() {

  fun testWithNoActivities() {
    val util = SupportTestUtil(myFacet, myFixture, TEXT_VIEW, parentTag = FRAME_LAYOUT)
    val support = OnClickEnumSupport(util.nlModel)
    assertThat(support.values).isEmpty()
  }

  fun testWithFullyQualifiedActivityName() {
    val support = findEnumSupportFor("p1.p2.MainActivity").values
    assertThat((support[0] as HeaderEnumValue).header).isEqualTo("MainActivity")
    val values = support.subList(1, support.size)
    assertThat(values.map { it.display }).containsExactly("help", "onClick").inOrder()
    assertThat(values.map { it.value }).containsExactly("help", "onClick").inOrder()
  }

  fun testWithDotInActivityName() {
    val support = findEnumSupportFor(".MainActivity").values
    assertThat((support[0] as HeaderEnumValue).header).isEqualTo("MainActivity")
    val values = support.subList(1, support.size)
    assertThat(values.map { it.display }).containsExactly("help", "onClick").inOrder()
    assertThat(values.map { it.value }).containsExactly("help", "onClick").inOrder()
  }

  fun testWithNoActivityName() {
    val support = findEnumSupportFor("").values
    assertThat((support[0] as HeaderEnumValue).header).isEqualTo("MainActivity")
    val mainValues = support.subList(1, 3)
    assertThat((support[3] as HeaderEnumValue).header).isEqualTo("OtherActivity")
    val otherValues = support.subList(4, 6)
    assertThat(mainValues.map { it.display }).containsExactly("help", "onClick").inOrder()
    assertThat(mainValues.map { it.value }).containsExactly("help", "onClick").inOrder()
    assertThat(otherValues.map { it.display }).containsExactly("onClick", "startProcessing").inOrder()
    assertThat(otherValues.map { it.value }).containsExactly("onClick", "startProcessing").inOrder()
    assertThat(support.size).isEqualTo(6)
  }

  private fun findEnumSupportFor(activityName: String): OnClickEnumSupport {
    myFixture.addClass(MAIN_ACTIVITY.trimIndent())
    myFixture.addClass(OTHER_ACTIVITY.trimIndent())
    myFixture.addClass(THIRD_ACTIVITY.trimIndent())
    val util = SupportTestUtil(myFacet, myFixture, TEXT_VIEW, parentTag = FRAME_LAYOUT, activityName = activityName)
    return OnClickEnumSupport(util.nlModel)
  }
}
