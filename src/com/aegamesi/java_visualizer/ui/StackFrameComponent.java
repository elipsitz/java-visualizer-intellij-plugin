package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.Frame;
import com.aegamesi.java_visualizer.model.Value;
import com.intellij.util.ui.JBUI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;

public class StackFrameComponent extends JPanel {
	private Frame frame;

	public StackFrameComponent(Frame frame) {
		this.frame = frame;
		setBackground(Constants.colorFrameBG);
		setLayout(new GridBagLayout());
		setBorder(JBUI.Borders.empty(8));


		JLabel labelName = new JLabel(frame.name, JLabel.LEFT);
		labelName.setFont(Constants.fontUIMono);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		add(labelName, c);

		c.gridwidth = 1;
		c.insets.top = 4;
		int y = 1;
		for (Map.Entry<String, Value> local : frame.locals.entrySet()) {
			JLabel localLabel = new JLabel(local.getKey(), JLabel.RIGHT);
			ValueComponent value = new ValueComponent(local.getValue());

			c.gridx = 0;
			c.gridy = y;
			c.anchor = GridBagConstraints.LINE_END;
			c.weightx = 1.0;
			c.insets.left = 0;
			add(localLabel, c);
			c.gridx = 1;
			c.gridy = y;
			c.anchor = GridBagConstraints.LINE_START;
			c.weightx = 0.0;
			c.insets.left = 8;
			add(value, c);
			y += 1;
		}
	}
}