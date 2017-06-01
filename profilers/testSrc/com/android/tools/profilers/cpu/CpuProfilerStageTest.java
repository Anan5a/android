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
package com.android.tools.profilers.cpu;

import com.android.tools.adtui.model.AspectObserver;
import com.android.tools.adtui.model.FakeTimer;
import com.android.tools.adtui.model.Range;
import com.android.tools.perflib.vmtrace.ClockType;
import com.android.tools.perflib.vmtrace.ThreadInfo;
import com.android.tools.profiler.proto.Common;
import com.android.tools.profiler.proto.CpuProfiler;
import com.android.tools.profiler.proto.Profiler;
import com.android.tools.profilers.*;
import com.android.tools.profilers.event.FakeEventService;
import com.android.tools.profilers.memory.FakeMemoryService;
import com.android.tools.profilers.network.FakeNetworkService;
import com.android.tools.profilers.stacktrace.CodeLocation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CpuProfilerStageTest extends AspectObserver {
  private final FakeProfilerService myProfilerService = new FakeProfilerService();

  private final FakeCpuService myCpuService = new FakeCpuService();

  private final FakeTimer myTimer = new FakeTimer();

  @Rule
  public FakeGrpcChannel myGrpcChannel =
    new FakeGrpcChannel("CpuProfilerStageTestChannel", myCpuService, myProfilerService,
                        new FakeMemoryService(), new FakeEventService(), FakeNetworkService.newBuilder().build());

  private CpuProfilerStage myStage;

  private FakeIdeProfilerServices myServices;

  private boolean myCaptureDetailsCalled;

  @Before
  public void setUp() throws Exception {
    myServices = new FakeIdeProfilerServices();
    StudioProfilers profilers = new StudioProfilers(myGrpcChannel.getClient(), myServices, myTimer);
    // One second must be enough for new devices (and processes) to be picked up
    myTimer.tick(FakeTimer.ONE_SECOND_IN_NS);
    myStage = new CpuProfilerStage(profilers);
    myStage.getStudioProfilers().setStage(myStage);
  }

  @Test
  public void testDefaultValues() throws IOException {
    assertNotNull(myStage.getCpuTraceDataSeries());
    assertNotNull(myStage.getThreadStates());
    assertEquals(ProfilerMode.NORMAL, myStage.getProfilerMode());
    assertNull(myStage.getCapture());
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
    assertNotNull(myStage.getAspect());
  }

  @Test
  public void testStartCapturing() throws InterruptedException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());

    // Start a successful capture
    startCapturingSuccess();

    // Start a failing capture
    myCpuService.setStartProfilingStatus(CpuProfiler.CpuProfilingAppStartResponse.Status.FAILURE);
    startCapturing();
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
  }

  @Test
  public void startCapturingInstrumented() throws InterruptedException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
    myCpuService.setStartProfilingStatus(CpuProfiler.CpuProfilingAppStartResponse.Status.SUCCESS);
    myServices.setPrePoolExecutor(() -> assertEquals(CpuProfilerStage.CaptureState.STARTING, myStage.getCaptureState()));
    // Start a capture using INSTRUMENTED mode
    ProfilingConfiguration instrumented = new ProfilingConfiguration("My Instrumented Config",
                                                                     CpuProfiler.CpuProfilerType.ART,
                                                                     CpuProfiler.CpuProfilingAppStartRequest.Mode.INSTRUMENTED);
    myStage.setProfilingConfiguration(instrumented);
    startCapturing();
    assertEquals(CpuProfilerStage.CaptureState.CAPTURING, myStage.getCaptureState());
  }

  @Test
  public void testStopCapturingInvalidTrace() throws InterruptedException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());

    // Start a successful capture
    startCapturingSuccess();

    // Stop capturing, but don't include a trace in the response.
    myServices.setOnExecute(() -> {
      // First, the main executor is going to be called to execute stopCapturingCallback,
      // which should set the capture state to PARSING
      assertEquals(CpuProfilerStage.CaptureState.PARSING, myStage.getCaptureState());
      // Then, the next time the main executor is called, it will try to parse the capture unsuccessfully
      // and set the capture state to IDLE
      myServices.setOnExecute(() -> {
        assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
        // Capture was stopped successfully, but capture should still be null as the response has no valid trace
        assertNull(myStage.getCapture());
      });
    });
    myCpuService.setStopProfilingStatus(com.android.tools.profiler.proto.CpuProfiler.CpuProfilingAppStopResponse.Status.SUCCESS);
    myCpuService.setValidTrace(false);
    stopCapturing();
  }

  @Test
  public void testStopCapturingInvalidTraceFailureStatus() throws InterruptedException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());

    // Start a successful capture
    startCapturingSuccess();

    // Stop a capture unsuccessfully
    myCpuService.setStopProfilingStatus(CpuProfiler.CpuProfilingAppStopResponse.Status.FAILURE);
    myCpuService.setValidTrace(false);
    myServices.setOnExecute(() -> {
      assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
      assertNull(myStage.getCapture());
    });
    stopCapturing();
  }

  @Test
  public void testStopCapturingValidTraceFailureStatus() throws InterruptedException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());

    // Start a successful capture
    startCapturingSuccess();

    // Stop a capture unsuccessfully, but with a valid trace
    myCpuService.setStopProfilingStatus(CpuProfiler.CpuProfilingAppStopResponse.Status.FAILURE);
    myCpuService.setValidTrace(true);
    myCpuService.setGetTraceResponseStatus(CpuProfiler.GetTraceResponse.Status.SUCCESS);
    myServices.setOnExecute(() -> {
      assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
      // Despite the fact of having a valid trace, we first check for the response status.
      // As it wasn't SUCCESS, capture should not be set.
      assertNull(myStage.getCapture());
    });
    stopCapturing();
  }

  @Test
  public void testStopCapturingSuccessfully() throws InterruptedException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
    captureSuccessfully();
  }

  @Test
  public void testSelectedThread() {
    myStage.setSelectedThread(0);
    assertEquals(0, myStage.getSelectedThread());

    myStage.setSelectedThread(42);
    assertEquals(42, myStage.getSelectedThread());
  }

  @Test
  public void testCaptureDetails() throws InterruptedException, IOException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());

    captureSuccessfully();

    myStage.setSelectedThread(myStage.getCapture().getMainThreadId());

    AspectObserver observer = new AspectObserver();
    myStage.getAspect().addDependency(observer).onChange(CpuProfilerAspect.CAPTURE_DETAILS, () -> myCaptureDetailsCalled = true);

    // Top Down
    myCaptureDetailsCalled = false;
    myStage.setCaptureDetails(CaptureModel.Details.Type.TOP_DOWN);
    assertTrue(myCaptureDetailsCalled = true);

    CaptureModel.Details details = myStage.getCaptureDetails();
    assertTrue(details instanceof CaptureModel.TopDown);
    assertNotNull(((CaptureModel.TopDown)details).getModel());

    // Bottom Up
    myCaptureDetailsCalled = false;
    myStage.setCaptureDetails(CaptureModel.Details.Type.BOTTOM_UP);
    assertTrue(myCaptureDetailsCalled);

    details = myStage.getCaptureDetails();
    assertTrue(details instanceof CaptureModel.BottomUp);
    assertNotNull(((CaptureModel.BottomUp)details).getModel());

    // Chart
    myCaptureDetailsCalled = false;
    myStage.setCaptureDetails(CaptureModel.Details.Type.CALL_CHART);
    assertTrue(myCaptureDetailsCalled);

    details = myStage.getCaptureDetails();
    assertTrue(details instanceof CaptureModel.CallChart);
    assertNotNull(((CaptureModel.CallChart)details).getNode());

    // null
    myCaptureDetailsCalled = false;
    myStage.setCaptureDetails(null);
    assertTrue(myCaptureDetailsCalled);
    assertNull(myStage.getCaptureDetails());

    // CaptureNode is null, as a result the model is null as well
    myStage.setSelectedThread(-1);
    myCaptureDetailsCalled = false;
    myStage.setCaptureDetails(CaptureModel.Details.Type.BOTTOM_UP);
    assertTrue(myCaptureDetailsCalled);
    details = myStage.getCaptureDetails();
    assertTrue(details instanceof CaptureModel.BottomUp);
    assertNull(((CaptureModel.BottomUp)details).getModel());

    // Capture has changed, keeps the same type of details
    CpuCapture capture = new CpuCapture(CpuCaptureTest.readValidTrace());
    myStage.setAndSelectCapture(capture);
    CaptureModel.Details newDetails = myStage.getCaptureDetails();
    assertNotEquals(details, newDetails);
    assertTrue(newDetails instanceof CaptureModel.BottomUp);
    assertNotNull(((CaptureModel.BottomUp)newDetails).getModel());
  }

  @Test
  public void setCaptureShouldChangeDetails() throws Exception {
    // Capture a trace
    myCpuService.setTraceId(0);
    captureSuccessfully();

    AspectObserver observer = new AspectObserver();
    myStage.getAspect().addDependency(observer).onChange(CpuProfilerAspect.CAPTURE_DETAILS, () -> myCaptureDetailsCalled = true);

    myCaptureDetailsCalled = false;
    // Capture another trace
    myCpuService.setTraceId(1);
    captureSuccessfully();

    assertNotNull(myStage.getCapture());
    assertEquals(myStage.getCapture(1), myStage.getCapture());
    assertTrue(myCaptureDetailsCalled);
  }

  @Test
  public void setSelectedThreadShouldChangeDetails() throws Exception {
    captureSuccessfully();

    AspectObserver observer = new AspectObserver();
    myStage.getAspect().addDependency(observer).onChange(CpuProfilerAspect.CAPTURE_DETAILS, () -> myCaptureDetailsCalled = true);

    myCaptureDetailsCalled = false;
    myStage.setSelectedThread(42);

    assertEquals(42, myStage.getSelectedThread());
    assertTrue(myCaptureDetailsCalled);
  }

  @Test
  public void settingTheSameThreadDoesNothing() throws Exception {
    myCpuService.setTraceId(0);
    captureSuccessfully();

    AspectObserver observer = new AspectObserver();
    myStage.getAspect().addDependency(observer).onChange(CpuProfilerAspect.CAPTURE_DETAILS, () -> myCaptureDetailsCalled = true);

    myCaptureDetailsCalled = false;
    myStage.setSelectedThread(42);
    assertTrue(myCaptureDetailsCalled);

    myCaptureDetailsCalled = false;
    // Thread id is the same as the current selected thread, so it should do nothing
    myStage.setSelectedThread(42);
    assertFalse(myCaptureDetailsCalled);
  }

  @Test
  public void settingTheSameDetailsTypeDoesNothing() throws Exception {
    myCpuService.setTraceId(0);
    captureSuccessfully();

    AspectObserver observer = new AspectObserver();
    myStage.getAspect().addDependency(observer).onChange(CpuProfilerAspect.CAPTURE_DETAILS, () -> myCaptureDetailsCalled = true);
    assertEquals(CaptureModel.Details.Type.CALL_CHART, myStage.getCaptureDetails().getType());

    myCaptureDetailsCalled = false;
    // The first time we set it to bottom up, CAPTURE_DETAILS should be fired
    myStage.setCaptureDetails(CaptureModel.Details.Type.BOTTOM_UP);
    assertTrue(myCaptureDetailsCalled);

    myCaptureDetailsCalled = false;
    // If we call it again for bottom up, we shouldn't fire CAPTURE_DETAILS
    myStage.setCaptureDetails(CaptureModel.Details.Type.BOTTOM_UP);
    assertFalse(myCaptureDetailsCalled);
  }

  @Test
  public void callChartShouldBeSetAfterACapture() throws Exception {
    captureSuccessfully();
    assertEquals(CaptureModel.Details.Type.CALL_CHART, myStage.getCaptureDetails().getType());

    // Change details type and verify it was actually changed.
    myStage.setCaptureDetails(CaptureModel.Details.Type.BOTTOM_UP);
    assertEquals(CaptureModel.Details.Type.BOTTOM_UP, myStage.getCaptureDetails().getType());

    CpuCapture capture = new CpuCapture(CpuCaptureTest.readValidTrace());
    myStage.setAndSelectCapture(capture);
    // Just selecting a different capture shouldn't change the capture details
    assertEquals(CaptureModel.Details.Type.BOTTOM_UP, myStage.getCaptureDetails().getType());

    captureSuccessfully();
    // Capturing again should set the details to call chart
    assertEquals(CaptureModel.Details.Type.CALL_CHART, myStage.getCaptureDetails().getType());
  }

  @Test
  public void profilerReturnsToNormalModeAfterNavigatingToCode() throws IOException {
    // We need to be on the stage itself or else we won't be listening to code navigation events
    myStage.getStudioProfilers().setStage(myStage);

    // to EXPANDED mode
    assertEquals(ProfilerMode.NORMAL, myStage.getProfilerMode());
    myStage.setAndSelectCapture(new CpuCapture(CpuCaptureTest.readValidTrace()));
    assertEquals(ProfilerMode.EXPANDED, myStage.getProfilerMode());
    // After code navigation it should be Normal mode.
    myStage.getStudioProfilers().getIdeServices().getCodeNavigator().navigate(CodeLocation.stub());
    assertEquals(ProfilerMode.NORMAL, myStage.getProfilerMode());

    myStage.setCapture(new CpuCapture(CpuCaptureTest.readValidTrace()));
    assertEquals(ProfilerMode.EXPANDED, myStage.getProfilerMode());
  }

  @Test
  public void captureStateDependsOnAppBeingProfiling() {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
    myCpuService.setStartProfilingStatus(CpuProfiler.CpuProfilingAppStartResponse.Status.SUCCESS);
    startCapturing();
    assertEquals(CpuProfilerStage.CaptureState.CAPTURING, myStage.getCaptureState());
    myCpuService.setStopProfilingStatus(CpuProfiler.CpuProfilingAppStopResponse.Status.SUCCESS);
    stopCapturing();
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
  }

  @Test
  public void setAndSelectCaptureDifferentClockType() throws IOException {
    CpuCapture capture = new CpuCapture(CpuCaptureTest.readValidTrace());
    CaptureNode captureNode = capture.getCaptureNode(capture.getMainThreadId());
    assertNotNull(captureNode);
    myStage.setSelectedThread(capture.getMainThreadId());

    assertEquals(ClockType.GLOBAL, captureNode.getClockType());
    myStage.setAndSelectCapture(capture);
    ProfilerTimeline timeline = myStage.getStudioProfilers().getTimeline();
    double eps = 0.00001;
    // In GLOBAL clock type, selection should be the main node range
    assertEquals(timeline.getSelectionRange().getMin(), capture.getRange().getMin(), eps);
    assertEquals(timeline.getSelectionRange().getMax(), capture.getRange().getMax(), eps);

    timeline.getSelectionRange().set(captureNode.getStartGlobal(), captureNode.getEndGlobal());
    myStage.setClockType(ClockType.THREAD);
    assertEquals(ClockType.THREAD, captureNode.getClockType());
    myStage.setCapture(capture);
    // In THREAD clock type, selection should scale the interval based on thread-clock/wall-clock ratio [node's startTime, node's endTime].
    double threadToGlobal = 1 / captureNode.threadGlobalRatio();
    double threadSelectionStart = captureNode.getStartGlobal() +
                                  threadToGlobal * (captureNode.getStartThread() - timeline.getSelectionRange().getMin());
    double threadSelectionEnd = threadSelectionStart +
                                threadToGlobal * captureNode.duration();
    assertEquals(timeline.getSelectionRange().getMin(), threadSelectionStart, eps);
    assertEquals(timeline.getSelectionRange().getMax(), threadSelectionEnd, eps);

    myStage.setClockType(ClockType.GLOBAL);
    assertEquals(ClockType.GLOBAL, captureNode.getClockType());
    // Just setting the clock type shouldn't change the selection range
    assertEquals(timeline.getSelectionRange().getMin(), threadSelectionStart, eps);
    assertEquals(timeline.getSelectionRange().getMax(), threadSelectionEnd, eps);
  }

  @Test
  public void testCaptureRangeConversion() throws Exception {
    captureSuccessfully();

    myStage.setSelectedThread(myStage.getCapture().getMainThreadId());
    myStage.setCaptureDetails(CaptureModel.Details.Type.BOTTOM_UP);

    Range selection = myStage.getStudioProfilers().getTimeline().getSelectionRange();
    double eps = 1e-5;
    assertEquals(myStage.getCapture().getRange().getMin(), selection.getMin(), eps);
    assertEquals(myStage.getCapture().getRange().getMax(), selection.getMax(), eps);

    assertTrue(myStage.getCaptureDetails() instanceof CaptureModel.BottomUp);
    CaptureModel.BottomUp details = (CaptureModel.BottomUp)myStage.getCaptureDetails();

    Range detailsRange = details.getModel().getRange();

    // When ClockType.Global is used, the range of a capture details should the same as the selection range
    assertEquals(ClockType.GLOBAL, myStage.getClockType());
    assertEquals(detailsRange.getMin(), selection.getMin(), eps);
    assertEquals(detailsRange.getMax(), selection.getMax(), eps);

    detailsRange.set(0, 10);
    assertEquals(selection.getMin(), 0, eps);
    assertEquals(selection.getMax(), 10, eps);

    selection.set(1, 5);
    assertEquals(detailsRange.getMin(), 1, eps);
    assertEquals(detailsRange.getMax(), 5, eps);
  }

  @Test
  public void settingACaptureAfterNullShouldSelectMainThread() throws Exception {
    assertEquals(CaptureModel.INVALID_THREAD, myStage.getSelectedThread());
    assertNull(myStage.getCapture());
    assertEquals(ProfilerMode.NORMAL, myStage.getProfilerMode());

    CpuCapture capture = new CpuCapture(CpuCaptureTest.readValidTrace());
    assertNotNull(capture);
    myStage.setAndSelectCapture(capture);
    assertEquals(ProfilerMode.EXPANDED, myStage.getProfilerMode());
    // Capture main thread should be selected
    assertEquals(capture.getMainThreadId(), myStage.getSelectedThread());

    myStage.setAndSelectCapture(null);
    assertEquals(ProfilerMode.NORMAL, myStage.getProfilerMode());
    // Thread selection is reset when going to NORMAL mode
    assertEquals(CaptureModel.INVALID_THREAD, myStage.getSelectedThread());
  }

  @Test
  public void changingCaptureShouldKeepThreadSelection() throws Exception {
    CpuCapture capture1 = new CpuCapture(CpuCaptureTest.readValidTrace());
    CpuCapture capture2 = new CpuCapture(CpuCaptureTest.readValidTrace());
    assertNotEquals(capture1, capture2);

    myStage.setAndSelectCapture(capture1);
    // Capture main thread should be selected
    int mainThread = capture1.getMainThreadId();
    assertEquals(mainThread, myStage.getSelectedThread());

    int otherThread = mainThread;
    // Select a thread other than main
    for (ThreadInfo thread : capture1.getThreads()) {
      if (thread.getId() != mainThread) {
        otherThread = thread.getId();
        break;
      }
    }

    assertNotEquals(otherThread, mainThread);
    myStage.setSelectedThread(otherThread);
    assertEquals(otherThread, myStage.getSelectedThread());

    myStage.setAndSelectCapture(capture2);
    assertEquals(capture2, myStage.getCapture());
    // Thread selection should be kept instead of selecting capture2 main thread.
    assertEquals(otherThread, myStage.getSelectedThread());
  }

  @Test
  public void testTooltipLegends() {
    myStage.enter();
    CpuProfilerStage.CpuStageLegends legends = myStage.getTooltipLegends();
    double tooltipTime = TimeUnit.SECONDS.toMicros(0);
    myCpuService.setAppTimeMs(10);
    myCpuService.setSystemTimeMs(50);
    myStage.getStudioProfilers().getTimeline().getTooltipRange().set(tooltipTime, tooltipTime);
    assertEquals("App", legends.getCpuLegend().getName());
    assertEquals("Others", legends.getOthersLegend().getName());
    assertEquals("Threads", legends.getThreadsLegend().getName());
    assertEquals("10%", legends.getCpuLegend().getValue());
    assertEquals("40%", legends.getOthersLegend().getValue());
    assertEquals("1", legends.getThreadsLegend().getValue());
  }

  @Test
  public void testElapsedTime() throws InterruptedException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
    // When there is no capture in progress, elapsed time is set to Long.MAX_VALUE.
    // As a result CpuProfilerStage#getCaptureElapsedTimeUs should return a negative value.
    assertTrue(myStage.getCaptureElapsedTimeUs() < 0);

    // Start capturing
    myCpuService.setStartProfilingStatus(CpuProfiler.CpuProfilingAppStartResponse.Status.SUCCESS);
    myStage.startCapturing();
    // Increment 3 seconds on data range
    Range dataRange = myStage.getStudioProfilers().getTimeline().getDataRange();
    dataRange.setMax(dataRange.getMax() + TimeUnit.SECONDS.toMicros(3));
    assertEquals(CpuProfilerStage.CaptureState.CAPTURING, myStage.getCaptureState());

    // Check that we're capturing for three seconds
    assertEquals(TimeUnit.SECONDS.toMicros(3), myStage.getCaptureElapsedTimeUs());

    stopCapturing();
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
    // Capture has finished. CpuProfilerStage#getCaptureElapsedTimeUs should return a negative value.
    assertTrue(myStage.getCaptureElapsedTimeUs() < 0);
  }

  @Test
  public void profilingModesAvailableDependOnDeviceApi() {
    myServices.enableSimplePerf(true);

    // Set a device that doesn't support simpleperf
    addAndSetDevice(14, "FakeDevice1");

    List<ProfilingConfiguration> configs = myStage.getProfilingConfigurations();
    assertEquals(3, configs.size());
    // First configuration in the list should be a dummy entry used to open the configurations dialog
    assertEquals(CpuProfilerStage.EDIT_CONFIGURATIONS_ENTRY, configs.get(0));
    // First actual configuration should be ART Sampled
    assertEquals(CpuProfiler.CpuProfilerType.ART, configs.get(1).getProfilerType());
    assertEquals(CpuProfiler.CpuProfilingAppStartRequest.Mode.SAMPLED, configs.get(1).getMode());
    assertEquals("Sampled", configs.get(1).getName());
    // Second actual configuration should be ART Instrumented
    assertEquals(CpuProfiler.CpuProfilerType.ART, configs.get(2).getProfilerType());
    assertEquals(CpuProfiler.CpuProfilingAppStartRequest.Mode.INSTRUMENTED, configs.get(2).getMode());
    assertEquals("Instrumented", configs.get(2).getName());

    // Simpleperf is supported on API 26 and greater.
    addAndSetDevice(26, "FakeDevice2");

    configs = myStage.getProfilingConfigurations();
    assertEquals(4, configs.size());
    // Dummy configuration
    assertEquals(CpuProfilerStage.EDIT_CONFIGURATIONS_ENTRY, configs.get(0));
    // First and second actual configurations should be the same
    assertEquals("Sampled", configs.get(1).getName());
    assertEquals("Instrumented", configs.get(2).getName());
    // Third configuration should be simpleperf
    assertEquals(CpuProfiler.CpuProfilerType.SIMPLE_PERF, configs.get(3).getProfilerType());
    assertEquals(CpuProfiler.CpuProfilingAppStartRequest.Mode.SAMPLED, configs.get(3).getMode());
    assertEquals("Sampled (Hybrid)", configs.get(3).getName());
  }

  @Test
  public void simpleperfIsOnlyAvailableWhenFlagIsTrue() {
    myServices.enableSimplePerf(true);

    // Set a device that supports simpleperf
    addAndSetDevice(26, "Fake Device 1");

    List<ProfilingConfiguration> configs = myStage.getProfilingConfigurations();
    assertEquals(4, configs.size());
    // First configuration in the list should be a dummy entry used to open the configurations dialog
    assertEquals(CpuProfilerStage.EDIT_CONFIGURATIONS_ENTRY, configs.get(0));
    // First and second actual configurations should be the same
    assertEquals("Sampled", configs.get(1).getName());
    assertEquals("Instrumented", configs.get(2).getName());
    // Third actual configuration should be simpleperf
    assertEquals(CpuProfiler.CpuProfilerType.SIMPLE_PERF, configs.get(3).getProfilerType());
    assertEquals(CpuProfiler.CpuProfilingAppStartRequest.Mode.SAMPLED, configs.get(3).getMode());
    assertEquals("Sampled (Hybrid)", configs.get(3).getName());

    // Now disable simpleperf
    myServices.enableSimplePerf(false);

    // Set a device that supports simpleperf
    addAndSetDevice(26, "Fake Device 2");
    configs = myStage.getProfilingConfigurations();
    // Simpleperf should not be listed as a profiling option
    assertEquals(3, configs.size());
    // Check for the dummy config in the list
    assertEquals(CpuProfilerStage.EDIT_CONFIGURATIONS_ENTRY, configs.get(0));
    // First and second actual configurations should be the ART ones
    assertEquals("Sampled", configs.get(1).getName());
    assertEquals("Instrumented", configs.get(2).getName());
  }

  @Test
  public void editConfigurationsEntryCantBeSetAsProfilingConfiguration() {
    assertNotNull(myStage.getProfilingConfiguration());
    // ART Sampled should be the default configuration when starting the stage,
    // as it's the first configuration on the list.
    assertEquals("Sampled", myStage.getProfilingConfiguration().getName());

    // Set a new configuration and check it's actually set as stage's profiling configuration
    ProfilingConfiguration instrumented = new ProfilingConfiguration("Instrumented",
                                                                     CpuProfiler.CpuProfilerType.ART,
                                                                     CpuProfiler.CpuProfilingAppStartRequest.Mode.INSTRUMENTED);
    myStage.setProfilingConfiguration(instrumented);
    assertEquals("Instrumented", myStage.getProfilingConfiguration().getName());

    // Set CpuProfilerStage.EDIT_CONFIGURATIONS_ENTRY as profiling configuration
    // and check it doesn't actually replace the current configuration
    myStage.setProfilingConfiguration(CpuProfilerStage.EDIT_CONFIGURATIONS_ENTRY);
    assertEquals("Instrumented", myStage.getProfilingConfiguration().getName());

    // Just sanity check "Instrumented" is not the name of CpuProfilerStage.EDIT_CONFIGURATIONS_ENTRY
    assertNotEquals("Instrumented", CpuProfilerStage.EDIT_CONFIGURATIONS_ENTRY.getName());
  }

  @Test
  public void stopProfilerIsConsistentToStartProfiler() throws InterruptedException {
    assertNull(myCpuService.getProfilerType());
    ProfilingConfiguration config1 = new ProfilingConfiguration("My Config",
                                                                CpuProfiler.CpuProfilerType.SIMPLE_PERF,
                                                                CpuProfiler.CpuProfilingAppStartRequest.Mode.SAMPLED);
    myStage.setProfilingConfiguration(config1);
    captureSuccessfully();
    assertEquals(CpuProfiler.CpuProfilerType.SIMPLE_PERF, myCpuService.getProfilerType());

    ProfilingConfiguration config2 = new ProfilingConfiguration("My Config 2",
                                                                CpuProfiler.CpuProfilerType.ART,
                                                                CpuProfiler.CpuProfilingAppStartRequest.Mode.SAMPLED);
    myStage.setProfilingConfiguration(config2);
    // Start capturing with ART
    startCapturingSuccess();
    // Change the profiling configurations in the middle of the capture and stop capturing
    myStage.setProfilingConfiguration(config1);
    stopCapturing();
    // Stop profiler should be the same as the one passed in the start request
    assertEquals(CpuProfiler.CpuProfilerType.ART, myCpuService.getProfilerType());
  }

  @Test
  public void exitingStateAndEnteringAgainShouldPreserveCaptureState() {
    assertNull(myCpuService.getProfilerType());
    ProfilingConfiguration config1 = new ProfilingConfiguration("My Config",
                                                                CpuProfiler.CpuProfilerType.SIMPLE_PERF,
                                                                CpuProfiler.CpuProfilingAppStartRequest.Mode.SAMPLED);
    myStage.setProfilingConfiguration(config1);
    myCpuService.setStartProfilingStatus(CpuProfiler.CpuProfilingAppStartResponse.Status.SUCCESS);
    startCapturing();

    // Go back to monitor stage and go back to a new Cpu profiler stage
    myStage.getStudioProfilers().setStage(new StudioMonitorStage(myStage.getStudioProfilers()));
    CpuProfilerStage stage = new CpuProfilerStage(myStage.getStudioProfilers());
    myStage.getStudioProfilers().setStage(stage);

    // Make sure we're capturing
    assertEquals(CpuProfilerStage.CaptureState.CAPTURING, stage.getCaptureState());

    myCpuService.setStopProfilingStatus(CpuProfiler.CpuProfilingAppStopResponse.Status.SUCCESS);
    stopCapturing(stage);
    assertEquals(CpuProfilerStage.CaptureState.IDLE, stage.getCaptureState());

    // Stop profiler should be the same as the one passed in the start request
    assertEquals(CpuProfiler.CpuProfilerType.SIMPLE_PERF, myCpuService.getProfilerType());
  }

  private void addAndSetDevice(int featureLevel, String serial) {
    Profiler.Device device =
      Profiler.Device.newBuilder().setFeatureLevel(featureLevel).setSerial(serial).setState(Profiler.Device.State.ONLINE).build();
    Profiler.Process process = Profiler.Process.newBuilder()
      .setPid(20)
      .setState(Profiler.Process.State.ALIVE)
      .setName("FakeProcess")
      .build();
    Common.Session session = Common.Session.newBuilder()
      .setBootId(device.getBootId())
      .setDeviceSerial(device.getSerial())
      .build();
    myProfilerService.addDevice(device);
    // Adds at least one ALIVE process as well. Otherwise, StudioProfilers would prefer selecting a device that has live processes.
    myProfilerService.addProcess(session, process);

    myTimer.tick(FakeTimer.ONE_SECOND_IN_NS); // One second must be enough for new device to be picked up
    myStage.getStudioProfilers().setDevice(device);
    // Setting the device will change the stage. We need to go back to CpuProfilerStage
    myStage.getStudioProfilers().setStage(myStage);
  }

  private void captureSuccessfully() throws InterruptedException {
    // Start a successful capture
    startCapturingSuccess();

    // Stop a capture successfully with a valid trace
    myServices.setOnExecute(() -> {
      // First, the main executor is going to be called to execute stopCapturingCallback,
      // which should set the capture state to PARSING
      assertEquals(CpuProfilerStage.CaptureState.PARSING, myStage.getCaptureState());
      // Then, the next time the main executor is called, it will parse the capture successfully
      // and set the capture state to IDLE
      myServices.setOnExecute(() -> {
        assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
        assertNotNull(myStage.getCapture());
      });
    });
    myCpuService.setStopProfilingStatus(CpuProfiler.CpuProfilingAppStopResponse.Status.SUCCESS);
    myCpuService.setValidTrace(true);
    myCpuService.setGetTraceResponseStatus(CpuProfiler.GetTraceResponse.Status.SUCCESS);
    stopCapturing();
  }

  private void startCapturingSuccess() throws InterruptedException {
    assertEquals(CpuProfilerStage.CaptureState.IDLE, myStage.getCaptureState());
    myCpuService.setStartProfilingStatus(CpuProfiler.CpuProfilingAppStartResponse.Status.SUCCESS);
    myServices.setPrePoolExecutor(() -> assertEquals(CpuProfilerStage.CaptureState.STARTING, myStage.getCaptureState()));
    startCapturing();
    assertEquals(CpuProfilerStage.CaptureState.CAPTURING, myStage.getCaptureState());
  }

  private void startCapturing() {
    myServices.setPrePoolExecutor(() -> assertEquals(CpuProfilerStage.CaptureState.STARTING, myStage.getCaptureState()));
    myStage.startCapturing();
  }

  private void stopCapturing(CpuProfilerStage stage) {
    // The pre executor will pass through STOPPING and then PARSING
    myServices.setPrePoolExecutor(() -> {
      assertEquals(CpuProfilerStage.CaptureState.STOPPING, stage.getCaptureState());
      myServices.setPrePoolExecutor(() -> assertEquals(CpuProfilerStage.CaptureState.PARSING, stage.getCaptureState()));
    });
    stage.stopCapturing();
  }

  private void stopCapturing() {
    stopCapturing(myStage);
  }
}
