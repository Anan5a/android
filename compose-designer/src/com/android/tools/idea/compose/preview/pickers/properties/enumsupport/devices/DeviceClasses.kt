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
package com.android.tools.idea.compose.preview.pickers.properties.enumsupport.devices

import com.android.tools.idea.avdmanager.AvdScreenData
import com.android.tools.idea.compose.preview.Preview.DeviceSpec.DEFAULT_DPI
import com.android.tools.idea.compose.preview.pickers.properties.DeviceConfig
import com.android.tools.idea.compose.preview.pickers.properties.DimUnit
import com.android.tools.idea.compose.preview.pickers.properties.Shape
import com.android.tools.idea.compose.preview.pickers.properties.enumsupport.PsiEnumValue
import com.android.tools.idea.compose.preview.pickers.properties.utils.DEVICE_BY_ID_PREFIX
import com.android.tools.idea.configurations.DEVICE_CLASS_DESKTOP_TOOLTIP
import com.android.tools.idea.configurations.DEVICE_CLASS_FOLDABLE_TOOLTIP
import com.android.tools.idea.configurations.DEVICE_CLASS_PHONE_TOOLTIP
import com.android.tools.idea.configurations.DEVICE_CLASS_TABLET_TOOLTIP
import com.android.tools.property.panel.api.EnumValue
import com.google.wireless.android.sdk.stats.EditorPickerEvent.EditorPickerAction.PreviewPickerModification.PreviewPickerValue
import icons.StudioIcons
import javax.swing.Icon
import kotlin.math.round
import kotlin.math.sqrt

/**
 * Used for `Round Chin` devices. Or when DeviceConfig.shape == Shape.Chin
 */
internal const val CHIN_SIZE_PX_FOR_ROUND_CHIN = 30

/** Default device configuration for Phones */
internal val ReferencePhoneConfig = deviceConfigWithDpDimensions(width = 360, height = 640)

/** Default device configuration for Foldables */
internal val ReferenceFoldableConfig = deviceConfigWithDpDimensions(width = 673, height = 841)

/** Default device configuration for Tablets */
internal val ReferenceTabletConfig = deviceConfigWithDpDimensions(width = 1280, height = 800)

/** Default device configuration for Desktops */
internal val ReferenceDesktopConfig = deviceConfigWithDpDimensions(width = 1920, height = 1080)

/**
 * The different types of devices that'll be available on the picker 'Device' DropDown.
 */
internal enum class DeviceClass(val display: String, val icon: Icon? = null) {
  Canonical("Reference Devices", StudioIcons.Wizards.Modules.PHONE_TABLET),
  Phone("Phone", StudioIcons.LayoutEditor.Toolbar.DEVICE_PHONE),
  Tablet("Tablet", StudioIcons.LayoutEditor.Toolbar.DEVICE_TABLET),
  Wear("Wear", StudioIcons.LayoutEditor.Toolbar.DEVICE_WEAR),
  Tv("Tv", StudioIcons.LayoutEditor.Toolbar.DEVICE_TV),
  Auto("Auto", StudioIcons.LayoutEditor.Toolbar.DEVICE_AUTOMOTIVE),
  Generic("Generic Devices", StudioIcons.LayoutEditor.Toolbar.DEVICE_PHONE)
}

/**
 * Helper class to assist on building a List of [EnumValue].
 *
 * Typically, each method will take device parameters (eg: width, height) and use [DeviceConfig] to encode it to a common format.
 *
 * [build] returns the List of [EnumValue] ordered by [DeviceClass] and including a header for each of them.
 */
internal class DeviceEnumValueBuilder {
  private val deviceEnumValues = mapOf<DeviceClass, MutableList<PsiEnumValue>>(
    Pair(DeviceClass.Canonical, mutableListOf()),
    Pair(DeviceClass.Phone, mutableListOf()),
    Pair(DeviceClass.Tablet, mutableListOf()),
    Pair(DeviceClass.Wear, mutableListOf()),
    Pair(DeviceClass.Tv, mutableListOf()),
    Pair(DeviceClass.Auto, mutableListOf()),
    Pair(DeviceClass.Generic, mutableListOf())
  )

  private fun addCanonical(
    name: String,
    description: String?,
    immutableDeviceConfig: DeviceConfig,
    trackableValue: PreviewPickerValue
  ): DeviceEnumValueBuilder = apply {
    val deviceSpec = immutableDeviceConfig.deviceSpec()
    val enumValue = PsiEnumValue.withTooltip(deviceSpec, name, description, trackableValue)
    deviceEnumValues[DeviceClass.Canonical]?.add(enumValue)
  }

  private fun addDevicePx(
    overrideDisplayName: String? = null,
    type: DeviceClass,
    widthPx: Int,
    heightPx: Int,
    diagonalIn: Double
  ): DeviceEnumValueBuilder = apply {
    val dpi = kotlin.run {
      val dpiCalc = sqrt((1.0 * widthPx * widthPx) + (1.0 * heightPx * heightPx)) / diagonalIn
      round(dpiCalc * 100) / 100.0
    }
    val density = AvdScreenData.getScreenDensity(null, true, dpi, heightPx)
    val deviceSpec = DeviceConfig(width = widthPx, height = heightPx, dimUnit = DimUnit.px, dpi = density.dpiValue).deviceSpec()
    val display = overrideDisplayName ?: "${round(diagonalIn * 100) / 100}\" ${type.name} ${heightPx}p"
    val enumValue = PsiEnumValue.indented(deviceSpec, display, PreviewPickerValue.DEVICE_REF_NONE)
    deviceEnumValues[type]?.add(enumValue)
  }

