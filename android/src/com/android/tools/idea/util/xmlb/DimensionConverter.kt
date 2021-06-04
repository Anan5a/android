/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.android.tools.idea.util.xmlb

import com.intellij.util.xmlb.Converter
import java.awt.Dimension
import java.util.regex.Pattern

private val pattern: Pattern = Pattern.compile("(?<width>\\d+)x(?<height>\\d+)")

/**
 * A [Converter] for [Dimension]
 */
class DimensionConverter : Converter<Dimension>() {
  override fun toString(value: Dimension): String = "${value.width}x${value.height}"

  override fun fromString(value: String): Dimension? {
    val matcher = pattern.matcher(value)
    return if (matcher.matches()) Dimension(matcher.group("width").toInt(), matcher.group("height").toInt()) else null
  }
}