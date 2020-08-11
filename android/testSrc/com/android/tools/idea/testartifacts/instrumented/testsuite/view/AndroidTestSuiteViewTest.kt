/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.tools.idea.testartifacts.instrumented.testsuite.view

import com.android.sdklib.AndroidVersion
import com.android.testutils.MockitoKt.eq
import com.android.testutils.MockitoKt.mock
import com.android.tools.idea.testartifacts.instrumented.testsuite.api.ANDROID_TEST_RESULT_LISTENER_KEY
import com.android.tools.idea.testartifacts.instrumented.testsuite.api.AndroidTestResults
import com.android.tools.idea.testartifacts.instrumented.testsuite.api.getFullTestCaseName
import com.android.tools.idea.testartifacts.instrumented.testsuite.model.AndroidDevice
import com.android.tools.idea.testartifacts.instrumented.testsuite.model.AndroidDeviceType
import com.android.tools.idea.testartifacts.instrumented.testsuite.model.AndroidTestCase
import com.android.tools.idea.testartifacts.instrumented.testsuite.model.AndroidTestCaseResult
import com.android.tools.idea.testartifacts.instrumented.testsuite.model.AndroidTestSuite
import com.google.common.truth.Truth.assertThat
import com.google.wireless.android.sdk.stats.ParallelAndroidTestReportUiEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.testFramework.DisposableRule
import com.intellij.testFramework.EdtRule
import com.intellij.testFramework.ProjectRule
import com.intellij.testFramework.RunsInEdt
import com.intellij.ui.dualView.TreeTableView
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Unit tests for [AndroidTestSuiteView].
 */
@RunWith(JUnit4::class)
@RunsInEdt
class AndroidTestSuiteViewTest {

  private val projectRule = ProjectRule()
  private val disposableRule = DisposableRule()

  @get:Rule val rules: RuleChain = RuleChain
    .outerRule(projectRule)
    .around(EdtRule())
    .around(disposableRule)

  @Mock lateinit var processHandler: ProcessHandler

  @Before
  fun setup() {
    MockitoAnnotations.initMocks(this)
    resetToDefaultState()
  }

  @After
  fun tearDown() {
    resetToDefaultState()
  }