  fun addWearDevice(
    isRound: Boolean,
    chinSizePx: Int,
    displayName: String
  ): DeviceEnumValueBuilder = apply {
    val density = AvdScreenData.getScreenDensity(null, false, 224.0, 300)
    val shape = if (isRound) Shape.Round else Shape.Normal
    val deviceSpec = DeviceConfig(
      width = 300,
      height = 300,
      dimUnit = DimUnit.px,
      dpi = density.dpiValue,
      shape = shape,
      chinSize = chinSizePx
    ).deviceSpec()
    val enumValue = PsiEnumValue.indented(deviceSpec, displayName, PreviewPickerValue.DEVICE_REF_NONE)
    deviceEnumValues[DeviceClass.Wear]?.add(enumValue)
  }

  fun addTvDevice(
    widthPx: Int,
    heightPx: Int,
    diagonalIn: Double
  ): DeviceEnumValueBuilder =
    addDevicePx(type = DeviceClass.Tv, widthPx = widthPx, heightPx = heightPx, diagonalIn = diagonalIn)

  fun addAutoDevice(
    widthPx: Int,
    heightPx: Int,
    diagonalIn: Double
  ): DeviceEnumValueBuilder =
    addDevicePx(type = DeviceClass.Auto, widthPx = widthPx, heightPx = heightPx, diagonalIn = diagonalIn)

  fun addPhoneById(
    displayName: String,
    id: String,
  ): DeviceEnumValueBuilder = addById(displayName, id, DeviceClass.Phone)

  fun addTabletById(
    displayName: String,
    id: String,
  ): DeviceEnumValueBuilder = addById(displayName, id, DeviceClass.Tablet)

  fun addGenericById(
    displayName: String,
    id: String,
  ): DeviceEnumValueBuilder = addById(displayName, id, DeviceClass.Generic)

  fun addById(
    displayName: String,
    id: String,
    type: DeviceClass
  ): DeviceEnumValueBuilder = apply {
    val enumValue = PsiEnumValue.indented("$DEVICE_BY_ID_PREFIX$id", displayName, PreviewPickerValue.DEVICE_REF_NONE)
    deviceEnumValues[type]?.add(enumValue)
  }

  /**
   * The returned [EnumValue]s is guaranteed to contain a set of pre-defined device options.
   *
   * Particularly for Canonical, Wear, TV and Auto, if any of these weren't populated with the builder.
   */
  fun includeDefaultsAndBuild(): List<EnumValue> {
    addDefaultsIfMissing()
    val enumValues = mutableListOf<EnumValue>()
    deviceEnumValues.keys.forEach { type ->
      val values = deviceEnumValues[type]
      if (values?.isNotEmpty() == true) {
        if (enumValues.isNotEmpty()) enumValues.add(EnumValue.SEPARATOR)
        enumValues.add(EnumValue.header(type.display, type.icon))
        values.forEach(enumValues::add)
      }
    }
    return enumValues
  }

  private fun addDefaultsIfMissing() {
    if (!deviceEnumValues.contains(DeviceClass.Canonical) || deviceEnumValues[DeviceClass.Canonical]?.isEmpty() == true) {
      addCanonical("Phone", DEVICE_CLASS_PHONE_TOOLTIP, ReferencePhoneConfig, PreviewPickerValue.DEVICE_REF_PHONE)
      addCanonical("Foldable", DEVICE_CLASS_FOLDABLE_TOOLTIP, ReferenceFoldableConfig, PreviewPickerValue.DEVICE_REF_FOLDABLE)
      addCanonical("Tablet", DEVICE_CLASS_TABLET_TOOLTIP, ReferenceTabletConfig, PreviewPickerValue.DEVICE_REF_TABLET)
      addCanonical("Desktop", DEVICE_CLASS_DESKTOP_TOOLTIP, ReferenceDesktopConfig, PreviewPickerValue.DEVICE_REF_DESKTOP)
    }
    if (!deviceEnumValues.contains(DeviceClass.Wear) || deviceEnumValues[DeviceClass.Wear]?.isEmpty() == true) {
      addWearDevice(isRound = false, chinSizePx = 0, displayName = "Square")
      addWearDevice(isRound = true, chinSizePx = 0, displayName = "Round")
      addWearDevice(isRound = true, chinSizePx = CHIN_SIZE_PX_FOR_ROUND_CHIN, displayName = "Round Chin")
    }
    if (!deviceEnumValues.contains(DeviceClass.Tv) || deviceEnumValues[DeviceClass.Tv]?.isEmpty() == true) {
      addTvDevice(widthPx = 3840, heightPx = 2160, diagonalIn = 55.0)
      addTvDevice(widthPx = 1920, heightPx = 1080, diagonalIn = 55.0)
      addTvDevice(widthPx = 1280, heightPx = 720, diagonalIn = 55.0)
    }
    if (!deviceEnumValues.contains(DeviceClass.Auto) || deviceEnumValues[DeviceClass.Auto]?.isEmpty() == true) {
      addAutoDevice(widthPx = 1024, heightPx = 768, diagonalIn = 8.4)
    }
  }
}

private fun deviceConfigWithDpDimensions(width: Int, height: Int) =
  DeviceConfig(
    width = width,
    height = height,
    dimUnit = DimUnit.dp,
    dpi = DEFAULT_DPI,
    shape = Shape.Normal
  )