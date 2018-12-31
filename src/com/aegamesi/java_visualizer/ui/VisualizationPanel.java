package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.model.HeapEntity;
import com.aegamesi.java_visualizer.model.Value;
import com.intellij.util.containers.hash.HashMap;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VisualizationPanel extends JPanel {
	private ExecutionTrace trace = null;

	private List<ValueComponent> referenceComponents;
	private Map<Long, HeapEntityComponent> heapEntityComponents;

	public VisualizationPanel() {
		setBackground(Constants.colorBackground);
		setLayout(null);
		referenceComponents = new ArrayList<>();
		heapEntityComponents = new HashMap<>();
	}

	public void setTrace(ExecutionTrace t) {
		this.trace = t;
		referenceComponents.clear();
		heapEntityComponents.clear();
		removeAll();

		buildUI();

		revalidate();
		repaint();
	}

	private void buildUI() {
		Box frames = Box.createVerticalBox();
		JLabel frameLabel = new JLabel("Frames");
		frameLabel.setFont(Constants.fontMessage);
		frameLabel.setHorizontalAlignment(JLabel.RIGHT);
		frames.add(frameLabel);
		frames.add(Box.createVerticalStrut(24));
		for (int i = 0; i < trace.frames.size(); i++) {
			frames.add(new StackFrameComponent(this, trace.frames.get(i), i == 0));
			frames.add(Box.createVerticalStrut(8));
		}
		add(frames);

		Box heap = Box.createVerticalBox();
		for (Map.Entry<Long, HeapEntity> pair : trace.heap.entrySet()) {
			HeapEntityComponent obj = new HeapEntityComponent(this, pair.getValue());
			heapEntityComponents.put(pair.getKey(), obj);
			heap.add(obj);
			heap.add(Box.createVerticalStrut(8));
		}
		add(heap);

		Dimension sizeFrames = frames.getPreferredSize();
		Dimension sizeHeap = heap.getPreferredSize();
		frames.setBounds(
				Constants.outerPadding,
				Constants.outerPadding,
				sizeFrames.width,
				sizeFrames.height
		);
		heap.setBounds(
				Constants.outerPadding + sizeFrames.width + Constants.centerMargin,
				Constants.outerPadding,
				sizeHeap.width,
				sizeHeap.height
		);

		setPreferredSize(new Dimension(
				Constants.outerPadding + sizeFrames.width + Constants.centerMargin + sizeHeap.width,
				Constants.outerPadding + Math.max(sizeFrames.height, sizeHeap.height)
		));
	}

	void registerValueComponent(ValueComponent component) {
		if (component.getValue().type == Value.Type.REFERENCE) {
			referenceComponents.add(component);
		}
	}

	@Override
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);

		paintArrows((Graphics2D) g);
	}

	private void paintArrows(Graphics2D g) {
		for (ValueComponent ref : referenceComponents) {
			g.setColor(Color.black);

			Rectangle refBounds = getTrueComponentBounds(ref);
			long refId = ref.getValue().reference;
			Rectangle objBounds = getTrueComponentBounds(heapEntityComponents.get(refId));

			g.drawLine(refBounds.x, refBounds.y, objBounds.x, objBounds.y + (objBounds.height / 2));
		}
	}

	/**
	 * Returns the Bounds of the component, relative to this panel.
	 */
	private Rectangle getTrueComponentBounds(Component c) {
		Rectangle r = c.getBounds();
		c = c.getParent();
		while (c != null && c != this) {
			Point loc = c.getLocation();
			r.translate(loc.x, loc.y);
			c = c.getParent();
		}
		return r;
	}
}
