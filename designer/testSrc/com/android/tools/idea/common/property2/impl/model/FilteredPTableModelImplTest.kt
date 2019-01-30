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
package com.android.tools.idea.common.property2.impl.model

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_FONT_FAMILY
import com.android.SdkConstants.ATTR_LAYOUT_GRAVITY
import com.android.SdkConstants.ATTR_LAYOUT_HEIGHT
import com.android.SdkConstants.ATTR_LAYOUT_MARGIN
import com.android.SdkConstants.ATTR_LAYOUT_MARGIN_BOTTOM
import com.android.SdkConstants.ATTR_LAYOUT_MARGIN_END
import com.android.SdkConstants.ATTR_LAYOUT_MARGIN_LEFT
import com.android.SdkConstants.ATTR_LAYOUT_MARGIN_RIGHT
import com.android.SdkConstants.ATTR_LAYOUT_MARGIN_START
import com.android.SdkConstants.ATTR_LAYOUT_MARGIN_TOP
import com.android.SdkConstants.ATTR_LAYOUT_WIDTH
import com.android.SdkConstants.ATTR_TEXT
import com.android.SdkConstants.ATTR_VISIBLE
import com.android.SdkConstants.VALUE_MATCH_PARENT
import com.android.SdkConstants.VALUE_TOP
import com.android.SdkConstants.VALUE_WRAP_CONTENT
import com.android.tools.adtui.ptable2.PTableColumn
import com.android.tools.adtui.ptable2.PTableGroupItem
import com.android.tools.adtui.ptable2.PTableItem
import com.android.tools.idea.common.property2.api.FilteredPTableModel
import com.android.tools.idea.common.property2.api.GroupSpec
import com.android.tools.idea.common.property2.impl.model.util.TestNewPropertyItem
import com.android.tools.idea.common.property2.impl.model.util.TestPTableModelUpdateListener
import com.android.tools.idea.common.property2.impl.model.util.TestPropertyItem
import com.android.tools.idea.common.property2.impl.model.util.TestPropertyModel
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val MARGIN_GROUP_NAME = "margin"

class FilteredPTableModelImplTest {
  private var model: TestPropertyModel? = null
  private var propHeight: TestPropertyItem? = null
  private var propWidth: TestPropertyItem? = null
  private var propGravity: TestPropertyItem? = null
  private var propText: TestPropertyItem? = null
  private var propVisible: TestPropertyItem? = null
  private var propMarginBottom: TestPropertyItem? = null
  private var propMarginEnd: TestPropertyItem? = null
  private var propMarginLeft: TestPropertyItem? = null
  private var propMarginRight: TestPropertyItem? = null
  private var propMarginStart: TestPropertyItem? = null
  private var propMarginTop: TestPropertyItem? = null
  private var propMargin: TestPropertyItem? = null
  private var alternateSortOrder: Comparator<PTableItem>? = null


