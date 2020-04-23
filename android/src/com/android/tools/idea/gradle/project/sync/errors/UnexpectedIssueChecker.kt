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
package com.android.tools.idea.gradle.project.sync.errors

import com.android.tools.idea.actions.SendFeedbackAction
import com.android.tools.idea.gradle.project.sync.errors.SyncErrorHandler.updateUsageTracker
import com.android.tools.idea.gradle.project.sync.idea.issues.MessageComposer
import com.google.wireless.android.sdk.stats.AndroidStudioEvent.GradleSyncFailure
import com.intellij.build.issue.BuildIssue
import com.intellij.build.issue.BuildIssueQuickFix
import com.intellij.ide.actions.ShowFilePathAction
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.issue.GradleIssueChecker
import org.jetbrains.plugins.gradle.issue.GradleIssueData
import org.jetbrains.plugins.gradle.service.execution.GradleExecutionErrorHandler
import java.io.File
import java.util.concurrent.CompletableFuture

class UnexpectedIssueChecker: GradleIssueChecker {
  private val UNEXPECTED_ERROR_FILE_BUG = "This is an unexpected error. Please file a bug containing the idea.log file."

  override fun check(issueData: GradleIssueData): BuildIssue? {
    val message= GradleExecutionErrorHandler.getRootCauseAndLocation(issueData.error).first.message ?: return null
    if (message.isEmpty() || !message.contains(UNEXPECTED_ERROR_FILE_BUG)) return null

    // Log metrics.
    invokeLater {
      updateUsageTracker(issueData.projectPath, GradleSyncFailure.UNEXPECTED_ERROR)
    }
    val description = MessageComposer(message).apply {
      addQuickFix("File a bug", FileBugQuickFix())
      addQuickFix("Show log file", ShowLogQuickFix())
    }
    return object : BuildIssue {
      override val title = "Gradle Sync issues."
      override val description = description.buildMessage()
      override val quickFixes = description.quickFixes
      override fun getNavigatable(project: Project) = null
    }
  }

  class FileBugQuickFix: BuildIssueQuickFix {
    override val id = "file.bug"

    override fun runQuickFix(project: Project, dataProvider: DataProvider): CompletableFuture<*> {
      val future = CompletableFuture<Any>()
      invokeLater {
        SendFeedbackAction.doPerformAction(project)
        future.complete(null)
      }
      return future
    }
  }

  class ShowLogQuickFix: BuildIssueQuickFix {
    override val id = "show.log.file"
    private val IDEA_LOG_FILE_NAME = "idea.log"

    override fun runQuickFix(project: Project, dataProvider: DataProvider): CompletableFuture<*> {
      val future = CompletableFuture<Any>()
      invokeLater {
        val logFile = File(PathManager.getLogPath(), IDEA_LOG_FILE_NAME)
        ShowFilePathAction.openFile(logFile)
        future.complete(null)
      }
      return future
    }
  }
}