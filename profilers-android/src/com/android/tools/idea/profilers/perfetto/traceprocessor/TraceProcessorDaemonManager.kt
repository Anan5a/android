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
package com.android.tools.idea.profilers.perfetto.traceprocessor

import com.android.tools.idea.transport.DeployableFile
import com.google.common.annotations.VisibleForTesting
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


/**
 * This is responsible to manage the lifetime of an instance of the TraceProcessorDaemon,
 * spawning a new one if necessary and properly shutting it down at the end of Studio execution.
 */
class TraceProcessorDaemonManager: Disposable {
  // All access paths to process should be synchronized.
  private var process: Process? = null

  private companion object {
    private val LOGGER = Logger.getInstance(TraceProcessorDaemonManager::class.java)

    private val TPD_DEV_PATH: String by lazy {
      when {
        SystemInfo.isWindows -> {
          "prebuilts/tools/common/trace-processor-daemon/windows"
        }
        SystemInfo.isMac -> {
          "prebuilts/tools/common/trace-processor-daemon/darwin"
        }
        SystemInfo.isLinux -> {
          "prebuilts/tools/common/trace-processor-daemon/linux"
        }
        else -> {
          LOGGER.warn("Unsupported platform for TPD. Using linux binary.")
          "prebuilts/tools/common/trace-processor-daemon/linux"
        }
      }
    }
    private val TPD_RELEASE_PATH = "plugins/android/resources/trace_processor_daemon"
    private val TPD_EXECUTABLE: String by lazy {
      when {
        SystemInfo.isWindows -> {
          "trace_processor_daemon.exe"
        }
        else -> {
          "trace_processor_daemon"
        }
      }
    }

    private val TPD_BINARY = DeployableFile.Builder(TPD_EXECUTABLE)
      .setReleaseDir(TPD_RELEASE_PATH)
      .setDevDir(DeployableFile.getDevDir(TPD_DEV_PATH))
      .setExecutable(true)
      .build()

    private fun getExecutablePath(): String {
      return File(TPD_BINARY.dir, TPD_BINARY.fileName).absolutePath
    }
  }

  @VisibleForTesting
  fun processIsRunning(): Boolean {
    return process?.isAlive ?: false
  }

  @Synchronized
  fun makeSureDaemonIsRunning() {
    // Spawn a new one if either we don't have one running already or if the current one is not alive anymore.
    if (!processIsRunning()) {
      LOGGER.info("TPD Manager: Starting new instance of TPD")
      val newProcess = ProcessBuilder(getExecutablePath())
        .redirectErrorStream(true)
        .start()
      val processInputReader = BufferedReader(InputStreamReader(newProcess.inputStream))

      // wait until we receive the message that the daemon is listening
      while (true) {
        val line = processInputReader.readLine() ?: break
        LOGGER.info("TPD Manager: TPD - $line")
        if (line.startsWith("Server listening on ", true)) {
          break
        }
      }
      LOGGER.info("TPD Manager: TPD instance ready.")
      process = newProcess
    }
  }

  @Synchronized
  override fun dispose() {
    // We waitFor after destroying the process in order to not leave a Zombie process in the system.
    process?.destroyForcibly()?.waitFor()
  }
}