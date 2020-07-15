/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.tools.idea.nav.safeargs.psi.java

import com.android.tools.idea.nav.safeargs.index.NavDestinationData
import com.android.tools.idea.nav.safeargs.psi.xml.findXmlTagById
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.xml.XmlFile
import org.jetbrains.android.augment.AndroidLightClassBase
import org.jetbrains.android.facet.AndroidFacet

/**
 * Inner class that is generated inside a Directions class, which helps build actions.
 *
 * See the docs on [LightDirectionsClass] which provide a concrete example of what this looks like.
 */
class LightActionBuilderClass(
  className: String,
  private val targetDestination: NavDestinationData,
  private val backingResourceFile: XmlFile?,
  facet: AndroidFacet,
  private val modulePackage: String,
  private val directionsClass: LightDirectionsClass
) : AndroidLightClassBase(PsiManager.getInstance(facet.module.project), setOf(PsiModifier.PUBLIC, PsiModifier.STATIC, PsiModifier.FINAL)) {
  private val NAV_DIRECTIONS_FQCN  = "androidx.navigation.NavDirections"
  private val name: String = className
  private val qualifiedName: String = "${directionsClass.qualifiedName}.$name"
  private val _constructors by lazy { computeConstructors() }
  private val _methods by lazy { computeMethods() }
  private val _fields by lazy { computeFields() }
  private val navDirectionsType by lazy { PsiType.getTypeByName(NAV_DIRECTIONS_FQCN, project, this.resolveScope) }
  private val navDirectionsClass by lazy { JavaPsiFacade.getInstance(project).findClass(NAV_DIRECTIONS_FQCN, this.resolveScope) }

  override fun getName() = name
  override fun getQualifiedName() = qualifiedName
  override fun getContainingFile() = directionsClass.containingFile
  override fun getContainingClass() = directionsClass
  override fun getParent() = directionsClass
  override fun isValid() = true
  override fun getNavigationElement() = directionsClass.navigationElement
  override fun getConstructors() = _constructors

  override fun getImplementsListTypes() = arrayOf(navDirectionsType)
  override fun getSuperTypes() = arrayOf(navDirectionsType)
  override fun getSuperClassType() = navDirectionsType
  override fun getSuperClass() = navDirectionsClass
  override fun getSupers() = navDirectionsClass?.let { arrayOf(it) } ?: emptyArray()

  override fun getMethods() = _methods
  override fun getAllMethods() = methods
  override fun findMethodsByName(name: String, checkBases: Boolean): Array<PsiMethod> {
    return allMethods.filter { method -> method.name == name }.toTypedArray()
  }

  private fun computeMethods(): Array<PsiMethod> {
    val thisType = PsiTypesUtil.getClassType(this)

    return targetDestination.arguments.flatMap { arg ->
      // Create a getter and setter per argument
      val argType = parsePsiType(modulePackage, arg.type, arg.defaultValue, this)
      val setter = createMethod(name = "set${arg.name.capitalize()}",
                                navigationElement = getFieldNavigationElementByName(arg.name),
                                returnType = annotateNullability(thisType))
        .addParameter(arg.name, argType)

      val getter = createMethod(name = "get${arg.name.capitalize()}",
                                navigationElement = getFieldNavigationElementByName(arg.name),
                                returnType = annotateNullability(argType, arg.nullable))

      listOf(setter, getter)
    }.toTypedArray()
  }

  private fun computeConstructors(): Array<PsiMethod> {
    val privateConstructor = createConstructor().apply {
      targetDestination.arguments.forEach { arg ->
        if (arg.defaultValue == null) {
          val argType = parsePsiType(modulePackage, arg.type, arg.defaultValue, this)
          this.addParameter(arg.name, argType)
        }
      }
      this.setModifiers(PsiModifier.PRIVATE)
    }

    return arrayOf(privateConstructor)
  }

  private fun computeFields(): Array<PsiField> {
    val targetDestinationTag = backingResourceFile?.findXmlTagById(targetDestination.id) ?: return emptyArray()
    return targetDestination.arguments
      .asSequence()
      .map { createField(it, modulePackage, targetDestinationTag) }
      .toList()
      .toTypedArray()
  }

  private fun getFieldNavigationElementByName(name: String): PsiElement? {
    return _fields.firstOrNull { it.name == name }?.navigationElement
  }
}