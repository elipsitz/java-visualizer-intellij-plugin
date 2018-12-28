package com.aegamesi.java_visualizer.plugin;

import com.aegamesi.java_visualizer.backend.Tracer;
import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.SuspendContext;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import com.sun.jdi.ThreadReference;
import org.jetbrains.annotations.NotNull;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class JavaVisualizerManager implements XDebugSessionListener {
	private static final String CONTENT_ID = "aegamesi.JavaVisualizerContent2";

	private XDebugProcess debugProcess;
	private XDebugSession debugSession;
	private Content content;
	private JVPanel panel;
	private Project project;

	public JavaVisualizerManager(Project project, XDebugProcess process) {
		this.project = project;
		this.debugProcess = process;
		this.debugSession = process.getSession();
		this.content = null;
	}

	public void attach() {
		debugProcess.getProcessHandler().addProcessListener(new ProcessListener() {
			@Override
			public void startNotified(@NotNull ProcessEvent processEvent) {
				initializeContent();
			}

			@Override
			public void processTerminated(@NotNull ProcessEvent processEvent) {
			}

			@Override
			public void processWillTerminate(@NotNull ProcessEvent processEvent, boolean b) {
			}

			@Override
			public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {
			}
		});
		debugSession.addSessionListener(this);
	}


	private void initializeContent() {
		panel = new JVPanel();

		panel.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				forceRefreshVisualizer();
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		RunnerLayoutUi ui = debugSession.getUI();
		content = ui.createContent(
				CONTENT_ID,
				panel,
				"Java Visualizer",
				IconLoader.getIcon("/icons/jv.png"),
				null);
		UIUtil.invokeLaterIfNeeded(() -> ui.addContent(content));
	}

	@Override
	public void sessionPaused() {
		if (content == null) {
			initializeContent();
		}

		try {
			if (panel.isShowing()) {
				traceAndVisualize();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void forceRefreshVisualizer() {
		try {
			DebugProcess p = DebuggerManager.getInstance(project).getDebugProcess(debugSession.getDebugProcess().getProcessHandler());
			p.getManagerThread().invokeCommand(new DebuggerCommand() {
				@Override
				public void action() {
					traceAndVisualize();
				}

				@Override
				public void commandCancelled() {
				}
			});
		} catch (Exception e) {
			System.out.println("unable to force refresh visualizer: " + e);
		}
	}

	private void traceAndVisualize() {
		try {
			SuspendContext sc = (SuspendContext) debugSession.getSuspendContext();
			if (sc == null) {
				return;
			}
			ThreadReference reference = sc.getThread().getThreadReference();

			Tracer t = new Tracer(reference);
			ExecutionTrace model = t.getModel();

			/*ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream("/tmp/jv.ser"));
			o.writeObject(model);
			o.close();*/

			panel.setTrace(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
