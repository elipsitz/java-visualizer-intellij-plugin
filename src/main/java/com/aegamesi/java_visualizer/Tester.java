package com.aegamesi.java_visualizer;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.ui.VisualizationPanel;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tester {
	private static final String tracePath = "/Users/eli/Desktop/trace.json";

	public static void main(String[] args) throws Exception {
		ExecutionTrace trace = makeTrace(tracePath);
		VisualizationPanel panel = new VisualizationPanel();
		panel.setTrace(trace);

		JScrollPane scrollPane = new JScrollPane(panel);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(scrollPane);
		f.setVisible(true);
		f.pack();
	}

	private static ExecutionTrace makeTrace(String path) throws Exception {
		String json = new String(Files.readAllBytes(Paths.get(path)));
		return ExecutionTrace.fromJsonString(json);
	}
}
