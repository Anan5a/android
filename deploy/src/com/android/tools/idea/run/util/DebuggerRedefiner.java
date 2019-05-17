/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tools.idea.run.util;

import com.android.tools.deploy.proto.Deploy;
import com.android.tools.deployer.ClassRedefiner;
import com.android.tools.deployer.DeployerException;
import com.android.tools.deployer.JdiBasedClassRedefiner;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.JavaExecutionStack;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.DebuggerTask;
import com.intellij.debugger.impl.MultiProcessCommand;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.debugger.ui.breakpoints.BreakpointManager;
import com.intellij.debugger.ui.breakpoints.StackCapturingLineBreakpoint;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.xdebugger.XDebugSession;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import javax.swing.SwingUtilities;

/**
 * Helper class for the deploy task to deal with the interactions with the IntelliJ debugger.
 *
 * In particular, all the debugger interaction within IntelliJ happens in the main thread. This class takes care of queuing up debugger
 * task in that thread.
 */
public class DebuggerRedefiner implements ClassRedefiner {

  private final Project project;

  // This is the port that the IntelliJ talks to (port opended by ddmlib).
  private final int debuggerPort;

  private RedefineClassSupportState supportState = null;

  public DebuggerRedefiner(Project project, int debuggerPort) {
    this.project = project;
    this.debuggerPort = debuggerPort;
  }

  @Override
  public RedefineClassSupportState canRedefineClass() {
    if (supportState != null) {
      return supportState;
    }

    MultiProcessCommand commands = new MultiProcessCommand();
    Collection<DebuggerSession> debuggerSessions = DebuggerManagerEx.getInstanceEx(project).getSessions();
    List<DebuggerTask> tasks = new ArrayList<>(debuggerSessions.size());
    final AtomicReference<RedefineClassSupportState> result = new AtomicReference<>();

    for (DebuggerSession debuggerSession : debuggerSessions) {
      RemoteConnection s = debuggerSession.getProcess().getConnection();
      String address = s.getAddress();
      int projectDebuggerPort = Integer.parseInt(address);
      if (debuggerPort == projectDebuggerPort) {
        DebuggerCommandImpl command = new DebuggerCommandImpl() {
          @Override
          protected void action() {
            result.set(canRedefineClassInternal(debuggerSession));
          }
        };
        commands.addCommand(debuggerSession.getProcess(), command);
        tasks.add(command);
      }

    }
    commands.run();
    tasks.forEach(cmd -> cmd.waitFor());
    supportState = result.get();
    return supportState;
  }

  private RedefineClassSupportState canRedefineClassInternal(DebuggerSession debuggerSession) {
    // We use the IntelliJ abstraction of the debugger here since it is available.
    DebuggerManagerThreadImpl.assertIsManagerThread();
    DebugProcessImpl debugProcess = debuggerSession.getProcess();
    VirtualMachineProxyImpl virtualMachineProxy = debugProcess.getVirtualMachineProxy();

    // Simple case, debugger has the capability to all is good.
    if (virtualMachineProxy.canRedefineClasses()) {
      return new RedefineClassSupportState(RedefineClassSupport.FULL, null);
    }

    Collection<ThreadReferenceProxyImpl> allThreads = virtualMachineProxy.allThreads();

    // Prioritize on MAIN_THREAD_RUNNING since this is the safest option.
    for (ThreadReferenceProxyImpl thread : allThreads) {
      if (thread.name().equals("main")) {
        if (!thread.isSuspended()) {
          return new RedefineClassSupportState(RedefineClassSupport.MAIN_THREAD_RUNNING, "main");
        }
      }
    }

    // Just hope for a thread on a breakpoint after.
    for (ThreadReferenceProxyImpl thread : allThreads) {
      if (thread.isAtBreakpoint()) {
        RedefineClassSupportState state = new RedefineClassSupportState(RedefineClassSupport.NEEDS_AGENT_SERVER, thread.name());
        return state;
      }
    }

    return new RedefineClassSupportState(RedefineClassSupport.NONE, null);
  }

