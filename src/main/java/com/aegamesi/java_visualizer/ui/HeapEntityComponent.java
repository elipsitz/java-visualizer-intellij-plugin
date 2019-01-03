package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.HeapEntity;
import com.aegamesi.java_visualizer.model.HeapList;
import com.aegamesi.java_visualizer.model.HeapMap;
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
	private List<ValueComponent> valueComponents = new ArrayList<>();

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
		} else if (entity instanceof HeapMap) {
			mainPanel = new PanelMap((HeapMap) entity);
		}

		if (mainPanel != null) {
			add(mainPanel, BorderLayout.WEST);
		}
	}

	HeapEntity getEntity() {
		return entity;
	}

	List<ValueComponent> getValueComponents() {
		return valueComponents;
	}

	private class PanelObject extends KVComponent {
		PanelObject(HeapObject e) {
			List<JLabel> keys = new ArrayList<>();
			List<ValueComponent> vals = new ArrayList<>();
			for (Map.Entry<String, Value> local : e.fields.entrySet()) {
				JLabel key = new JLabel(local.getKey(), JLabel.RIGHT);
				ValueComponent val = new ValueComponent(viz, local.getValue());
				valueComponents.add(val);
				keys.add(key);
				vals.add(val);
			}

			setColors(Constants.colorHeapKey, Constants.colorHeapVal, Constants.colorHeapBorder);
			setPadding(Constants.padHeapMap);
			setComponents(keys, vals);
			build();
		}
	}

	private class PanelMap extends KVComponent {
		PanelMap(HeapMap e) {
			List<ValueComponent> keys = new ArrayList<>();
			List<ValueComponent> vals = new ArrayList<>();
			for (HeapMap.Pair entry : e.pairs) {
				ValueComponent key = new ValueComponent(viz, entry.key);
				ValueComponent val = new ValueComponent(viz, entry.val);
				valueComponents.add(val);
				valueComponents.add(val);
				keys.add(key);
				vals.add(val);
			}

			setColors(Constants.colorHeapKey, Constants.colorHeapVal, Constants.colorHeapBorder);
			setPadding(Constants.padHeapMap);
			setComponents(keys, vals);
			build();
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
			for (int i = 0; i < e.items.size(); i++) {
				splits[i] = x;
				ValueComponent value = new ValueComponent(viz, e.items.get(i));
				valueComponents.add(value);
				Dimension size = value.getPreferredSize();
				JLabel indexLabel = new JLabel(Integer.toString(i));
				indexLabel.setFont(Constants.fontUISmall);
				indexLabel.setForeground(Constants.colorHeapLabel);
				Dimension indexSize = indexLabel.getPreferredSize();
				indexLabel.setBounds(x + 4, 4, indexSize.width, indexSize.height);
				add(indexLabel);
				x += 8;
				value.setBounds(x, 4 + indexSize.height + 4, size.width, size.height);
				x += size.width + 8;
				add(value);
				height = Math.max(height, indexSize.height + size.height);
			}
			height += 8 + 8;
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
