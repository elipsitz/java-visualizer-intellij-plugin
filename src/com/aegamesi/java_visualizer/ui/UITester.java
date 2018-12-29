package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.ui.JVPanel;

import javax.swing.JFrame;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UITester {
	public static void main(String[] args) throws Exception {
		int a = 123;
		String b = "hello!";
		List<String> c = new ArrayList<>(Arrays.asList("a", "b", "c"));

		// toy above

		JVPanel panel = new JVPanel();
		ExecutionTrace trace = makeTrace();
		panel.setTrace(trace);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(800, 600);
		f.getContentPane().add(panel);
		f.setVisible(true);
	}

	public static ExecutionTrace makeTrace() throws Exception {
		ObjectInputStream o = new ObjectInputStream(new FileInputStream("/tmp/jv.ser"));
		ExecutionTrace e = (ExecutionTrace) o.readObject();
		o.close();
		return e;
	}
}
