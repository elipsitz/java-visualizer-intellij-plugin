package com.aegamesi.java_visualizer.plugin;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerManagerListener;
import org.jetbrains.annotations.NotNull;

public class JavaVisualizerComponent implements ProjectComponent {
	private Project project;
	private GoogleAnalytics ga;

	public JavaVisualizerComponent(Project project) {
		this.project = project;

		this.ga = GoogleAnalytics.builder()
				.withTrackingId("UA-131767395-1")
				.build();
		ga.getConfig().setDiscoverRequestParameters(true);

		MessageBusConnection conn = project.getMessageBus().connect();
		conn.subscribe(XDebuggerManager.TOPIC, new XDebuggerManagerListener() {
			@Override
			public void processStarted(@NotNull XDebugProcess xDebugProcess) {
				new JavaVisualizerManager(project, xDebugProcess, ga);
			}
		});
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "Java Visualizer";
	}
}
