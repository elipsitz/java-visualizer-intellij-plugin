package com.aegamesi.java_visualizer.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerManagerListener;
import org.jetbrains.annotations.NotNull;

public class JavaVisualizerComponent implements ProjectComponent {
	private Project project;

	public JavaVisualizerComponent(Project project) {
		this.project = project;

		MessageBusConnection conn = project.getMessageBus().connect();
		conn.subscribe(XDebuggerManager.TOPIC, new XDebuggerManagerListener() {
			@Override
			public void processStarted(@NotNull XDebugProcess xDebugProcess) {
				new JavaVisualizerManager(project, xDebugProcess);
			}
		});
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "Java Visualizer";
	}
}
