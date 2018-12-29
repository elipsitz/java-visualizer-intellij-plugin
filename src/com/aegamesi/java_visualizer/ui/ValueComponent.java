package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.Value;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ValueComponent extends JPanel {
	private Value val;

	public ValueComponent(Value val) {
		this.val = val;

		setBackground(Constants.colorFrameBG.brighter());
		setLayout(new BorderLayout());

		JLabel label = new JLabel(val.toString());
		if (val.type == Value.Type.STRING) {
			label.setFont(Constants.fontUIMono);
		} else {
			label.setFont(Constants.fontUI);
		}
		add(label);
	}
}
