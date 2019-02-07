package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.Frame;
import com.aegamesi.java_visualizer.model.Value;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class StackFrameComponent extends JPanel {
	private Frame frame;
	private VisualizationPanel viz;

	StackFrameComponent(VisualizationPanel viz, Frame frame, boolean first) {
		this.frame = frame;
		this.viz = viz;
		setBackground(first ? Constants.colorFrameBGFirst : Constants.colorFrameBG);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MatteBorder(0, 1, 0, 0, Constants.colorFrameOutline));

		JLabel labelName = new CustomJLabel(frame.name, JLabel.LEFT);
		labelName.setFont(Constants.fontUIMono);
		labelName.setForeground(Constants.colorText);
		labelName.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		labelName.setAlignmentX(RIGHT_ALIGNMENT);
		labelName.setMaximumSize(Constants.maxDimension);
		add(labelName);

		List<JComponent> keys = new ArrayList<>();
		List<JComponent> vals = new ArrayList<>();
		for (Map.Entry<String, Value> local : frame.locals.entrySet()) {
			JLabel localLabel = new CustomJLabel(local.getKey(), JLabel.RIGHT);
			localLabel.setForeground(Constants.colorText);
			localLabel.setFont(Constants.fontUI);
			ValueComponent value = new ValueComponent(viz, local.getValue(), first);
			Border b1 = new MatteBorder(0, 1, 1, 0, Constants.colorFrameOutline);
			Border b2 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
			value.setBorder(new CompoundBorder(b1, b2));
			keys.add(localLabel);
			vals.add(value);
		}
		KVComponent locals = new KVComponent();
		locals.setPadding(4);
		locals.setComponents(keys, vals);
		locals.build();
		locals.setAlignmentX(RIGHT_ALIGNMENT);
		locals.setMaximumSize(locals.getPreferredSize());
		add(locals);
	}

	@Override
	public Dimension getMaximumSize() {
		return Constants.maxDimension;
	}
}
