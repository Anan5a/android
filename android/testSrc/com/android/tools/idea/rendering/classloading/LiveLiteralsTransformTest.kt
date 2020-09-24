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
package com.android.tools.idea.rendering.classloading

import com.android.tools.idea.editors.literals.LiteralUsageReference
import com.android.tools.idea.run.util.StopWatch
import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassWriter
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.ClassRemapper
import org.jetbrains.org.objectweb.asm.commons.SimpleRemapper
import org.jetbrains.org.objectweb.asm.util.TraceClassVisitor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Duration

annotation class FileInfo(val file: String)
annotation class KeyInfo(val key: String, val offset: Int)
/**
 * Base interface for the test to log the constants.
 */
interface Receiver {
  fun receive(receiver: (Any?) -> Unit)
}

class TestClass : Receiver {
  @FileInfo(file = "Test.kt")
  object `LiveLiterals$TestClass` {
    private val a1 = "A1"
    private val a2 = "A2"
    private val a3 =  2
    private val a4 =  3f
    private val a5 =  null
    private val a6 = 0 // This is the default value for int so there is no initialization in the constructor

    @KeyInfo(key = "a1", offset = 0)
    fun a1(): String = a1
    @KeyInfo(key = "a2", offset = 1)
    fun a2(): String = a2
    @KeyInfo(key = "a3", offset = 2)
    fun a3(): Int = a3
    @KeyInfo(key = "a4", offset = 3)
    fun a4(): Float = a4
    @KeyInfo(key = "a5", offset = 4)
    fun a5(): Any? = a5
    @KeyInfo(key = "a6", offset = 5)
    fun a6(): Int = a6
  }

  private val a1
    get() = `LiveLiterals$TestClass`.a1()
  val concat = `LiveLiterals$TestClass`.a1() + `LiveLiterals$TestClass`.a2()

  // Small constants use ICONST
  private val a3
    get() = `LiveLiterals$TestClass`.a3()
  private val a4
    get() = `LiveLiterals$TestClass`.a4()
  private val a5
    get() = `LiveLiterals$TestClass`.a5()
  private val a6
    get() = `LiveLiterals$TestClass`.a6()

  override fun receive(receiver: (Any?) -> Unit) {
    receiver(a1)
    receiver(a3)
    receiver(a4)
    receiver(a5)
    receiver(a6)
  }
}


class LiveLiteralsTransformTest {
  /** [StringWriter] that stores the decompiled classes after they've been transformed. */
  private val afterTransformTrace = StringWriter()

  /** [StringWriter] that stores the decompiled classes before they've been transformed. */
  private val beforeTransformTrace = StringWriter()

  /** [StringWriter] all the constant accesses. */
  private val constantAccessTrace = StringWriter()
  private val constantAccessLogger = PrintWriter(constantAccessTrace)

  // This will will log to the stdout logging information that might be useful debugging failures.
  // The logging only happens if the test fails.
  @get:Rule
  val onFailureRule = object : TestWatcher() {
    override fun failed(e: Throwable?, description: Description?) {
      super.failed(e, description)

      println("---- Constant accesses ----")
      println(constantAccessTrace)
      println("\n---- All available keys ----")
      println(DefaultConstantRemapper.allKeysToText())
      println("\n---- Classes before transformation ----")
      println(beforeTransformTrace)
      println("\n---- Classes after transformation ----")
      println(afterTransformTrace)
    }
  }

  /**
   * Sets up a new [TestClassLoader].
   * We take the already compiled classes in the test project, and save it to a byte array, applying the
   * transformations.
   */
  private fun setupTestClassLoader(classDefinitions: Map<String, Class<*>>, onHasLiveLiterals: () -> Unit = {}): TestClassLoader {
    // Create a SimpleRemapper that renames all the classes in `classDefinitions` from their old
    // names to the new ones.
    val classNameRemapper = SimpleRemapper(
      classDefinitions.map { (newClassName, clazz) -> Type.getInternalName(clazz) to newClassName }.toMap())
    val redefinedClasses = classDefinitions.map { (newClassName, clazz) ->
      val testClassBytes = loadClassBytes(clazz)

      val classReader = ClassReader(testClassBytes)
      val classOutputWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
      // Apply the live literals rewrite to all classes/methods
      val liveLiteralsRewriter = LiveLiteralsTransform(
        HasLiveLiteralsTransform(
          TraceClassVisitor(classOutputWriter, PrintWriter(afterTransformTrace)),
          fileInfoAnnotationName = FileInfo::class.java.name,
          onLiveLiteralsFound = onHasLiveLiterals
        ),
        fileInfoAnnotationName = FileInfo::class.java.name,
        infoAnnotationName = KeyInfo::class.java.name)
      // Move the class
      val remapper = ClassRemapper(liveLiteralsRewriter, classNameRemapper)
      classReader.accept(TraceClassVisitor(remapper, PrintWriter(beforeTransformTrace)), ClassReader.EXPAND_FRAMES)

      newClassName to classOutputWriter.toByteArray()
    }.toMap()

    return TestClassLoader(LiveLiteralsTransformTest::class.java.classLoader,
                           redefinedClasses)
  }

