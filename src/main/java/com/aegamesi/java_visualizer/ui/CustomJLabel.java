package com.aegamesi.java_visualizer.ui;

import javax.swing.JLabel;
import java.awt.Dimension;

class CustomJLabel extends JLabel {
	CustomJLabel(String str) {
		this(str, LEFT);
	}

	CustomJLabel(String str, int horizontalAlignment) {
		super(str, horizontalAlignment);
		setForeground(Constants.colorText);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension(d.width + 4, d.height);
	}
}
