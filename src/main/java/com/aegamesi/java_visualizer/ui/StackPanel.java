package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.util.List;

public class StackPanel extends JPanel {
	public StackPanel(VisualizationPanel viz, List<Frame> callStack) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);

		for (int i = callStack.size() - 1; i >= 0; i -= 1) {
			Frame f = callStack.get(i);
			add(new StackFrameComponent(viz, f, i == 0));
			if (i > 0) {
				add(Box.createVerticalStrut(Constants.padStackVertical));
			}
		}
	}
}
