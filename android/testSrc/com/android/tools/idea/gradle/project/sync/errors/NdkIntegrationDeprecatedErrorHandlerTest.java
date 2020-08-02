/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tools.idea.gradle.project.sync.errors;

import com.android.tools.idea.gradle.project.sync.errors.NdkIntegrationDeprecatedErrorHandler.SetUseDeprecatedNdkHyperlink;
import com.android.tools.idea.gradle.project.sync.issues.TestSyncIssueUsageReporter;
import com.android.tools.idea.gradle.project.sync.messages.GradleSyncMessagesStub;
import com.android.tools.idea.project.hyperlink.NotificationHyperlink;
import com.android.tools.idea.gradle.project.sync.hyperlink.OpenUrlHyperlink;
import com.android.tools.idea.testing.AndroidGradleTestCase;

import com.google.common.collect.ImmutableList;
import java.util.List;

import static com.android.tools.idea.gradle.project.sync.SimulatedSyncErrors.registerSyncErrorToSimulate;
import static com.android.tools.idea.testing.TestProjectPaths.SIMPLE_APPLICATION;
import static com.google.common.truth.Truth.assertThat;
import static com.google.wireless.android.sdk.stats.AndroidStudioEvent.GradleSyncFailure.NDK_INTEGRATION_DEPRECATED;
import static com.google.wireless.android.sdk.stats.AndroidStudioEvent.GradleSyncQuickFix.OPEN_URL_HYPERLINK;
import static com.google.wireless.android.sdk.stats.AndroidStudioEvent.GradleSyncQuickFix.SET_USE_DEPRECATED_NDK_HYPERLINK;

/**
 * Tests for {@link NdkIntegrationDeprecatedErrorHandler}.
 */
public class NdkIntegrationDeprecatedErrorHandlerTest extends AndroidGradleTestCase {
  private GradleSyncMessagesStub mySyncMessagesStub;
  private TestSyncIssueUsageReporter myUsageReporter;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    mySyncMessagesStub = GradleSyncMessagesStub.replaceSyncMessagesService(getProject(), getTestRootDisposable());
    myUsageReporter = TestSyncIssueUsageReporter.replaceSyncMessagesService(getProject(), getTestRootDisposable());
  }

  public void testHandleError() throws Exception {
    registerSyncErrorToSimulate("Error: NDK integration is deprecated in the current plugin.  Consider trying the new " +
                                "experimental plugin.  For details, see " +
                                "https://developer.android.com/studio/build/experimental-plugin.html  " +
                                "Set \"android.useDeprecatedNdk=true\" in gradle.properties to continue using the current " +
                                "NDK integration.");

    loadProjectAndExpectSyncError(SIMPLE_APPLICATION);

    GradleSyncMessagesStub.NotificationUpdate notificationUpdate = mySyncMessagesStub.getNotificationUpdate();
    assertNotNull(notificationUpdate);
    assertThat(notificationUpdate.getText()).isEqualTo("NDK integration is deprecated in the current plugin.");

    // Verify hyperlinks are correct.
    List<NotificationHyperlink> quickFixes = notificationUpdate.getFixes();
    assertThat(quickFixes).hasSize(2);

    assertThat(quickFixes.get(0)).isInstanceOf(OpenUrlHyperlink.class);
    assertThat(quickFixes.get(1)).isInstanceOf(SetUseDeprecatedNdkHyperlink.class);

    assertEquals(NDK_INTEGRATION_DEPRECATED, myUsageReporter.getCollectedFailure());
    assertEquals(ImmutableList.of(OPEN_URL_HYPERLINK, SET_USE_DEPRECATED_NDK_HYPERLINK), myUsageReporter.getCollectedQuickFixes());
  }
}