  private fun resetToDefaultState() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    // These persisted properties need to be reset before and after tests.
    view.myPassedToggleButton.isSelected = true
    view.myFailedToggleButton.isSelected = true
    view.mySkippedToggleButton.isSelected = true
    view.myInProgressToggleButton.isSelected = true
    view.mySortByNameToggleButton.isSelected = false
    view.mySortByDurationToggleButton.isSelected = false
  }

  @Test
  fun attachToProcess() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    view.attachToProcess(processHandler)

    verify(processHandler).putCopyableUserData(eq(ANDROID_TEST_RESULT_LISTENER_KEY), eq(view))
  }

  @Test
  fun detailsViewIsVisibleAndRawTestOutputIsDisplayedInitially() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    assertThat(view.detailsViewForTesting.rootPanel.isVisible).isTrue()
    assertThat(view.tableForTesting.getTableViewForTesting().selectedColumn).isEqualTo(0)
    assertThat(view.tableForTesting.getTableViewForTesting().selectedRow).isEqualTo(0)
  }

  @Test
  fun openAndCloseDetailsView() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    val device1 = device("deviceId1", "deviceName1")
    val device2 = device("deviceId2", "deviceName2")
    val testsuiteOnDevice1 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 2)
    val testcase1OnDevice1 = AndroidTestCase("testId1", "method1", "class1", "package1")
    val testcase2OnDevice1 = AndroidTestCase("testId2", "method2", "class2", "package2")
    val testsuiteOnDevice2 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 2)
    val testcase1OnDevice2 = AndroidTestCase("testId1", "method1", "class1", "package1")
    val testcase2OnDevice2 = AndroidTestCase("testId2", "method2", "class2", "package2")

    view.onTestSuiteScheduled(device1)
    view.onTestSuiteScheduled(device2)

    // Test execution on device 1.
    view.onTestSuiteStarted(device1, testsuiteOnDevice1)
    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase1OnDevice1)
    testcase1OnDevice1.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase1OnDevice1)
    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase2OnDevice1)
    testcase2OnDevice1.result = AndroidTestCaseResult.FAILED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase2OnDevice1)
    view.onTestSuiteFinished(device1, testsuiteOnDevice1)

    // Test execution on device 2.
    view.onTestSuiteStarted(device2, testsuiteOnDevice2)
    view.onTestCaseStarted(device2, testsuiteOnDevice2, testcase1OnDevice2)
    testcase1OnDevice2.result = AndroidTestCaseResult.SKIPPED
    view.onTestCaseFinished(device2, testsuiteOnDevice2, testcase1OnDevice2)
    view.onTestCaseStarted(device2, testsuiteOnDevice2, testcase2OnDevice2)
    testcase2OnDevice2.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device2, testsuiteOnDevice2, testcase2OnDevice2)
    view.onTestSuiteFinished(device2, testsuiteOnDevice2)

    // Click on the test case 2 results row.
    view.onAndroidTestResultsRowSelected(view.tableForTesting.getTableViewForTesting().getItem(4),
                                         /*selectedDevice=*/null)

    // Verifies the details view is visible now.
    assertThat(view.detailsViewForTesting.rootPanel.isVisible).isTrue()
    assertThat(view.detailsViewForTesting.titleTextViewForTesting.text).isEqualTo("package2.class2.method2")
    assertThat(view.detailsViewForTesting.selectedDeviceForTesting).isEqualTo(device1)

    // Click on the test case 1 results row in device2 column.
    view.onAndroidTestResultsRowSelected(view.tableForTesting.getTableViewForTesting().getItem(2),
                                         /*selectedDevice=*/device2)

    // Verifies the details view is visible now.
    assertThat(view.detailsViewForTesting.rootPanel.isVisible).isTrue()
    assertThat(view.detailsViewForTesting.titleTextViewForTesting.text).isEqualTo("package1.class1.method1")
    assertThat(view.detailsViewForTesting.selectedDeviceForTesting).isEqualTo(device2)

    // Finally, close the details view.
    view.onAndroidTestSuiteDetailsViewCloseButtonClicked()

    assertThat(view.detailsViewForTesting.rootPanel.isVisible).isFalse()

    assertThat(view.myLogger.getImpressionsForTesting()).containsExactly(
      ParallelAndroidTestReportUiEvent.UiElement.TEST_SUITE_VIEW,
      ParallelAndroidTestReportUiEvent.UiElement.TEST_SUITE_VIEW_TABLE_ROW,
      ParallelAndroidTestReportUiEvent.UiElement.TEST_SUITE_DETAILS_HORIZONTAL_VIEW)
  }

  @Test
  fun filterByTestStatus() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    val device1 = device("deviceId1", "deviceName1")
    val testsuiteOnDevice1 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 4)
    val testcase1OnDevice1 = AndroidTestCase("testId1", "method1", "classA", "packageA")
    val testcase2OnDevice1 = AndroidTestCase("testId2", "method2", "classA", "packageA")
    val testcase3OnDevice1 = AndroidTestCase("testId3", "method3", "classB", "packageB")
    val testcase4OnDevice1 = AndroidTestCase("testId4", "method4", "classB", "packageB")
    val testcase5OnDevice1 = AndroidTestCase("testId5", "method5", "classC", "packageC")

    view.onTestSuiteScheduled(device1)

    // Test execution on device 1.
    view.onTestSuiteStarted(device1, testsuiteOnDevice1)

    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase1OnDevice1)
    testcase1OnDevice1.result = AndroidTestCaseResult.FAILED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase1OnDevice1)

    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase2OnDevice1)
    testcase2OnDevice1.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase2OnDevice1)

    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase3OnDevice1)
    testcase3OnDevice1.result = AndroidTestCaseResult.SKIPPED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase3OnDevice1)

    testcase4OnDevice1.result = AndroidTestCaseResult.IN_PROGRESS
    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase4OnDevice1)

    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase5OnDevice1)
    testcase5OnDevice1.result = AndroidTestCaseResult.CANCELLED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase5OnDevice1)

    val tableView = view.tableForTesting.getTableViewForTesting()

    // Initially, all tests are displayed.
    assertThat(tableView.rowCount).isEqualTo(9)
    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")  // Root aggregation (failed)
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("packageA.classA.")  // Class A aggregation (failed)
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("packageA.classA.method1")  // method 1 (failed)
    assertThat(tableView.getItem(3).getFullTestCaseName()).isEqualTo("packageA.classA.method2")  // method 2 (passed)
    assertThat(tableView.getItem(4).getFullTestCaseName()).isEqualTo("packageB.classB.")  // Class B aggregation (in progress)
    assertThat(tableView.getItem(5).getFullTestCaseName()).isEqualTo("packageB.classB.method3")  // method 3 (skipped)
    assertThat(tableView.getItem(6).getFullTestCaseName()).isEqualTo("packageB.classB.method4")  // method 4 (in progress)
    assertThat(tableView.getItem(7).getFullTestCaseName()).isEqualTo("packageC.classC.")  // Class C aggregation (cancelled)
    assertThat(tableView.getItem(8).getFullTestCaseName()).isEqualTo("packageC.classC.method5")  // method 5 (cancelled)

    // Remove "Skipped".
    view.mySkippedToggleButton.isSelected = false

    assertThat(tableView.rowCount).isEqualTo(8)
    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")  // Root aggregation (failed)
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("packageA.classA.")  // Class A aggregation (failed)
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("packageA.classA.method1")  // method 1 (failed)
    assertThat(tableView.getItem(3).getFullTestCaseName()).isEqualTo("packageA.classA.method2")  // method 2 (passed)
    assertThat(tableView.getItem(4).getFullTestCaseName()).isEqualTo("packageB.classB.")  // Class B aggregation (in progress)
    assertThat(tableView.getItem(5).getFullTestCaseName()).isEqualTo("packageB.classB.method4")  // method 4 (in progress)
    assertThat(tableView.getItem(6).getFullTestCaseName()).isEqualTo("packageC.classC.")  // Class C aggregation (cancelled)
    assertThat(tableView.getItem(7).getFullTestCaseName()).isEqualTo("packageC.classC.method5")  // method 5 (cancelled)

    // Remove "Passed", "Failed" and "In progress". Then select "Skipped".
    view.myPassedToggleButton.isSelected = false
    view.myFailedToggleButton.isSelected = false
    view.mySkippedToggleButton.isSelected = true
    view.myInProgressToggleButton.isSelected = false

    assertThat(tableView.rowCount).isEqualTo(5)
    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")  // Root aggregation (failed)
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("packageB.classB.")  // Class B aggregation (in progress)
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("packageB.classB.method3")  // method 3 (skipped)
    assertThat(tableView.getItem(3).getFullTestCaseName()).isEqualTo("packageC.classC.")  // Class C aggregation (cancelled)
    assertThat(tableView.getItem(4).getFullTestCaseName()).isEqualTo("packageC.classC.method5")  // method 5 (cancelled)

    // Remove "Skipped" and select "In Progress".
    view.mySkippedToggleButton.isSelected = false
    view.myInProgressToggleButton.isSelected = true
    view.tableForTesting.createExpandAllAction().actionPerformed(mock())

    assertThat(tableView.rowCount).isEqualTo(5)
    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")  // Root aggregation (failed)
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("packageB.classB.")  // Class B aggregation (in progress)
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("packageB.classB.method4")  // method 4 (in progress)
    assertThat(tableView.getItem(3).getFullTestCaseName()).isEqualTo("packageC.classC.")  // Class C aggregation (cancelled)
    assertThat(tableView.getItem(4).getFullTestCaseName()).isEqualTo("packageC.classC.method5")  // method 5 (cancelled)

    // Remove "In Progress". (Nothing is selected).
    view.myInProgressToggleButton.isSelected = false

    assertThat(tableView.rowCount).isEqualTo(3)
    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")  // Root aggregation should always be visible.
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("packageC.classC.")  // Class C aggregation (cancelled)
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("packageC.classC.method5")  // method 5 (cancelled)
  }

  @Test
  fun filterByTestStatusButtonStateShouldPersist() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    // All buttons are selected initially.
    assertThat(view.myFailedToggleButton.isSelected).isTrue()
    assertThat(view.myPassedToggleButton.isSelected).isTrue()
    assertThat(view.mySkippedToggleButton.isSelected).isTrue()
    assertThat(view.myInProgressToggleButton.isSelected).isTrue()

    // Update state to false.
    view.myFailedToggleButton.isSelected = false
    view.myPassedToggleButton.isSelected = false
    view.mySkippedToggleButton.isSelected = false
    view.myInProgressToggleButton.isSelected = false

    val view2 = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    // The new state should persist even after recreation of the view.
    assertThat(view2.myFailedToggleButton.isSelected).isFalse()
    assertThat(view2.myPassedToggleButton.isSelected).isFalse()
    assertThat(view2.mySkippedToggleButton.isSelected).isFalse()
    assertThat(view2.myInProgressToggleButton.isSelected).isFalse()
  }

  @Test
  fun filterByDevice() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    val device1 = device("deviceId1", "deviceName1")
    val device2 = device("deviceId2", "deviceName2")
    val testsuiteOnDevice1 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 2)
    val testcase1OnDevice1 = AndroidTestCase("testId1", "method1", "class1", "package1")
    val testcase2OnDevice1 = AndroidTestCase("testId2", "method2", "class2", "package2")
    val testsuiteOnDevice2 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 2)
    val testcase1OnDevice2 = AndroidTestCase("testId1", "method1", "class1", "package1")
    val testcase2OnDevice2 = AndroidTestCase("testId2", "method2", "class2", "package2")

    view.onTestSuiteScheduled(device1)
    view.onTestSuiteScheduled(device2)

    // Test execution on device 1.
    view.onTestSuiteStarted(device1, testsuiteOnDevice1)
    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase1OnDevice1)
    testcase1OnDevice1.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase1OnDevice1)
    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase2OnDevice1)
    testcase2OnDevice1.result = AndroidTestCaseResult.FAILED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase2OnDevice1)
    view.onTestSuiteFinished(device1, testsuiteOnDevice1)

    // Test execution on device 2.
    view.onTestSuiteStarted(device2, testsuiteOnDevice2)
    view.onTestCaseStarted(device2, testsuiteOnDevice2, testcase1OnDevice2)
    testcase1OnDevice2.result = AndroidTestCaseResult.SKIPPED
    view.onTestCaseFinished(device2, testsuiteOnDevice2, testcase1OnDevice2)
    view.onTestCaseStarted(device2, testsuiteOnDevice2, testcase2OnDevice2)
    testcase2OnDevice2.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device2, testsuiteOnDevice2, testcase2OnDevice2)
    view.onTestSuiteFinished(device2, testsuiteOnDevice2)

    // Select "device2" in the device filter ComboBox.
    val selectDevice2Action = view.myDeviceAndApiLevelFilterComboBoxAction.createActionGroup().flattenedActions().find {
      it.templateText == "deviceName2"
    }
    requireNotNull(selectDevice2Action).actionPerformed(mock())

    val tableView = view.tableForTesting.getTableViewForTesting()
    val tableViewModel = view.tableForTesting.getModelForTesting()
    assertThat(tableView.columnCount).isEqualTo(3)
    assertThat(tableViewModel.columns[0].name).isEqualTo("Tests")
    assertThat(tableViewModel.columns[1].name).isEqualTo("Status")
    assertThat(tableViewModel.columns[2].name).isEqualTo("deviceName2")
  }

  @Test
  fun filterByApiLevel() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    val device1 = device("deviceId1", "deviceName1", 29)
    val device2 = device("deviceId2", "deviceName2", 28)
    val testsuiteOnDevice1 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 2)
    val testcase1OnDevice1 = AndroidTestCase("testId1", "method1", "class1", "package1")
    val testcase2OnDevice1 = AndroidTestCase("testId2", "method2", "class2", "package2")
    val testsuiteOnDevice2 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 2)
    val testcase1OnDevice2 = AndroidTestCase("testId1", "method1", "class1", "package1")
    val testcase2OnDevice2 = AndroidTestCase("testId2", "method2", "class2", "package2")

    view.onTestSuiteScheduled(device1)
    view.onTestSuiteScheduled(device2)

    // Test execution on device 1.
    view.onTestSuiteStarted(device1, testsuiteOnDevice1)
    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase1OnDevice1)
    testcase1OnDevice1.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase1OnDevice1)
    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase2OnDevice1)
    testcase2OnDevice1.result = AndroidTestCaseResult.FAILED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase2OnDevice1)
    view.onTestSuiteFinished(device1, testsuiteOnDevice1)

    // Test execution on device 2.
    view.onTestSuiteStarted(device2, testsuiteOnDevice2)
    view.onTestCaseStarted(device2, testsuiteOnDevice2, testcase1OnDevice2)
    testcase1OnDevice2.result = AndroidTestCaseResult.SKIPPED
    view.onTestCaseFinished(device2, testsuiteOnDevice2, testcase1OnDevice2)
    view.onTestCaseStarted(device2, testsuiteOnDevice2, testcase2OnDevice2)
    testcase2OnDevice2.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device2, testsuiteOnDevice2, testcase2OnDevice2)
    view.onTestSuiteFinished(device2, testsuiteOnDevice2)

    // Select "API 29" in the API level filter ComboBox.
    view.myDeviceAndApiLevelFilterComboBoxAction.createActionGroup().getChildren(null)
    val selectApi29Action = view.myDeviceAndApiLevelFilterComboBoxAction.createActionGroup().flattenedActions().find {
      it.templateText == "API 29"
    }
    requireNotNull(selectApi29Action).actionPerformed(mock())

    val tableView = view.tableForTesting.getTableViewForTesting()
    val tableViewModel = view.tableForTesting.getModelForTesting()
    assertThat(tableView.columnCount).isEqualTo(3)
    assertThat(tableViewModel.columns[0].name).isEqualTo("Tests")
    assertThat(tableViewModel.columns[1].name).isEqualTo("Status")
    assertThat(tableViewModel.columns[2].name).isEqualTo("deviceName1")
  }

  private fun ActionGroup.flattenedActions(): Sequence<AnAction> = sequence {
    getChildren(null).forEach {
      if (it is ActionGroup) {
        yieldAll(it.flattenedActions())
      } else {
        yield(it)
      }
    }
  }

  @Test
  fun sortRows() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    val device1 = device("deviceId1", "deviceName1")
    val testsuite = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 4)
    val testcase1 = AndroidTestCase("testId1", "Z_method1", "Z_class", "package")
    val testcase2 = AndroidTestCase("testId2", "A_method2", "Z_class", "package")
    val testcase3 = AndroidTestCase("testId3", "A_method3", "A_class2", "package")
    val testcase4 = AndroidTestCase("testId4", "Z_method4", "A_class2", "package")

    view.onTestSuiteScheduled(device1)
    view.onTestSuiteStarted(device1, testsuite)

    val runTestCase = { testCase: AndroidTestCase, elapsedTimeMillis: Long ->
      view.onTestCaseStarted(device1, testsuite, testCase)
      testCase.apply {
        result = AndroidTestCaseResult.PASSED
        startTimestampMillis = 0
        endTimestampMillis = elapsedTimeMillis
      }
      view.onTestCaseFinished(device1, testsuite, testCase)
    }

    runTestCase(testcase1, 100)
    runTestCase(testcase2, 200)
    runTestCase(testcase3, 300)
    runTestCase(testcase4, 10)

    view.onTestSuiteFinished(device1, testsuite)

    val tableView = view.tableForTesting.getTableViewForTesting()
    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("package.Z_class.")
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("package.Z_class.Z_method1")
    assertThat(tableView.getItem(3).getFullTestCaseName()).isEqualTo("package.Z_class.A_method2")
    assertThat(tableView.getItem(4).getFullTestCaseName()).isEqualTo("package.A_class2.")
    assertThat(tableView.getItem(5).getFullTestCaseName()).isEqualTo("package.A_class2.A_method3")
    assertThat(tableView.getItem(6).getFullTestCaseName()).isEqualTo("package.A_class2.Z_method4")

    // Enable sort by name.
    view.mySortByNameToggleButton.isSelected = true

    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("package.A_class2.")
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("package.A_class2.A_method3")
    assertThat(tableView.getItem(3).getFullTestCaseName()).isEqualTo("package.A_class2.Z_method4")
    assertThat(tableView.getItem(4).getFullTestCaseName()).isEqualTo("package.Z_class.")
    assertThat(tableView.getItem(5).getFullTestCaseName()).isEqualTo("package.Z_class.A_method2")
    assertThat(tableView.getItem(6).getFullTestCaseName()).isEqualTo("package.Z_class.Z_method1")

    // Disabling sort by name should restore the original insertion order.
    view.mySortByNameToggleButton.isSelected = false

    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("package.Z_class.")
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("package.Z_class.Z_method1")
    assertThat(tableView.getItem(3).getFullTestCaseName()).isEqualTo("package.Z_class.A_method2")
    assertThat(tableView.getItem(4).getFullTestCaseName()).isEqualTo("package.A_class2.")
    assertThat(tableView.getItem(5).getFullTestCaseName()).isEqualTo("package.A_class2.A_method3")
    assertThat(tableView.getItem(6).getFullTestCaseName()).isEqualTo("package.A_class2.Z_method4")

    // Enable sort by duration.
    view.mySortByDurationToggleButton.isSelected = true

    assertThat(tableView.getItem(0).getFullTestCaseName()).isEqualTo(".")
    assertThat(tableView.getItem(1).getFullTestCaseName()).isEqualTo("package.A_class2.")
    assertThat(tableView.getItem(2).getFullTestCaseName()).isEqualTo("package.A_class2.A_method3")  // 300 ms
    assertThat(tableView.getItem(3).getFullTestCaseName()).isEqualTo("package.A_class2.Z_method4")  // 10 ms
    assertThat(tableView.getItem(4).getFullTestCaseName()).isEqualTo("package.Z_class.")
    assertThat(tableView.getItem(5).getFullTestCaseName()).isEqualTo("package.Z_class.A_method2")  // 200 ms
    assertThat(tableView.getItem(6).getFullTestCaseName()).isEqualTo("package.Z_class.Z_method1")  // 100 ms
  }

  @Test
  fun progressBar() {
    val view = AndroidTestSuiteView(disposableRule.disposable, projectRule.project, null)

    val device1 = device("deviceId1", "deviceName1", 29)
    val device2 = device("deviceId2", "deviceName2", 28)
    val testsuiteOnDevice1 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 2)
    val testcase1OnDevice1 = AndroidTestCase("testId1", "method1", "class1", "package1")
    val testcase2OnDevice1 = AndroidTestCase("testId2", "method2", "class2", "package2")
    val testsuiteOnDevice2 = AndroidTestSuite("testsuiteId", "testsuiteName", testCaseCount = 2)
    val testcase1OnDevice2 = AndroidTestCase("testId1", "method1", "class1", "package1")
    val testcase2OnDevice2 = AndroidTestCase("testId2", "method2", "class2", "package2")

    assertThat(view.myProgressBar.isIndeterminate).isFalse()
    assertThat(view.myProgressBar.value).isEqualTo(0)
    assertThat(view.myProgressBar.maximum).isEqualTo(100)

    // Add scheduled devices.
    view.onTestSuiteScheduled(device1)
    view.onTestSuiteScheduled(device2)

    assertThat(view.myProgressBar.value).isEqualTo(0)
    assertThat(view.myProgressBar.maximum).isEqualTo(100)

    // Test execution on device 1.
    view.onTestSuiteStarted(device1, testsuiteOnDevice1)

    // Progress = (completed tests / scheduled tests) * (started devices / scheduled devices).
    assertThat(view.myProgressBar.value).isEqualTo(0 * 1)
    assertThat(view.myProgressBar.maximum).isEqualTo(2 * 2)

    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase1OnDevice1)
    testcase1OnDevice1.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase1OnDevice1)

    assertThat(view.myProgressBar.value).isEqualTo(1 * 1)
    assertThat(view.myProgressBar.maximum).isEqualTo(2 * 2)

    view.onTestCaseStarted(device1, testsuiteOnDevice1, testcase2OnDevice1)
    testcase2OnDevice1.result = AndroidTestCaseResult.FAILED
    view.onTestCaseFinished(device1, testsuiteOnDevice1, testcase2OnDevice1)
    view.onTestSuiteFinished(device1, testsuiteOnDevice1)

    assertThat(view.myProgressBar.value).isEqualTo(2 * 1)
    assertThat(view.myProgressBar.maximum).isEqualTo(2 * 2)

    // Test execution on device 2.
    view.onTestSuiteStarted(device2, testsuiteOnDevice2)
    view.onTestCaseStarted(device2, testsuiteOnDevice2, testcase1OnDevice2)
    testcase1OnDevice2.result = AndroidTestCaseResult.SKIPPED
    view.onTestCaseFinished(device2, testsuiteOnDevice2, testcase1OnDevice2)

    assertThat(view.myProgressBar.value).isEqualTo(3 * 2)
    assertThat(view.myProgressBar.maximum).isEqualTo(4 * 2)

    view.onTestCaseStarted(device2, testsuiteOnDevice2, testcase2OnDevice2)
    testcase2OnDevice2.result = AndroidTestCaseResult.PASSED
    view.onTestCaseFinished(device2, testsuiteOnDevice2, testcase2OnDevice2)
    view.onTestSuiteFinished(device2, testsuiteOnDevice2)

    assertThat(view.myProgressBar.value).isEqualTo(4 * 2)
    assertThat(view.myProgressBar.maximum).isEqualTo(4 * 2)
  }

  private fun device(id: String, name: String, apiVersion: Int = 28): AndroidDevice {
    return AndroidDevice(id, name, AndroidDeviceType.LOCAL_EMULATOR, AndroidVersion(apiVersion))
  }

  private fun TreeTableView.getItem(index: Int): AndroidTestResults {
    return getValueAt(index, 0) as AndroidTestResults
  }
}