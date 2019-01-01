package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.HeapEntity;
import com.aegamesi.java_visualizer.model.HeapList;
import com.aegamesi.java_visualizer.model.HeapObject;
import com.aegamesi.java_visualizer.model.Value;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;

class HeapEntityComponent extends JPanel {
	private VisualizationPanel viz;
	private HeapEntity entity;

	HeapEntityComponent(VisualizationPanel viz, HeapEntity entity) {
		this.viz = viz;
		this.entity = entity;

		setOpaque(false);
		setLayout(new BorderLayout());
		// setBorder(JBUI.Borders.empty(8));

		JLabel topLabel = new JLabel(entity.label);
		topLabel.setFont(Constants.fontUISmall);
		topLabel.setForeground(Constants.colorHeapLabel);
		add(topLabel, BorderLayout.NORTH);

		JPanel mainPanel = null;
		if (entity instanceof HeapObject) {
			mainPanel = makePanelForObject((HeapObject) entity);
		} else if (entity instanceof HeapList) {
			mainPanel = new PanelList((HeapList) entity);
		}

		if (mainPanel != null) {
			add(mainPanel, BorderLayout.WEST);
		}
	}

	private JPanel makePanelForObject(HeapObject e) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.insets.top = 4;
		c.fill = GridBagConstraints.BOTH;
		int y = 1;
		for (Map.Entry<String, Value> local : e.fields.entrySet()) {
			JLabel localLabel = new JLabel(local.getKey(), JLabel.RIGHT);
			ValueComponent value = new ValueComponent(viz, local.getValue());

			c.gridx = 0;
			c.gridy = y;
			c.weightx = 1.0;
			c.insets.left = 0;
			panel.add(localLabel, c);
			c.gridx = 1;
			c.gridy = y;
			c.weightx = 0.0;
			c.insets.left = 8;
			panel.add(value, c);
			y += 1;
		}
		return panel;
	}

	private class PanelList extends JPanel {
		private int[] splits;

		public PanelList(HeapList e) {
			setBackground(Constants.colorHeapVal);
			setLayout(null);
			splits = new int[e.items.size()];

			int height = 0;
			int x = 0;
			int y = 8;
			for (int i = 0; i < e.items.size(); i++) {
				splits[i] = x;
				ValueComponent value = new ValueComponent(viz, e.items.get(i));
				Dimension size = value.getPreferredSize();
				x += 8;
				value.setBounds(x, y, size.width, size.height);
				x += size.width + 8;
				add(value);
				height = Math.max(height, size.height);
			}
			height += 16;
			setPreferredSize(new Dimension(x, height));
		}

		@Override
		protected void paintComponent(Graphics _g) {
			super.paintComponent(_g);
			Graphics2D g = (Graphics2D) _g;

			g.setColor(Constants.colorHeapBorder);
			g.drawLine(1, getHeight() - 1, getWidth(), getHeight() - 1);
			for (int s : splits) {
				g.drawLine(s + 1, 0, s + 1, getHeight() - 1);
			}
		}
	}
}
