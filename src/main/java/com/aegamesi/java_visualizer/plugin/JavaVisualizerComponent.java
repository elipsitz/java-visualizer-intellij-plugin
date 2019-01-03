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
				attachToDebugger(xDebugProcess);
			}

			@Override
			public void processStopped(@NotNull XDebugProcess xDebugProcess) {
			}
		});
	}

	private void attachToDebugger(XDebugProcess xDebugProcess) {
		JavaVisualizerManager manager = new JavaVisualizerManager(project, xDebugProcess);
		manager.attach();
	}

	@Override
	public void projectOpened() {
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "Java Visualizer";
	}
}
