package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.Value;

import javax.swing.JLabel;

public class ValueComponent extends JLabel {
	private Value val;

	public ValueComponent(Value val) {
		this.val = val;

		setText(val.toString());
		if (val.type == Value.Type.STRING) {
			setFont(Constants.fontUIMono);
		} else {
			setFont(Constants.fontUI);
		}
	}
}
