package com.aegamesi.java_visualizer.plugin;

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
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.jetbrains.annotations.NotNull;
import traceprinter.JDI2JSON;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.util.ArrayList;

public class JavaVisualizerManager implements XDebugSessionListener {
	private static final String CONTENT_ID = "61B.JavaVisualizerContent2";

	private XDebugProcess debugProcess;
	private XDebugSession debugSession;
	private Content content;
	private JComponent component;
	private WebView webView;
	private boolean webViewReady;
	private JDI2JSON jdi2json;
	private Project project;

	public JavaVisualizerManager(Project project, XDebugProcess process) {
		this.project = project;
		this.debugProcess = process;
		this.debugSession = process.getSession();
		this.content = null;
		this.jdi2json = new JDI2JSON();
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

	private void initializeComponent() {
		final JFXPanel jfxPanel = new JFXPanel();
		Platform.setImplicitExit(false);
		Platform.runLater(() -> {
			webView = new WebView();
			webView.getEngine().setOnStatusChanged((WebEvent<String> e) -> {
				if (e != null && e.getData() != null && e.getData().equals("Ready")) {
					webViewReady = true;
				}
			});
			webView.getEngine().load(getClass().getResource("/web/visualizer.html").toExternalForm());
			jfxPanel.setScene(new Scene(webView));
		});

		jfxPanel.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				forceRefreshVisualizer();
			}

			public void ancestorRemoved(AncestorEvent event) {
			}

			public void ancestorMoved(AncestorEvent event) {
			}
		});
		component = jfxPanel;
	}

	private void initializeContent() {
		initializeComponent();
		RunnerLayoutUi ui = debugSession.getUI();
		content = ui.createContent(
				CONTENT_ID,
				component,
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
			if (component.isShowing()) {
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

			ArrayList<JsonObject> objs = jdi2json.convertExecutionPoint(reference);
			if (objs.size() > 0) {
				JsonArrayBuilder arr = Json.createArrayBuilder();
				objs.forEach(arr::add);
				String lastTrace = arr.build().toString();

				Platform.runLater(() -> {
					if (webView != null && webViewReady) {
						JSObject window = (JSObject) webView.getEngine().executeScript("window");
						window.call("visualize", lastTrace);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
