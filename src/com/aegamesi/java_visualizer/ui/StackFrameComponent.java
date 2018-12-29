package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.Frame;
import com.aegamesi.java_visualizer.model.Value;
import com.intellij.util.ui.JBUI;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Map;

public class StackFrameComponent extends JPanel {
	private Frame frame;

	public StackFrameComponent(Frame frame) {
		this.frame = frame;
		setBackground(Constants.colorFrameBG);
		setLayout(new BorderLayout());
		setBorder(JBUI.Borders.empty(8));

		Box b = Box.createVerticalBox();
		add(b);

		JLabel labelName = new JLabel(frame.name, JLabel.LEFT);
		labelName.setFont(Constants.fontUIMono);
		b.add(labelName);

		for (Map.Entry<String, Value> local : frame.locals.entrySet()) {
			JLabel localLabel = new JLabel(local.getKey(), JLabel.RIGHT);
			b.add(localLabel);
			b.add(new ValueComponent(local.getValue()));
		}
	}
}
