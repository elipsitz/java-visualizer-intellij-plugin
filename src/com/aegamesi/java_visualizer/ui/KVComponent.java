package com.aegamesi.java_visualizer.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

/**
 * A component that displays a table of two columns of components with
 * some additional drawing
 */
class KVComponent extends JPanel {
	private int hsplit;
	private int[] vsplits;
	private int padding;
	private Color colorLeft;
	private Color colorRight;
	private Color colorBorder;

	private List<? extends JComponent> keys;
	private List<? extends JComponent> vals;

	KVComponent() {
		setLayout(null);
	}

	void setPadding(int padding) {
		this.padding = padding;
	}

	void setComponents(List<? extends JComponent> keys, List<? extends JComponent> vals) {
		if (keys.size() != vals.size()) {
			throw new IllegalArgumentException();
		}
		this.keys = keys;
		this.vals = vals;
	}

	void setColors(Color colorLeft, Color colorRight, Color colorBorder) {
		this.colorLeft = colorLeft;
		this.colorRight = colorRight;
		this.colorBorder = colorBorder;
	}

	void build() {
		int keyWidth = 0;
		int valueWidth = 0;

		int n = keys.size();
		for (int i = 0; i < n; i++) {
			keyWidth = Math.max(keyWidth, keys.get(i).getPreferredSize().width);
			valueWidth = Math.max(valueWidth, vals.get(i).getPreferredSize().width);
		}

		int y = 0;
		vsplits = new int[n];
		for (int i = 0; i < n; i += 1) {
			JComponent key = keys.get(i);
			JComponent val = vals.get(i);
			Dimension keySize = key.getPreferredSize();
			Dimension valSize = val.getPreferredSize();
			int h = Math.max(keySize.height, valSize.height);

			add(key);
			add(val);
			y += padding;
			key.setBounds(padding + keyWidth - keySize.width, y, keySize.width, h);
			val.setBounds((padding * 3) + keyWidth, y, valueWidth, h);
			y += h + padding;
			vsplits[i] = y;
		}

		setPreferredSize(new Dimension((padding * 4) + keyWidth + valueWidth, y));
		hsplit = keyWidth + (padding * 2);
	}


	@Override
	protected void paintComponent(Graphics g) {
		if (colorLeft != null) {
			g.setColor(Constants.colorHeapKey);
			g.fillRect(0, 0, hsplit, getHeight());
		}

		if (colorRight != null) {
			g.setColor(Constants.colorHeapVal);
			g.fillRect(hsplit, 0, getWidth() - hsplit, getHeight());
		}

		if (colorBorder != null) {
			g.setColor(Constants.colorHeapBorder);
			g.drawLine(hsplit, 0, hsplit, getHeight() - 1);
			for (int s : vsplits) {
				g.drawLine(0, s - 1, getWidth(), s - 1);
			}
		}
	}
}
