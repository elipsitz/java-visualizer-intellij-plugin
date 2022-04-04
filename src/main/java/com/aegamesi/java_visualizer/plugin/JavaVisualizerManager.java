package com.aegamesi.java_visualizer.plugin;

import com.aegamesi.java_visualizer.backend.Tracer;
import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.SuspendContext;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import com.sun.jdi.ThreadReference;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.AncestorEvent;

public class JavaVisualizerManager implements XDebugSessionListener {
	private static final String CONTENT_ID = "aegamesi.JavaVisualizerContent2";

	static final String PROPERTY_KEY_BASE = "java_visualizer.";
	static final String KEY_ZOOM = PROPERTY_KEY_BASE + "zoom";

	private XDebugSession debugSession;
	private Content content;
	private MainPane panel;
	private Project project;

	JavaVisualizerManager(Project project, XDebugProcess debugProcess) {
		this.project = project;
		this.debugSession = debugProcess.getSession();
		this.content = null;

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
		panel = new MainPane();
		panel.addAncestorListener(new AncestorListenerAdapter() {
			public void ancestorAdded(AncestorEvent event) {
				forceRefreshVisualizer();
			}
		});

		SimpleToolWindowPanel container = new SimpleToolWindowPanel(false, true);
		final ActionManager actionManager = ActionManager.getInstance();
		ActionToolbar actionToolbar = actionManager.createActionToolbar(
				"JavaVisualizer.VisualizerToolbar",
				(DefaultActionGroup) actionManager.getAction("JavaVisualizer.VisualizerToolbar"),
				false
		);
		actionToolbar.setTargetComponent(panel);
		container.setToolbar(actionToolbar.getComponent());
		container.setContent(panel);

		RunnerLayoutUi ui = debugSession.getUI();
		content = ui.createContent(
				CONTENT_ID,
				container,
				"Java Visualizer",
				IconLoader.getIcon("/icons/viz.png", JavaVisualizerManager.class),
				null);
		content.setCloseable(false);
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
			if (p != null) {
				p.getManagerThread().invokeCommand(new DebuggerCommand() {
					@Override
					public void action() {
						traceAndVisualize();
					}

					@Override
					public void commandCancelled() {
					}
				});
			}
		} catch (Exception e) {
			System.out.println("unable to force refresh visualizer: " + e);
		}
	}

	private void traceAndVisualize() {
		try {
			SuspendContext sc = (SuspendContext) debugSession.getSuspendContext();
			if (sc == null || sc.getThread() == null) {
				return;
			}
			ThreadReference thread = sc.getThread().getThreadReference();

			Tracer t = new Tracer(thread);
			panel.setTrace(t.getModel());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
