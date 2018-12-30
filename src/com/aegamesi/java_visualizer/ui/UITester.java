package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
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

		VisualizationPanel panel = new VisualizationPanel();
		ExecutionTrace trace = makeTrace();
		panel.setTrace(trace);

		JScrollPane scrollPane = new JBScrollPane(panel);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(800, 600);
		f.getContentPane().add(scrollPane);
		f.setVisible(true);
	}

	private static ExecutionTrace makeTrace() throws Exception {
		ObjectInputStream o = new ObjectInputStream(new FileInputStream("/tmp/jv.ser"));
		ExecutionTrace e = (ExecutionTrace) o.readObject();
		o.close();
		return e;
	}
}