  @Override
  public Deploy.SwapResponse redefine(Deploy.SwapRequest request) throws DeployerException {
    MultiProcessCommand commands = new MultiProcessCommand();
    Collection<DebuggerSession> debuggerSessions = DebuggerManagerEx.getInstanceEx(project).getSessions();
    List<DebuggerTask> tasks = new ArrayList<>(debuggerSessions.size());

    if (debuggerSessions.isEmpty()) {
      return Deploy.SwapResponse.newBuilder().setStatus(Deploy.SwapResponse.Status.NO_DEBUGGER_SESSIONS).build();
    }

    // A bit of a hack. Exceptions posted to background tasks ends up on the log only. We are going to gather
    // as much of these as possible and present it to the user.
    final List<DeployerException> exceptions = Collections.synchronizedList(new LinkedList<>());
    for (DebuggerSession debuggerSession : debuggerSessions) {
      DebuggerCommandImpl command = new DebuggerCommandImpl() {
        @Override
        protected void action() {
          try {
            redefine(project, debuggerSession, request);
          }
          catch (DeployerException e) {
            exceptions.add(e);
          }
        }

        @Override
        protected void commandCancelled() {
          debuggerSession.setModifiedClassesScanRequired(true);
        }
      };
      commands.addCommand(debuggerSession.getProcess(), command);
      tasks.add(command);
    }
    commands.run();
    tasks.forEach(cmd -> cmd.waitFor());

    if (!exceptions.isEmpty()) {
      throw exceptions.get(0);
    }
    return Deploy.SwapResponse.newBuilder().setStatus(Deploy.SwapResponse.Status.OK).build();
  }

  private void redefine(Project project, DebuggerSession session, Deploy.SwapRequest request) throws DeployerException {
    try {
      disableBreakPoints(project, session);
      VirtualMachine vm = session.getProcess().getVirtualMachineProxy().getVirtualMachine();
      new JdiBasedClassRedefiner(vm, canRedefineClass()).redefine(request);
    } finally {
      enableBreakPoints(project, session);
    }
  }

  /**
   * @return True true if there is at least one debugger attached to a given project.
   */
  public static boolean hasDebuggersAttached(Project project) {
    return !DebuggerManagerEx.getInstanceEx(project).getSessions().isEmpty();
  }

  private static void disableBreakPoints(Project project, DebuggerSession debuggerSession) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    DebugProcessImpl debugProcess = debuggerSession.getProcess();
    BreakpointManager breakpointManager = (DebuggerManagerEx.getInstanceEx(project)).getBreakpointManager();
    breakpointManager.disableBreakpoints(debugProcess);
    StackCapturingLineBreakpoint.deleteAll(debugProcess);
    VirtualMachineProxyImpl virtualMachineProxy = debugProcess.getVirtualMachineProxy();

    if (Registry.is("debugger.resume.yourkit.threads")) {
      virtualMachineProxy.allThreads().stream()
                         .filter(ThreadReferenceProxyImpl::isResumeOnHotSwap)
                         .filter(ThreadReferenceProxyImpl::isSuspended)
                         .forEach(t -> IntStream.range(0, t.getSuspendCount()).forEach(i -> t.resume()));
    }
  }

  private static void enableBreakPoints(Project project, DebuggerSession debuggerSession) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    DebugProcessImpl debugProcess = debuggerSession.getProcess();
    BreakpointManager breakpointManager = (DebuggerManagerEx.getInstanceEx(project)).getBreakpointManager();

    debugProcess.onHotSwapFinished();

    DebuggerContextImpl context = debuggerSession.getContextManager().getContext();
    SuspendContextImpl suspendContext = context.getSuspendContext();
    if (suspendContext != null) {
      JavaExecutionStack stack = suspendContext.getActiveExecutionStack();
      if (stack != null) {
        stack.initTopFrame();
      }
    }

    final Semaphore waitSemaphore = new Semaphore();
    waitSemaphore.down();
    //noinspection SSBasedInspection
    SwingUtilities.invokeLater(() -> {
      try {
        if (!project.isDisposed()) {
          breakpointManager.reloadBreakpoints();
          debugProcess.getRequestsManager().clearWarnings();
          debuggerSession.refresh(false);

          XDebugSession session = debuggerSession.getXDebugSession();
          if (session != null) {
            session.rebuildViews();
          }
        }
      }
      finally {
        waitSemaphore.up();
      }
    });

    waitSemaphore.waitFor();

    if (!project.isDisposed()) {
      breakpointManager.enableBreakpoints(debugProcess);
      StackCapturingLineBreakpoint.createAll(debugProcess);
    }
  }
}
