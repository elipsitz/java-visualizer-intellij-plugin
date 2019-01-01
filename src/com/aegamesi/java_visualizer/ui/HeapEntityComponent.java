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
import java.util.ArrayList;
import java.util.List;
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
			mainPanel = new PanelObject((HeapObject) entity);
		} else if (entity instanceof HeapList) {
			mainPanel = new PanelList((HeapList) entity);
		}

		if (mainPanel != null) {
			add(mainPanel, BorderLayout.WEST);
		}
	}

	private class PanelObject extends JPanel {
		private int hsplit;
		private int[] vsplits;

		PanelObject(HeapObject e) {
			setLayout(null);

			int keyWidth = 0;
			int valueWidth = 0;

			List<JLabel> keys = new ArrayList<>();
			List<ValueComponent> vals = new ArrayList<>();
			for (Map.Entry<String, Value> local : e.fields.entrySet()) {
				JLabel key = new JLabel(local.getKey(), JLabel.RIGHT);
				ValueComponent val = new ValueComponent(viz, local.getValue());
				keyWidth = Math.max(keyWidth, key.getPreferredSize().width);
				valueWidth = Math.max(valueWidth, val.getPreferredSize().width);
				keys.add(key);
				vals.add(val);
				add(key);
				add(val);
			}

			int y = 0;
			vsplits = new int[e.fields.size()];
			for (int i = 0; i < e.fields.size(); i += 1) {
				JLabel key = keys.get(i);
				ValueComponent val = vals.get(i);
				int h = Math.max(key.getPreferredSize().height, val.getPreferredSize().height);

				y += 4;
				key.setBounds(4, y, keyWidth, h);
				val.setBounds(4 + keyWidth + 4 + 4, y, valueWidth, h);
				y += h + 4;
				vsplits[i] = y;
			}

			setPreferredSize(new Dimension(16 + keyWidth + valueWidth, y));
			hsplit = 4 + keyWidth + 4;
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(Constants.colorHeapKey);
			g.fillRect(0, 0, hsplit, getHeight());
			g.setColor(Constants.colorHeapVal);
			g.fillRect(hsplit, 0, getWidth() - hsplit, getHeight());

			g.setColor(Constants.colorHeapBorder);
			g.drawLine(hsplit, 0, hsplit, getHeight() - 1);
			for (int s : vsplits) {
				g.drawLine(0, s - 1, getWidth(), s - 1);
			}
		}
	}

	private class PanelList extends JPanel {
		private int[] splits;

		PanelList(HeapList e) {
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
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			g.setColor(Constants.colorHeapBorder);
			g.drawLine(1, getHeight() - 1, getWidth(), getHeight() - 1);
			for (int s : splits) {
				g.drawLine(s + 1, 0, s + 1, getHeight() - 1);
			}
		}
	}
}
