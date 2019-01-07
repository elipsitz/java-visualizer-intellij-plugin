package com.aegamesi.java_visualizer.plugin;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.ui.VisualizationPanel;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

class MainPane extends JPanel {
	private JLabel placeholderLabel;
	private VisualizationPanel viz;

	MainPane() {
		setLayout(new BorderLayout());

		String text = "No execution trace loaded: make sure you've stopped on a breakpoint.";
		placeholderLabel = new JLabel(text, SwingConstants.CENTER);
		add(placeholderLabel);
	}

	void setTrace(ExecutionTrace trace) {
		if (viz == null) {
			remove(placeholderLabel);
			viz = new VisualizationPanel();
			JBScrollPane scrollPane = new JBScrollPane(viz);
			scrollPane.setBorder(null);
			add(scrollPane);
			revalidate();
		}
		viz.setTrace(trace);
	}
}
