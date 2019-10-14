/*
 * Copyright (C) 2019 The Android opening Source Project
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
package com.android.tools.idea.lang.proguardR8.parser

import com.android.tools.idea.lang.AndroidParsingTestCase
import com.android.tools.idea.lang.proguardR8.ProguardR8FileType
import com.android.tools.idea.lang.proguardR8.ProguardR8Language
import com.android.tools.idea.lang.proguardR8.ProguardR8PairedBraceMatcher
import com.intellij.lang.LanguageBraceMatching

class ProguardR8ParserTest : AndroidParsingTestCase(ProguardR8FileType.INSTANCE.defaultExtension, ProguardR8ParserDefinition()) {

  override fun setUp() {
    super.setUp()
    addExplicitExtension(LanguageBraceMatching.INSTANCE, ProguardR8Language.INSTANCE, ProguardR8PairedBraceMatcher())
  }

  fun testParse() {
    assertEquals(
      """
      FILE
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-printmapping')
          ProguardR8FlagArgumentImpl(FLAG_ARGUMENT)
            PsiElement(FILE_NAME)('out.map')
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keep')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassModifierImpl(CLASS_MODIFIER)
              PsiElement(public)('public')
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(asterisk)('*')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8FieldsSpecificationImpl(FIELDS_SPECIFICATION)
                ProguardR8FieldImpl(FIELD)
                  ProguardR8ModifierImpl(MODIFIER)
                    PsiElement(public)('public')
                  ProguardR8ModifierImpl(MODIFIER)
                    PsiElement(protected)('protected')
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(asterisk)('*')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-keepparameternames')
          """.trimIndent(),
      toParseTreeText(
        """
          -printmapping out.map
          -keep public class * {
            public protected *;
          }
          -keepparameternames
        """.trimIndent())
    )

    // few flags in the same line
    assertEquals(
      """
      FILE
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-printmapping')
          ProguardR8FlagArgumentImpl(FLAG_ARGUMENT)
            PsiElement(FILE_NAME)('out.map')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-android')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-dontpreverify')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-repackageclasses')
      """.trimIndent(),
      toParseTreeText(
        """
          -printmapping out.map -android -dontpreverify -repackageclasses
        """.trimIndent())
    )
  }

  fun testParseMethodSpecification() {
    assertEquals(
      """
      FILE
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keep')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassModifierImpl(CLASS_MODIFIER)
              PsiElement(public)('public')
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(asterisk)('*')
            PsiElement(extends)('extends')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('android')
                PsiElement(dot)('.')
                PsiElement(JAVA_IDENTIFIER)('view')
                PsiElement(dot)('.')
                PsiElement(JAVA_IDENTIFIER)('View')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8ModifierImpl(MODIFIER)
                  PsiElement(public)('public')
                PsiElement(<init>)('<init>')
                ProguardR8ParametersImpl(PARAMETERS)
                  PsiElement(left parenthesis)('(')
                  ProguardR8TypeListImpl(TYPE_LIST)
                    ProguardR8TypeImpl(TYPE)
                      ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                        PsiElement(JAVA_IDENTIFIER)('android')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('content')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('Context')
                  PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8ModifierImpl(MODIFIER)
                  PsiElement(public)('public')
                PsiElement(<init>)('<init>')
                ProguardR8ParametersImpl(PARAMETERS)
                  PsiElement(left parenthesis)('(')
                  ProguardR8TypeListImpl(TYPE_LIST)
                    ProguardR8TypeImpl(TYPE)
                      ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                        PsiElement(JAVA_IDENTIFIER)('android')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('content')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('Context')
                    PsiElement(comma)(',')
                    ProguardR8TypeImpl(TYPE)
                      ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                        PsiElement(JAVA_IDENTIFIER)('android')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('util')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('AttributeSet')
                    PsiElement(comma)(',')
                    ProguardR8TypeImpl(TYPE)
                      ProguardR8JavaPrimitiveImpl(JAVA_PRIMITIVE)
                        PsiElement(int)('int')
                  PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8MethodImpl(METHOD)
                  ProguardR8ModifierImpl(MODIFIER)
                    PsiElement(public)('public')
                  ProguardR8TypeImpl(TYPE)
                    ProguardR8JavaPrimitiveImpl(JAVA_PRIMITIVE)
                      PsiElement(void)('void')
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(JAVA_IDENTIFIER_WITH_WILDCARDS)('set*')
                  ProguardR8ParametersImpl(PARAMETERS)
                    PsiElement(left parenthesis)('(')
                    PsiElement(...)('...')
                    PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
          """.trimIndent(),
      toParseTreeText(
        """
          -keep public class * extends android.view.View {
            public <init>(android.content.Context);
            public <init>(android.content.Context, android.util.AttributeSet, int);
            public void set*(...);
          }
        """.trimIndent())
    )
  }

  fun testParseKeepOptionWithModifier() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keepclasseswithmembers')
            PsiElement(comma)(',')
            ProguardR8KeepOptionModifierImpl(KEEP_OPTION_MODIFIER)
              PsiElement(allowobfuscation)('allowobfuscation')
            PsiElement(comma)(',')
            ProguardR8KeepOptionModifierImpl(KEEP_OPTION_MODIFIER)
              PsiElement(includedescriptorclasses)('includedescriptorclasses')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(asterisk)('*')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8FieldsSpecificationImpl(FIELDS_SPECIFICATION)
                  ProguardR8AnnotationNameImpl(ANNOTATION_NAME)
                    PsiElement(@)('@')
                    ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                      PsiElement(JAVA_IDENTIFIER)('com')
                      PsiElement(dot)('.')
                      PsiElement(JAVA_IDENTIFIER)('google')
                      PsiElement(dot)('.')
                      PsiElement(JAVA_IDENTIFIER)('gson')
                      PsiElement(dot)('.')
                      PsiElement(JAVA_IDENTIFIER)('annotations')
                      PsiElement(dot)('.')
                      PsiElement(JAVA_IDENTIFIER)('SerializedName')
                  PsiElement(<fields>)('<fields>')
              PsiElement(semicolon)(';')
              PsiElement(closing brace)('}')
          """.trimIndent(),
      toParseTreeText(
        """
          -keepclasseswithmembers,allowobfuscation,includedescriptorclasses class * {
            @com.google.gson.annotations.SerializedName <fields>;
          }
        """.trimIndent())
    )
  }

  fun testFileNamesAndFileFilters() {
    assertEquals(
      """
      FILE
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-injars')
          ProguardR8FlagArgumentImpl(FLAG_ARGUMENT)
            PsiElement(FILE_NAME)('in.jar')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-outjars')
          ProguardR8FlagArgumentImpl(FLAG_ARGUMENT)
            PsiElement(FILE_NAME)('out.jar')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-libraryjars')
          ProguardR8FlagArgumentImpl(FLAG_ARGUMENT)
            PsiElement(FILE_NAME)('<java.home>/jmods/java.base.jmod')
            PsiElement(left parenthesis)('(')
            ProguardR8FileFilterImpl(FILE_FILTER)
              PsiElement(!)('!')
              PsiElement(FILE_NAME)('**.jar')
              PsiElement(semicolon)(';')
              PsiElement(!)('!')
              PsiElement(FILE_NAME)('module-info.class')
            PsiElement(right parenthesis)(')')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-libraryjars')
          ProguardR8FlagArgumentImpl(FLAG_ARGUMENT)
            PsiElement(FILE_NAME)('<java.home>/jmods/java.desktop.jmod')
            PsiElement(left parenthesis)('(')
            ProguardR8FileFilterImpl(FILE_FILTER)
              PsiElement(!)('!')
              PsiElement(FILE_NAME)('**.jar')
              PsiElement(semicolon)(';')
              PsiElement(!)('!')
              PsiElement(FILE_NAME)('module-info.class')
            PsiElement(right parenthesis)(')')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-printseeds')
    """.trimIndent(),
      toParseTreeText(
        """
        -injars      in.jar
        -outjars     out.jar
        -libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
        -libraryjars <java.home>/jmods/java.desktop.jmod(!**.jar;!module-info.class)
        -printseeds
      """.trimIndent()
      ))
  }

  fun testFieldSpecification() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-assumenoexternalsideeffects')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER_WITH_WILDCARDS)('**java')
                  PsiElement(dot)('.')
                  PsiElement(JAVA_IDENTIFIER)('lang')
                  PsiElement(dot)('.')
                  PsiElement(JAVA_IDENTIFIER)('StringBuilder')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8FieldsSpecificationImpl(FIELDS_SPECIFICATION)
                  ProguardR8FieldImpl(FIELD)
                    ProguardR8ModifierImpl(MODIFIER)
                      PsiElement(static)('static')
                    ProguardR8TypeImpl(TYPE)
                      ProguardR8AnyTypeImpl(ANY_TYPE)
                        PsiElement(***)('***')
                    ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                      PsiElement(JAVA_IDENTIFIER)('fieldName')
              PsiElement(semicolon)(';')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                  ProguardR8ModifierImpl(MODIFIER)
                    PsiElement(public)('public')
                  PsiElement(<methods>)('<methods>')
              PsiElement(semicolon)(';')
              PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
          -assumenoexternalsideeffects class **java.lang.StringBuilder {
            static *** fieldName;
            public <methods>;
          }
        """.trimIndent()
      )
    )
  }

  fun testKeepOptionModifier() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keepclasseswithmembernames')
            PsiElement(comma)(',')
            ProguardR8KeepOptionModifierImpl(KEEP_OPTION_MODIFIER)
              PsiElement(includedescriptorclasses)('includedescriptorclasses')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(asterisk)('*')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                  ProguardR8ModifierImpl(MODIFIER)
                    PsiElement(native)('native')
                  PsiElement(<methods>)('<methods>')
              PsiElement(semicolon)(';')
              PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
          -keepclasseswithmembernames,includedescriptorclasses class * {
              native <methods>;
          }
        """.trimIndent()
      )
    )
  }

  fun testRecoveryClassSpecification() {
    assertEquals(
      """
      FILE
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-keepclasseswithmembernames')
          ProguardR8FlagArgumentImpl(FLAG_ARGUMENT)
            PsiElement(FILE_NAME)('error')
        PsiErrorElement:colon, comma, left parenthesis or semicolon expected, got 'error'
          PsiElement(FILE_NAME)('error')
        PsiElement(DUMMY_BLOCK)
          PsiElement(opening brace)('{')
          PsiElement(DUMMY_BLOCK)
            PsiElement(JAVA_IDENTIFIER)('java')
            PsiElement(dot)('.')
            PsiElement(JAVA_IDENTIFIER)('lang')
            PsiElement(dot)('.')
            PsiElement(JAVA_IDENTIFIER)('StringBuilder')
            PsiElement(semicolon)(';')
            PsiElement(<methods>)('<methods>')
            PsiElement(semicolon)(';')
          PsiElement(closing brace)('}')
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keepclasseswithmembernames')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('MyClass')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8MethodImpl(METHOD)
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(JAVA_IDENTIFIER)('validOne')
                  ProguardR8ParametersImpl(PARAMETERS)
                    PsiElement(left parenthesis)('(')
                    ProguardR8TypeListImpl(TYPE_LIST)
                      <empty list>
                    PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
          -keepclasseswithmembernames error error {
            java.lang.StringBuilder;
            <methods>;
          }

          -keepclasseswithmembernames class MyClass {
            validOne();
          }
        """.trimIndent()
      )
    )
  }

  fun testRecoveryJavaRule() {
    assertEquals(
      """
      FILE
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keepclasseswithmembernames')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(asterisk)('*')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8FullyQualifiedNameConstructorImpl(FULLY_QUALIFIED_NAME_CONSTRUCTOR)
                  ProguardR8ConstructorNameImpl(CONSTRUCTOR_NAME)
                    ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                      PsiElement(JAVA_IDENTIFIER)('java')
                      PsiElement(dot)('.')
                      PsiElement(JAVA_IDENTIFIER)('lang')
                      PsiElement(dot)('.')
                      PsiElement(JAVA_IDENTIFIER)('StringBuilder')
                  PsiErrorElement:<class member name>, '[]', dot or left parenthesis expected, got ';'
                    <empty list>
            PsiElement(semicolon)(';')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                PsiElement(<methods>)('<methods>')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
          -keepclasseswithmembernames class * {
              java.lang.StringBuilder;
              <methods>;
          }
        """.trimIndent()
      )
    )
  }

  fun testRegularFlagBetweenFlagsWithClassSpecification() {
    assertEquals(
      """
      FILE
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keep')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassModifierImpl(CLASS_MODIFIER)
              PsiElement(public)('public')
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('com')
                PsiElement(dot)('.')
                PsiElement(JAVA_IDENTIFIER)('example')
                PsiElement(dot)('.')
                PsiElement(JAVA_IDENTIFIER)('MyApplication')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8ModifierImpl(MODIFIER)
                  PsiElement(public)('public')
                PsiElement(<methods>)('<methods>')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
        ProguardR8RuleImpl(RULE)
          PsiElement(FLAG)('-flag')
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keepclassmembers')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(asterisk)('*')
            PsiElement(implements)('implements')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('android')
                PsiElement(dot)('.')
                PsiElement(JAVA_IDENTIFIER)('os')
                PsiElement(dot)('.')
                PsiElement(JAVA_IDENTIFIER)('Parcelable')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8ModifierImpl(MODIFIER)
                  PsiElement(public)('public')
                PsiElement(<methods>)('<methods>')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
          -keep public class com.example.MyApplication {
              public <methods>;
          }
          
          -flag
          
          -keepclassmembers class * implements android.os.Parcelable {
              public <methods>;
          }
        """.trimIndent()
      )
    )
  }

  fun testAnyParametersSymbol() {
    assertEquals(
      """
      FILE
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keep')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('myClass')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8MethodImpl(METHOD)
                  ProguardR8TypeImpl(TYPE)
                    ProguardR8JavaPrimitiveImpl(JAVA_PRIMITIVE)
                      PsiElement(void)('void')
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(JAVA_IDENTIFIER)('anyType')
                  ProguardR8ParametersImpl(PARAMETERS)
                    PsiElement(left parenthesis)('(')
                    PsiElement(...)('...')
                    PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            void anyType(...);
          }
        """.trimIndent()
      )
    )
  }

  fun testAnyParametersSymbolInTypeList() {
    assertEquals(
      """
      FILE
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keep')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('myClass')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8MethodImpl(METHOD)
                  ProguardR8TypeImpl(TYPE)
                    ProguardR8JavaPrimitiveImpl(JAVA_PRIMITIVE)
                      PsiElement(void)('void')
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(JAVA_IDENTIFIER)('mixedTypes')
                  ProguardR8ParametersImpl(PARAMETERS)
                    PsiElement(left parenthesis)('(')
                    ProguardR8TypeListImpl(TYPE_LIST)
                      ProguardR8TypeImpl(TYPE)
                        ProguardR8JavaPrimitiveImpl(JAVA_PRIMITIVE)
                          PsiElement(int)('int')
                      PsiElement(comma)(',')
                      PsiElement(...)('...')
                    PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            void mixedTypes(int, ...);
          }
        """.trimIndent()
      )
    )
  }

  fun testAnyParametersSymbolInTypeListAtWrongPlace() {
    assertEquals(
      """
      FILE
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keep')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('myClass')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8MethodImpl(METHOD)
                  ProguardR8TypeImpl(TYPE)
                    ProguardR8JavaPrimitiveImpl(JAVA_PRIMITIVE)
                      PsiElement(void)('void')
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(JAVA_IDENTIFIER)('mixedTypes')
                  ProguardR8ParametersImpl(PARAMETERS)
                    PsiElement(left parenthesis)('(')
                    ProguardR8TypeListImpl(TYPE_LIST)
                      ProguardR8TypeImpl(TYPE)
                        ProguardR8JavaPrimitiveImpl(JAVA_PRIMITIVE)
                          PsiElement(int)('int')
                      PsiElement(comma)(',')
                      PsiElement(...)('...')
                      PsiErrorElement:',' unexpected
                        PsiElement(comma)(',')
                      PsiElement(int)('int')
                    PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            void mixedTypes(int, ..., int);
          }
        """.trimIndent()
      )
    )
  }

  fun testTypeListRecovery() {
    assertEquals(
      """
      FILE
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keep')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('myClass')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8MethodImpl(METHOD)
                  ProguardR8TypeImpl(TYPE)
                    ProguardR8JavaPrimitiveImpl(JAVA_PRIMITIVE)
                      PsiElement(void)('void')
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(JAVA_IDENTIFIER)('badTypeList')
                  ProguardR8ParametersImpl(PARAMETERS)
                    PsiElement(left parenthesis)('(')
                    ProguardR8TypeListImpl(TYPE_LIST)
                      PsiErrorElement:'...' or <type> expected, got '2'
                        PsiElement(BAD_CHARACTER)('2')
                      PsiElement(comma)(',')
                      PsiElement(int)('int')
                    PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            void badTypeList(2, int);
          }
        """.trimIndent()
      )
    )
  }

  fun testClassMemberWithoutType() {
    assertEquals(
      """
      FILE
        ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
          PsiElement(FLAG)('-keep')
          ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
            ProguardR8ClassTypeImpl(CLASS_TYPE)
              PsiElement(class)('class')
            ProguardR8ClassNameImpl(CLASS_NAME)
              ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                PsiElement(JAVA_IDENTIFIER)('myClass')
          ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
            PsiElement(opening brace)('{')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8FieldsSpecificationImpl(FIELDS_SPECIFICATION)
                ProguardR8FieldImpl(FIELD)
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(JAVA_IDENTIFIER)('field')
            PsiElement(semicolon)(';')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8MethodImpl(METHOD)
                  ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                    PsiElement(JAVA_IDENTIFIER)('method')
                  ProguardR8ParametersImpl(PARAMETERS)
                    PsiElement(left parenthesis)('(')
                    ProguardR8TypeListImpl(TYPE_LIST)
                      <empty list>
                    PsiElement(right parenthesis)(')')
            PsiElement(semicolon)(';')
            ProguardR8JavaRuleImpl(JAVA_RULE)
              ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                ProguardR8FullyQualifiedNameConstructorImpl(FULLY_QUALIFIED_NAME_CONSTRUCTOR)
                  ProguardR8ConstructorNameImpl(CONSTRUCTOR_NAME)
                    ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                      PsiElement(JAVA_IDENTIFIER)('not')
                      PsiElement(dot)('.')
                      PsiElement(JAVA_IDENTIFIER)('classMember')
                  PsiErrorElement:<class member name>, '[]', dot or left parenthesis expected, got ';'
                    <empty list>
            PsiElement(semicolon)(';')
            PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            field;
            method();
            not.classMember;
          }
        """.trimIndent()
      )
    )
  }

  fun testFullyQualifiedNameConstructor() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER)('p1')
                  PsiElement(dot)('.')
                  PsiElement(JAVA_IDENTIFIER)('p2')
                  PsiElement(dot)('.')
                  PsiElement(JAVA_IDENTIFIER)('myClass')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                  ProguardR8FullyQualifiedNameConstructorImpl(FULLY_QUALIFIED_NAME_CONSTRUCTOR)
                    ProguardR8ConstructorNameImpl(CONSTRUCTOR_NAME)
                      ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                        PsiElement(JAVA_IDENTIFIER)('p1')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('p2')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('myClass')
                    ProguardR8ParametersImpl(PARAMETERS)
                      PsiElement(left parenthesis)('(')
                      ProguardR8TypeListImpl(TYPE_LIST)
                        <empty list>
                      PsiElement(right parenthesis)(')')
                PsiErrorElement:'}' unexpected
                  PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class p1.p2.myClass {
            p1.p2.myClass()
          }
        """.trimIndent()
      )
    )
  }

  fun testClassPathWithKeyWord() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(class)('class')
                  PsiElement(dot)('.')
                  PsiElement(interface)('interface')
                  PsiElement(dot)('.')
                  PsiElement(JAVA_IDENTIFIER)('myClass')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                  ProguardR8FullyQualifiedNameConstructorImpl(FULLY_QUALIFIED_NAME_CONSTRUCTOR)
                    ProguardR8ConstructorNameImpl(CONSTRUCTOR_NAME)
                      ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                        PsiElement(void)('void')
                        PsiElement(dot)('.')
                        PsiElement(int)('int')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('myClass')
                    ProguardR8ParametersImpl(PARAMETERS)
                      PsiElement(left parenthesis)('(')
                      ProguardR8TypeListImpl(TYPE_LIST)
                        <empty list>
                      PsiElement(right parenthesis)(')')
              PsiElement(semicolon)(';')
              PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class class.interface.myClass {
            void.int.myClass();
          }
        """.trimIndent()
      )
    )
  }

  fun testDontParseModifierAsType() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER)('myClass')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                  ProguardR8FullyQualifiedNameConstructorImpl(FULLY_QUALIFIED_NAME_CONSTRUCTOR)
                    ProguardR8ModifierImpl(MODIFIER)
                      PsiElement(strictfp)('strictfp')
                    ProguardR8ConstructorNameImpl(CONSTRUCTOR_NAME)
                      ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                        PsiElement(JAVA_IDENTIFIER)('my')
                    PsiErrorElement:<class member name>, '[]', dot or left parenthesis expected, got '}'
                      <empty list>
                PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            strictfp my
          }
        """.trimIndent()
      )
    )

    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER)('myClass')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8FieldsSpecificationImpl(FIELDS_SPECIFICATION)
                  ProguardR8FieldImpl(FIELD)
                    ProguardR8ModifierImpl(MODIFIER)
                      PsiElement(public)('public')
                    ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                      PsiElement(JAVA_IDENTIFIER)('my')
                PsiErrorElement:<class member name>, '[]' or dot expected, got '}'
                  PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            public my
          }
        """.trimIndent()
      )
    )


    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER)('myClass')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8FieldsSpecificationImpl(FIELDS_SPECIFICATION)
                  ProguardR8FieldImpl(FIELD)
                    ProguardR8ModifierImpl(MODIFIER)
                      PsiElement(volatile)('volatile')
                    ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                      PsiElement(JAVA_IDENTIFIER)('my')
                PsiErrorElement:<class member name>, '[]' or dot expected, got '}'
                  PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            volatile my
          }
        """.trimIndent()
      )
    )
  }

  fun testParseModifierAsPartOfQualifiedName() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER)('myClass')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                  ProguardR8FullyQualifiedNameConstructorImpl(FULLY_QUALIFIED_NAME_CONSTRUCTOR)
                    ProguardR8ModifierImpl(MODIFIER)
                      PsiElement(private)('private')
                    ProguardR8ConstructorNameImpl(CONSTRUCTOR_NAME)
                      ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                        PsiElement(private)('private')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('not')
                        PsiElement(dot)('.')
                        PsiElement(JAVA_IDENTIFIER)('modifier')
                    ProguardR8ParametersImpl(PARAMETERS)
                      PsiElement(left parenthesis)('(')
                      ProguardR8TypeListImpl(TYPE_LIST)
                        <empty list>
                      PsiElement(right parenthesis)(')')
              PsiElement(semicolon)(';')
              PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class myClass {
            private private.not.modifier();
          }
        """.trimIndent()
      )
    )
  }

  fun testFileNameAfterAt() {
    assertEquals(
      """
        FILE
          ProguardR8IncludeFileImpl(INCLUDE_FILE)
            PsiElement(@)('@')
            PsiElement(FILE_NAME)('keep-rules.txt')
          ProguardR8RuleImpl(RULE)
            PsiElement(FLAG)('-secondrule')
      """.trimIndent(),
      toParseTreeText(
        """
        @keep-rules.txt
        
        -secondrule
        """.trimIndent()
      )
    )
  }

  fun testParsingSingleAsterisk() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8AnnotationNameImpl(ANNOTATION_NAME)
                PsiElement(@)('@')
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER_WITH_WILDCARDS)('**')
                  PsiElement(dot)('.')
                  PsiElement(JAVA_IDENTIFIER)('RunWith')
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(asterisk)('*')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8FieldsSpecificationImpl(FIELDS_SPECIFICATION)
                  ProguardR8FieldImpl(FIELD)
                    ProguardR8ClassMemberNameImpl(CLASS_MEMBER_NAME)
                      PsiElement(asterisk)('*')
              PsiElement(semicolon)(';')
              PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep @**.RunWith class * { *; }
        """.trimIndent()
      )
    )
  }

  fun testAnnotationInSuperClass() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER_WITH_WILDCARDS)('**')
              PsiElement(implements)('implements')
              ProguardR8AnnotationNameImpl(ANNOTATION_NAME)
                PsiElement(@)('@')
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER)('shaking3')
                  PsiElement(dot)('.')
                  PsiElement(JAVA_IDENTIFIER)('SubtypeUsedByReflection')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER_WITH_WILDCARDS)('**')
            ProguardR8ClassSpecificationBodyImpl(CLASS_SPECIFICATION_BODY)
              PsiElement(opening brace)('{')
              ProguardR8JavaRuleImpl(JAVA_RULE)
                ProguardR8MethodSpecificationImpl(METHOD_SPECIFICATION)
                  PsiElement(<init>)('<init>')
                  ProguardR8ParametersImpl(PARAMETERS)
                    PsiElement(left parenthesis)('(')
                    PsiElement(...)('...')
                    PsiElement(right parenthesis)(')')
              PsiElement(semicolon)(';')
              PsiElement(closing brace)('}')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class ** implements @shaking3.SubtypeUsedByReflection ** {
          <init>(...);
        }
        """.trimIndent()
      )
    )
  }

  fun testBackReferenceWildcard() {
    assertEquals(
      """
        FILE
          ProguardR8RuleWithClassSpecificationImpl(RULE_WITH_CLASS_SPECIFICATION)
            PsiElement(FLAG)('-keep')
            ProguardR8ClassSpecificationHeaderImpl(CLASS_SPECIFICATION_HEADER)
              ProguardR8ClassTypeImpl(CLASS_TYPE)
                PsiElement(class)('class')
              ProguardR8ClassNameImpl(CLASS_NAME)
                ProguardR8QualifiedNameImpl(QUALIFIED_NAME)
                  PsiElement(JAVA_IDENTIFIER_WITH_WILDCARDS)('**${'$'}D<2>')
      """.trimIndent(),
      toParseTreeText(
        """
        -keep class **${'$'}D<2>
        """.trimIndent()
      )
    )
  }

}
