package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UITester {
	public static void main(String[] args) throws Exception {
		int a = 123;
		String b = "hello!";
		List<String> c = new ArrayList<>(Arrays.asList("a", "b", "c"));
		Map<String, String> map = new HashMap<>();
		map.put("abc", "def");
		map.put("123", "456");
		map.put("loooong key", "sval");
		map.put("skey", "loooooooong val");

		// toy above

		ExecutionTrace trace = makeTrace();
		System.out.println(trace.toJsonString());
		VisualizationPanel panel = new VisualizationPanel();
		panel.setTrace(trace);

		JScrollPane scrollPane = new JBScrollPane(panel);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(800, 600);
		f.getContentPane().add(scrollPane);
		f.setVisible(true);
	}

	private static ExecutionTrace makeTrace() throws Exception {
		String json = new String(Files.readAllBytes(Paths.get("/Users/eli/Desktop/trace.json")));
		return ExecutionTrace.fromJsonString(json);
	}
}
