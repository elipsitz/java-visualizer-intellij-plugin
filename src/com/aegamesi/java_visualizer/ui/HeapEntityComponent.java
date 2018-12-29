package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.HeapEntity;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;

public class HeapEntityComponent extends JPanel {
	private HeapEntity entity;

	public HeapEntityComponent(HeapEntity entity) {
		this.entity = entity;

		setBackground(new Color(0xFF, 0xFF, 0xC6));
		JLabel labelName = new JLabel(entity.label);
		labelName.setFont(Constants.fontUI);
		add(labelName);
	}
}
