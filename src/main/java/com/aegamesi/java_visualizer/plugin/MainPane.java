package com.aegamesi.java_visualizer.plugin;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.ui.VisualizationPanel;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JPanel;
import java.awt.BorderLayout;

class MainPane extends JPanel {
	private VisualizationPanel viz;

	MainPane() {
		setLayout(new BorderLayout());

		viz = new VisualizationPanel();
		add(new JBScrollPane(viz));
	}

	void setTrace(ExecutionTrace trace) {
		viz.setTrace(trace);
	}
}
