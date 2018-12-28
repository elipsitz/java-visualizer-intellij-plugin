package com.aegamesi.java_visualizer.plugin;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.intellij.ui.JBColor;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class JVPanel extends JPanel {
	private ExecutionTrace trace = null;

	private Font fontMessage = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
	private Font fontUI = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	private Font fontUIMono = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	private FontMetrics metricsMessage;
	private FontMetrics metricsUI;
	private FontMetrics metricsUIMono;

	private Color colorFrameText = new Color(0x0, 0x0, 0x0);
	private Color colorFrameBG = new Color(0xE2, 0xEB, 0xF6);
	private Color colorFrameOutline = new Color(0xAA, 0xAA, 0xAA);

	public JVPanel() {
		add(new JLabel("test jlabel"));
	}

	public void setTrace(ExecutionTrace t) {
		this.trace = t;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int w = getSize().width;
		int h = getSize().height;

		metricsUIMono = g.getFontMetrics(fontUIMono);

		g.setColor(JBColor.white);
		g.fillRect(0, 0, w, h);

		if (trace == null) {
			g.setColor(JBColor.black);
			g.setFont(fontMessage);
			g.drawString("No trace loaded.", 0, 32);
		} else {
			paintTrace(g);
		}
	}

	private void paintTrace(Graphics2D g) {
		g.setColor(JBColor.black);
		g.setFont(fontUIMono);

		int y = 16;
		for (int i = 0; i < trace.frames.size(); i += 1) {
			g.setColor(colorFrameBG);
			g.fillRect(8, y, 160, 16 + metricsUIMono.getHeight());
			g.setColor(colorFrameText);
			g.drawString(trace.frames.get(i).name, 16, y + 8 + metricsUIMono.getHeight());

			y += 16 + metricsUIMono.getHeight() + 16;
		}
	}
}