  @Before
  fun setup() {
    ConstantRemapperManager.setRemapper(object : ConstantRemapper {
      override fun addConstant(classLoader: ClassLoader?, reference: LiteralUsageReference, initialValue: Any, newValue: Any) =
        DefaultConstantRemapper.addConstant(classLoader, reference, initialValue, newValue)

      override fun clearConstants(classLoader: ClassLoader?) = DefaultConstantRemapper.clearConstants(classLoader)

      override fun remapConstant(source: Any?, fileName: String, offset: Int, initialValue: Any?): Any? {
        val result = DefaultConstantRemapper.remapConstant(source, fileName, offset, initialValue)
        val shortFileName = fileName.substringAfter(File.separator)
        constantAccessLogger.println("Access ($shortFileName:$offset, $initialValue) -> $result")
        return result
      }

      override fun hasConstants(): Boolean = DefaultConstantRemapper.hasConstants()

      override fun getModificationCount(): Long = DefaultConstantRemapper.modificationCount
    })
  }

  @After
  fun tearDown() {
    ConstantRemapperManager.restoreDefaultRemapper()
  }

  private fun usageReference(fileName: String, offset: Int): LiteralUsageReference {
    return LiteralUsageReference(
      FqName("Unused.method"),
      fileName,
      TextRange.create(offset, offset + 4), // The endoffset is not relevant for the constant mapping right now.
      -1)
  }

  @Test
  fun `regular top class instrumented successfully`() {
    var hasLiveLiterals = false
    val testClassLoader = setupTestClassLoader(mapOf(
      "Test" to TestClass::class.java,
      "LiveLiterals${'$'}Test" to TestClass.`LiveLiterals$TestClass`::class.java
    )) { hasLiveLiterals = true }

    DefaultConstantRemapper.addConstant(
      testClassLoader, usageReference("Test.kt", 0), "A1", "Remapped A1")
    DefaultConstantRemapper.addConstant(
      testClassLoader, usageReference("Test.kt", 3), 3.0f, 90f)
    DefaultConstantRemapper.addConstant(
      testClassLoader, usageReference("Test.kt", 5), 0, 999)
    val newTestClassInstance = testClassLoader.load("Test").newInstance() as Receiver

    assertTrue(hasLiveLiterals)

    val constantOutput = StringBuilder()
    newTestClassInstance.receive {
      constantOutput.append(it).append('\n')
    }
    assertEquals("""
        Remapped A1
        2
        90.0
        null
        999

      """.trimIndent(),
                 constantOutput.toString())
  }

  private fun time(callback: () -> Unit): Duration {
    val stopWatch = StopWatch()
    callback()
    return stopWatch.duration
  }

  @Test
  @Ignore("This test is just to measure performance locally and it's disabled by default")
  fun measurePerformance() {
    val iterations = 9500

    val originalClass = TestClass()
    println(time {
      repeat(iterations) {
        val builder = StringBuilder()
        originalClass.receive {
          builder.append(builder)
        }
      }
    })

    val testClassLoader = setupTestClassLoader(mapOf("Test" to TestClass::class.java))
    DefaultConstantRemapper.addConstant(testClassLoader, usageReference("Test", 0), "A1", "Remapped A1")
    val newTestClassInstance = testClassLoader.load("Test").newInstance() as Receiver

    println(time {
      repeat(iterations) {
        val builder = StringBuilder()
        newTestClassInstance.receive {
          builder.append(builder)
        }
      }
    })
  }
}