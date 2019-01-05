package com.aegamesi.java_visualizer;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.ui.VisualizationPanel;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Tester {
	public static void main(String[] args) throws Exception {
		ExecutionTrace trace = makeTrace();
		VisualizationPanel panel = new VisualizationPanel();
		panel.setTrace(trace);

		JScrollPane scrollPane = new JScrollPane(panel);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(scrollPane);
		f.setVisible(true);
		f.pack();
	}

	private static ExecutionTrace makeTrace() throws Exception {
		InputStream is = Tester.class.getResourceAsStream("/sample_trace.json");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
		String json = new String(os.toByteArray());
		return ExecutionTrace.fromJsonString(json);
	}
}
