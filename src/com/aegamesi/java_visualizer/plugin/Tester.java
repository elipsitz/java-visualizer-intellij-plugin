package com.aegamesi.java_visualizer.plugin;

import com.aegamesi.java_visualizer.model.ExecutionTrace;

import javax.swing.JFrame;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tester {
	public static void main(String[] args) throws Exception {
		int a = 123;
		String b = "hello!";
		List<String> c = new ArrayList<>(Arrays.asList("a", "b", "c"));

		// toy above

		JVPanel panel = new JVPanel();

		JFrame f = new JFrame();
		f.setContentPane(panel);
		f.setSize(800, 600);
		f.setVisible(true);

		ExecutionTrace trace = makeTrace();
		panel.setTrace(trace);
	}

	public static ExecutionTrace makeTrace() throws Exception {
		ObjectInputStream o = new ObjectInputStream(new FileInputStream("/tmp/jv.ser"));
		ExecutionTrace e = (ExecutionTrace) o.readObject();
		o.close();
		return e;
	}
}
