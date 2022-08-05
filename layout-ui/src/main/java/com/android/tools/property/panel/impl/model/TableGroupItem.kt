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
package com.android.tools.property.panel.impl.model

import com.android.tools.property.panel.api.GroupSpec
import com.android.tools.property.panel.api.PropertyItem
import com.android.tools.property.ptable.PTableGroupItem

class TableGroupItem<P: PropertyItem>(val group: GroupSpec<P>): PTableGroupItem {

  override val name: String
    get() = group.name

  override val value: String?
    get() = group.value

  override val children: MutableList<P> = mutableListOf()

  override fun hashCode(): Int {
    return group.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    return group.equals((other as? TableGroupItem<*>)?.group)
  }
}