  @Before
  fun init() {
    propHeight = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_HEIGHT, VALUE_WRAP_CONTENT)
    propWidth = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_WIDTH, VALUE_WRAP_CONTENT)
    propGravity = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_GRAVITY)
    propText = TestPropertyItem(ANDROID_URI, ATTR_TEXT, "Hello")
    propVisible = TestPropertyItem(ANDROID_URI, ATTR_VISIBLE)
    propMarginBottom = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_MARGIN_BOTTOM)
    propMarginEnd = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_MARGIN_END)
    propMarginLeft = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_MARGIN_LEFT)
    propMarginRight = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_MARGIN_RIGHT)
    propMarginStart = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_MARGIN_START)
    propMarginTop = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_MARGIN_TOP)
    propMargin = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_MARGIN)

    model = TestPropertyModel()
    model!!.add(propHeight!!)
    model!!.add(propWidth!!)
    model!!.add(propGravity!!)
    model!!.add(propText!!)
    model!!.add(propVisible!!)
    model!!.add(propMarginBottom!!)
    model!!.add(propMarginEnd!!)
    model!!.add(propMarginLeft!!)
    model!!.add(propMarginRight!!)
    model!!.add(propMarginStart!!)
    model!!.add(propMarginTop!!)
    model!!.add(propMargin!!)
    alternateSortOrder = Comparator
      .comparingInt<PTableItem>{ it.name.length }
      .thenComparing(PTableItem::name)
  }

  @After
  fun cleanUp() {
    model = null

    propHeight = null
    propWidth = null
    propGravity = null
    propText = null
    propVisible = null
    propMarginBottom = null
    propMarginEnd = null
    propMarginLeft = null
    propMarginRight = null
    propMarginStart = null
    propMarginTop = null
    propMargin = null
  }

  @Test
  fun testFilteredContent() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, keepNewAfterFlyAway = false)
    assertThat(tableModel.items.map{ it.name }).containsExactly(ATTR_LAYOUT_HEIGHT, ATTR_LAYOUT_WIDTH, ATTR_TEXT).inOrder()
  }

  @Test
  fun testAddExistingProperty() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, keepNewAfterFlyAway = false)
    val listener = TestPTableModelUpdateListener()
    val property = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_WIDTH, VALUE_MATCH_PARENT)
    tableModel.addListener(listener)
    tableModel.addNewItem(property)
    assertThat(tableModel.items.map { it.name })
      .containsExactly(ATTR_LAYOUT_HEIGHT, ATTR_LAYOUT_WIDTH, ATTR_TEXT).inOrder()
    assertThat(listener.updateCount).isEqualTo(0)
  }

  @Test
  fun testAddExistingPropertyAlternateOrder() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, alternateSortOrder!!, keepNewAfterFlyAway = false)
    val listener = TestPTableModelUpdateListener()
    val property = TestPropertyItem(ANDROID_URI, ATTR_LAYOUT_WIDTH, VALUE_MATCH_PARENT)
    tableModel.addListener(listener)
    tableModel.addNewItem(property)
    assertThat(tableModel.items.map { it.name })
      .containsExactly(ATTR_TEXT, ATTR_LAYOUT_WIDTH, ATTR_LAYOUT_HEIGHT).inOrder()
    assertThat(listener.updateCount).isEqualTo(0)
  }

  @Test
  fun testAddNonExistingProperty() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, alternateSortOrder!!, keepNewAfterFlyAway = false)
    val listener = TestPTableModelUpdateListener()
    val property = TestPropertyItem(ANDROID_URI, ATTR_FONT_FAMILY, "Sans")
    tableModel.editedItem = propHeight
    tableModel.addListener(listener)
    tableModel.addNewItem(property)
    assertThat(tableModel.items.map { it.name })
      .containsExactly(ATTR_TEXT, ATTR_FONT_FAMILY, ATTR_LAYOUT_WIDTH, ATTR_LAYOUT_HEIGHT).inOrder()
    assertThat(listener.updateCount).isEqualTo(1)
    assertThat(listener.nextEditedItem).isEqualTo(propHeight)
  }

  @Test
  fun testAddNewProperty() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, alternateSortOrder!!, keepNewAfterFlyAway = false)
    val listener = TestPTableModelUpdateListener()
    val property = TestNewPropertyItem()
    tableModel.editedItem = propHeight
    tableModel.addListener(listener)
    tableModel.addNewItem(property)
    assertThat(tableModel.items.map { it.name })
      .containsExactly(ATTR_TEXT, ATTR_LAYOUT_WIDTH, ATTR_LAYOUT_HEIGHT, "").inOrder()
    assertThat(listener.updateCount).isEqualTo(1)
    assertThat(listener.nextEditedItem).isEqualTo(property)
  }

  @Test
  fun testIsCellEditable() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, alternateSortOrder!!, keepNewAfterFlyAway = false)
    val property = TestNewPropertyItem()
    assertThat(tableModel.isCellEditable(propWidth!!, PTableColumn.NAME)).isFalse()
    assertThat(tableModel.isCellEditable(propWidth!!, PTableColumn.VALUE)).isTrue()
    assertThat(tableModel.isCellEditable(property, PTableColumn.NAME)).isTrue()
    assertThat(tableModel.isCellEditable(property, PTableColumn.VALUE)).isFalse()
    property.delegate = propGravity
    assertThat(tableModel.isCellEditable(property, PTableColumn.NAME)).isTrue()
    assertThat(tableModel.isCellEditable(property, PTableColumn.VALUE)).isTrue()
  }

  @Test
  fun testAcceptMoveToNextEditor() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, alternateSortOrder!!, keepNewAfterFlyAway = false)
    val property = TestNewPropertyItem()
    assertThat(tableModel.acceptMoveToNextEditor(propWidth!!, PTableColumn.NAME)).isTrue()
    assertThat(tableModel.acceptMoveToNextEditor(propWidth!!, PTableColumn.VALUE)).isTrue()
    assertThat(tableModel.acceptMoveToNextEditor(property, PTableColumn.NAME)).isTrue()
    assertThat(tableModel.acceptMoveToNextEditor(property, PTableColumn.VALUE)).isTrue()
    property.delegate = propGravity
    assertThat(tableModel.acceptMoveToNextEditor(property, PTableColumn.NAME)).isTrue()
    assertThat(tableModel.acceptMoveToNextEditor(property, PTableColumn.VALUE)).isFalse()
  }

  @Test
  fun testRefreshWhenHeightIsRemoved() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, alternateSortOrder!!, keepNewAfterFlyAway = false)
    val listener = TestPTableModelUpdateListener()
    tableModel.addListener(listener)
    tableModel.editedItem = propWidth

    propHeight!!.value = ""
    tableModel.refresh()
    assertThat(tableModel.items.map { it.name })
      .containsExactly(ATTR_TEXT, ATTR_LAYOUT_WIDTH).inOrder()
    assertThat(listener.updateCount).isEqualTo(1)
    assertThat(listener.nextEditedItem).isEqualTo(propWidth)
  }

  @Test
  fun testRefreshWhenWidthIsEditedAndRemoved() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, alternateSortOrder!!, keepNewAfterFlyAway = false)
    val listener = TestPTableModelUpdateListener()
    tableModel.addListener(listener)
    tableModel.editedItem = propWidth

    propWidth!!.value = ""
    tableModel.refresh()
    assertThat(tableModel.items.map { it.name })
      .containsExactly(ATTR_TEXT, ATTR_LAYOUT_HEIGHT).inOrder()
    assertThat(listener.updateCount).isEqualTo(1)
    assertThat(listener.nextEditedItem?.name).isEqualTo(ATTR_LAYOUT_HEIGHT)
  }

  @Test
  fun testRefreshWhenGravityIsAssigned() {
    val tableModel = FilteredPTableModel.create(model!!, { !it.value.isNullOrEmpty() }, alternateSortOrder!!, keepNewAfterFlyAway = false)
    val listener = TestPTableModelUpdateListener()
    tableModel.addListener(listener)
    tableModel.editedItem = propText

    propGravity!!.value = VALUE_TOP
    tableModel.refresh()
    assertThat(tableModel.items.map { it.name })
      .containsExactly(ATTR_TEXT, ATTR_LAYOUT_WIDTH, ATTR_LAYOUT_HEIGHT, ATTR_LAYOUT_GRAVITY).inOrder()
    assertThat(listener.updateCount).isEqualTo(1)
    assertThat(listener.nextEditedItem).isEqualTo(propText)
  }

  @Test
  fun testSortedGroup() {
    val tableModel = FilteredPTableModel.create(model!!, { true }, alternateSortOrder!!, listOf(MarginGroup()), false)
    val items = tableModel.items
    assertThat(items.map { it.name })
      .containsExactly(ATTR_TEXT, MARGIN_GROUP_NAME, ATTR_VISIBLE, ATTR_LAYOUT_WIDTH, ATTR_LAYOUT_HEIGHT, ATTR_LAYOUT_GRAVITY)
      .inOrder()
    val group = items[1] as PTableGroupItem
    assertThat(group.children.map { it.name })
      .containsExactly(ATTR_LAYOUT_MARGIN, ATTR_LAYOUT_MARGIN_END, ATTR_LAYOUT_MARGIN_TOP, ATTR_LAYOUT_MARGIN_LEFT,
                       ATTR_LAYOUT_MARGIN_RIGHT, ATTR_LAYOUT_MARGIN_START, ATTR_LAYOUT_MARGIN_BOTTOM).inOrder()
  }

  private inner class MarginGroup: GroupSpec<TestPropertyItem> {
    override val name = "margin"

    override val value: String?
      get() = "[${part(propMargin)}, ${part(propMarginLeft, propMarginStart)}, " +
              "${part(propMarginTop)}, ${part(propMarginRight, propMarginEnd)}, ${part(propMarginBottom)}]"

    override val itemFilter: (TestPropertyItem) -> Boolean
      get() = { it == propMargin || it == propMarginLeft || it == propMarginRight || it == propMarginStart ||
                it == propMarginEnd || it == propMarginTop || it == propMarginBottom }

    override val comparator: Comparator<PTableItem>
      get() = alternateSortOrder!!

    private fun part(property: TestPropertyItem?, override: TestPropertyItem? = null): String {
      return override?.value ?: property?.value ?: "?"
    }
  }
}
