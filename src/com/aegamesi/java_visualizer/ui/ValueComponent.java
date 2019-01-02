package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.Value;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

class ValueComponent extends JPanel {
	private Value val;
	private boolean active;

	ValueComponent(VisualizationPanel viz, Value val) {
		this(viz, val, true);
	}

	ValueComponent(VisualizationPanel viz, Value val, boolean active) {
		this.val = val;
		this.active = active;
		viz.registerValueComponent(this);

		setOpaque(false);
		setLayout(new BorderLayout());


		JLabel label = new JLabel(val.toString());
		if (val.type == Value.Type.STRING) {
			label.setFont(Constants.fontUIMono);
		} else {
			label.setFont(Constants.fontUI);
		}
		if (val.type == Value.Type.REFERENCE) {
			label.setText(" ");
		}
		add(label);
	}

	Value getValue() {
		return val;
	}

	boolean isActive() {
		return active;
	}
}
